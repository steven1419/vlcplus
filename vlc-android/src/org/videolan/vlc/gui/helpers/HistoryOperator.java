package org.videolan.vlc.gui.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class HistoryOperator {
    private static final String TAG = "VLC/HistoryOperator";

    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "VLCplus";
    private static final String TIME_FILE = "vlc_time_record.txt";
    private static final String VIDEO_FILE = "vlc_video_record.txt";

    private ArrayList<String> timeRecord = new ArrayList<>();
    private ArrayList<String> videoRecord = new ArrayList<>();

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static Date date;

    private static long startTime;
    private static long endTime;
    private static String currentDate = "";
    private int currentDateIndex;
    private long currentDuration;
    private int currentVideoIndex;

    private static volatile HistoryOperator historyOperatorInstance = new HistoryOperator();

    public HistoryOperator() {
        getInit();
    }

    public static HistoryOperator getInstance() {
        return historyOperatorInstance;
    }

    private void getInit() {
        startTime = System.currentTimeMillis();
        date = new Date(startTime);
        currentDate = simpleDateFormat.format(date);
        Log.d(TAG, "getInit: FILE_PATH: " + FILE_PATH);
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        getTimeRecord();
        getVideoRecord();
    }

    //time record load & save
    private void getTimeRecord() {
        loadTimeList();
        loadCurrentDateIndex();
    }

    private void loadTimeList() {
        File file = new File(FILE_PATH, TIME_FILE);
        String readline = "";
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((readline = br.readLine()) != null) {
                    timeRecord.add(readline);
                }
                br.close();
            } catch (IOException e) {
                Log.e(TAG, "loadTimeList", e);
            }
        }
        Collections.sort(timeRecord);
    }

    private void loadCurrentDateIndex() {
        currentDateIndex = -1;
        for (int i = timeRecord.size() - 1; i >= 0; i--) {
            if (timeRecord.get(i).contains(currentDate)) {
                currentDateIndex = i;
                currentDuration = Long.valueOf(timeRecord.get(i).split(",")[1]);
                break;
            }
        }
        if (currentDateIndex == -1) {
            currentDateIndex = timeRecord.size();
            currentDuration = 0;
            timeRecord.add(currentDate + "," + currentDuration);
        }
    }

    public void saveTimeRecord() {
        endTime = System.currentTimeMillis();
        currentDuration += endTime - startTime;
        startTime = endTime;
        timeRecord.set(currentDateIndex, currentDate + "," + currentDuration);
        saveTimeList();
    }

    private void saveTimeList() {
        File file = new File(FILE_PATH, TIME_FILE);
        deleteFile(file);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < timeRecord.size(); i++) {
                if (i != 0) {
                    bw.newLine();
                }
                bw.write(timeRecord.get(i));
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, "saveTimeList", e);
        }
    }

    public ArrayList<String> getTimeList() {
        saveTimeRecord();
        return timeRecord;
    }

    //video record load & save
    private void getVideoRecord() {
        loadVideoList();
        loadCurrentVideoIndex();
    }

    private void loadVideoList() {
        File file = new File(FILE_PATH, VIDEO_FILE);
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String readline = "";
                while ((readline = br.readLine()) != null) {
                    videoRecord.add(readline);
                }
                br.close();
            } catch (IOException e) {
                Log.e(TAG, "loadVideoList", e);
            }
        }
        Collections.sort(videoRecord);
    }

    private void loadCurrentVideoIndex() {
        currentVideoIndex = -1;
        for (int i = 0; i < videoRecord.size(); i++) {
            if (videoRecord.get(i).contains(currentDate)) {
                currentVideoIndex = i;
                break;
            }
        }
    }

    public void saveVideoRecord(String title, String uri) {
        String inputRecord = currentDate + "," + uri + "," + title;
        boolean hasRecord = false;
        Log.d(TAG, "saveVideoRecord, uri " + uri);
        if (currentVideoIndex != -1) {
            for (int i = currentVideoIndex; i < videoRecord.size() && videoRecord.get(i).contains(currentDate); i++) {
                Log.d(TAG, "saveVideoRecord, videoRecord" + videoRecord.get(i));
                if (videoRecord.get(i).split(",")[1].equals(uri)) {
                    hasRecord = true;
                    break;
                }
            }
            if (!hasRecord) {
                videoRecord.add(inputRecord);
            }
        } else {
            videoRecord.add(inputRecord);
            currentVideoIndex = videoRecord.size() - 1;
        }
        saveVideoList();
    }

    private void saveVideoList() {
        File file = new File(FILE_PATH, VIDEO_FILE);
        deleteFile(file);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < videoRecord.size(); i++) {
                if (i != 0) {
                    bw.newLine();
                }
                bw.write(videoRecord.get(i));
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, "saveVideoList", e);
        }
    }

    public ArrayList<String> getVideoList() {
        saveVideoList();
        return videoRecord;
    }

    private void deleteFile(File file) {
        Log.d(TAG, "deleteFile: " + file.getName());
        if (file.exists()) {
            boolean ret = file.delete();
            Log.d(TAG, "deleteFile: " + ret);
        }
    }
}