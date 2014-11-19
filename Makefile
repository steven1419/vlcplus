# Sources and objects

export ANDROID_HOME=$(ANDROID_SDK)

ARCH = $(ANDROID_ABI)

SRC=libvlc
APP_SRC=vlc-android
JAVA_SOURCES=$(shell find $(APP_SRC)/src/org/videolan -name "*.java" -o -name "*.aidl")
JAVA_SOURCES+=$(shell find $(SRC)/src/org/videolan -name "*.java" -o -name "*.aidl")
JAVA_SOURCES+=$(shell find $(APP_SRC)/res -name "*.xml" -o -name "*.png") 
JNI_SOURCES=$(SRC)/jni/*.c $(SRC)/jni/*.h
LIBVLC_LIBS = libvlcjni

ifneq ($(HAVE_64),1)
# Can't link with 32bits symbols.
# Not a problem since MediaCodec should work on 64bits devices (android-21)
LIBVLC_LIBS += libiomx.14 libiomx.13 libiomx.10
endif

# The following iomx libs are used for DEBUG only.
# (after android Jelly Bean, we prefer to use MediaCodec instead of iomx)
#LIBVLC_LIBS += libiomx.19 libiomx.18

LIBVLC_LIBS += libanw.10 libanw.13 libanw.14 libanw.18

LIBVLCJNI= $(addprefix $(SRC)/obj/local/$(ARCH)/,$(addsuffix .so,$(LIBVLC_LIBS)))

LIBVLCJNI_H=$(SRC)/jni/libvlcjni.h

PRIVATE_LIBDIR=android-libs
PRIVATE_LIBS=$(PRIVATE_LIBDIR)/libstagefright.so $(PRIVATE_LIBDIR)/libmedia.so $(PRIVATE_LIBDIR)/libutils.so $(PRIVATE_LIBDIR)/libcutils.so $(PRIVATE_LIBDIR)/libbinder.so $(PRIVATE_LIBDIR)/libui.so $(PRIVATE_LIBDIR)/libhardware.so

ifneq ($(V),)
ANT_OPTS += -v
VERBOSE =
GEN =
else
VERBOSE = @
GEN = @echo "Generating" $@;
endif

ifeq ($(RELEASE),1)
ANT_TARGET = release
VLC_APK=$(APP_SRC)/bin/VLC-release-unsigned.apk
NDK_DEBUG=0
else
ANT_TARGET = debug
VLC_APK=$(APP_SRC)/bin/VLC-debug.apk
NDK_DEBUG=1
endif

define build_apk
	@echo
	@echo "=== Building $(VLC_APK) for $(ARCH) ==="
	@echo
	date +"%Y-%m-%d" > $(APP_SRC)/assets/builddate.txt
	echo `id -u -n`@`hostname` > $(APP_SRC)/assets/builder.txt
	git rev-parse --short HEAD > $(APP_SRC)/assets/revision.txt
	./gen-env.sh $(APP_SRC)
	$(VERBOSE)cd $(APP_SRC) && ant $(ANT_OPTS) $(ANT_TARGET)
endef

$(VLC_APK): $(LIBVLCJNI) $(JAVA_SOURCES)
	$(call build_apk)

VLC_MODULES=`./find_modules.sh $(VLC_BUILD_DIR)`

$(LIBVLCJNI_H):
	$(VERBOSE)if [ -z "$(VLC_BUILD_DIR)" ]; then echo "VLC_BUILD_DIR not defined" ; exit 1; fi
	$(GEN)modules="$(VLC_MODULES)" ; \
	if [ -z "$$modules" ]; then echo "No VLC modules found in $(VLC_BUILD_DIR)/modules"; exit 1; fi; \
	DEFINITION=""; \
	BUILTINS="const void *vlc_static_modules[] = {\n"; \
	for file in $$modules; do \
		name=`echo $$file | sed 's/.*\.libs\/lib//' | sed 's/_plugin\.a//'`; \
		DEFINITION=$$DEFINITION"int vlc_entry__$$name (int (*)(void *, void *, int, ...), void *);\n"; \
		BUILTINS="$$BUILTINS vlc_entry__$$name,\n"; \
	done; \
	BUILTINS="$$BUILTINS NULL\n};\n"; \
	printf "/* Autogenerated from the list of modules */\n $$DEFINITION\n $$BUILTINS\n" > $@

$(PRIVATE_LIBDIR)/%.so: $(PRIVATE_LIBDIR)/%.c
	$(GEN)$(TARGET_TUPLE)-gcc $< -shared -o $@ --sysroot=$(ANDROID_NDK)/platforms/$(ANDROID_API)/arch-$(PLATFORM_SHORT_ARCH)

$(PRIVATE_LIBDIR)/%.c: $(PRIVATE_LIBDIR)/%.symbols
	$(VERBOSE)rm -f $@
	$(GEN)for s in `cat $<`; do echo "void $$s() {}" >> $@; done

$(LIBVLCJNI): $(JNI_SOURCES) $(LIBVLCJNI_H) $(PRIVATE_LIBS)
	@if [ -z "$(VLC_BUILD_DIR)" ]; then echo "VLC_BUILD_DIR not defined" ; exit 1; fi
	@if [ -z "$(ANDROID_NDK)" ]; then echo "ANDROID_NDK not defined" ; exit 1; fi
	@echo
	@echo "=== Building libvlcjni ==="
	@echo
	$(VERBOSE)if [ -z "$(VLC_SRC_DIR)" ] ; then VLC_SRC_DIR=./vlc; fi ; \
	if [ -z "$(VLC_CONTRIB)" ] ; then VLC_CONTRIB="$$VLC_SRC_DIR/contrib/$(TARGET_TUPLE)"; fi ; \
	vlc_modules="$(VLC_MODULES)" ; \
	if [ `echo "$(VLC_BUILD_DIR)" | head -c 1` != "/" ] ; then \
		vlc_modules="`echo $$vlc_modules|sed \"s|$(VLC_BUILD_DIR)|../$(VLC_BUILD_DIR)|g\"`" ; \
        VLC_BUILD_DIR="../$(VLC_BUILD_DIR)"; \
	fi ; \
	[ `echo "$$VLC_CONTRIB" | head -c 1` != "/" ] && VLC_CONTRIB="../$$VLC_CONTRIB"; \
	[ `echo "$$VLC_SRC_DIR" | head -c 1` != "/" ] && VLC_SRC_DIR="../$$VLC_SRC_DIR"; \
	$(ANDROID_NDK)/ndk-build -C $(SRC) \
		VLC_SRC_DIR="$$VLC_SRC_DIR" \
		VLC_CONTRIB="$$VLC_CONTRIB" \
		VLC_BUILD_DIR="$$VLC_BUILD_DIR" \
		VLC_MODULES="$$vlc_modules" \
		NDK_DEBUG=$(NDK_DEBUG) \
		TARGET_CFLAGS="$$VLC_EXTRA_CFLAGS" \
		LIBVLC_LIBS="$(LIBVLC_LIBS)"

apk:
	$(call build_apk)

apkclean:
	rm -f $(VLC_APK)

lightclean:
	cd $(SRC) && rm -rf libs obj
	cd $(APP_SRC) && rm -rf bin $(VLC_APK)
	rm -f $(PRIVATE_LIBDIR)/*.so $(PRIVATE_LIBDIR)/*.c

clean: lightclean
	rm -rf $(APP_SRC)/gen java-libs/*/gen java-libs/*/bin .sdk vlc-sdk/ vlc-sdk.7z

jniclean: lightclean
	rm -rf $(LIBVLCJNI) $(LIBVLCJNI_H) $(APP_SRC)/libs/

distclean: clean jniclean

install: $(VLC_APK)
	@echo "=== Installing VLC on device ==="
	adb wait-for-device
	adb install -r $(VLC_APK)

uninstall:
	adb wait-for-device
	adb uninstall org.videolan.vlc

run:
	@echo "=== Running VLC on device ==="
	adb wait-for-device
ifeq ($(URL),)
	adb shell am start -n org.videolan.vlc/org.videolan.vlc.gui.MainActivity
else
	adb shell am start -n org.videolan.vlc/org.videolan.vlc.gui.video.VideoPlayerActivity $(URL)
endif

build-and-run: install run

apkclean-run: apkclean build-and-run
	adb logcat -c

distclean-run: distclean build-and-run
	adb logcat -c

vlc-sdk.7z: .sdk
	7z a $@ vlc-sdk/

.sdk:
	mkdir -p vlc-sdk/libs
	cd libvlc; cp -r libs/* ../vlc-sdk/libs
	mkdir -p vlc-sdk/src/org/videolan
	cp -r libvlc/src/org/videolan/libvlc vlc-sdk/src/org/videolan
	touch $@

.PHONY: lightclean clean jniclean distclean distclean-run apkclean apkclean-run install run build-and-run
