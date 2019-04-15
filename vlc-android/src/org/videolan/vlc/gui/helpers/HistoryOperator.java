package org.videolan.vlc.gui.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryOperator {
    private static final String TAG = "VLC/HistoryOperator";

    private String filePath = "";
    private String timeFile = "vlc_time_record.txt";
    private String videoFile = "vlc_video_record.txt";

    private static ArrayList<String> timeRecord;
    private static ArrayList<String> videoRecord;
    private static String lastTimeRecord = "";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static Date date;

    private static long legacyTime;
    private static long startTime;
    private static long endTime;
    private static String currentDate = "";
    private static String currentDuration = "";
    private static boolean hasLegacy = false;

    public HistoryOperator() {
        getInit();
    }

    private void getInit() {
        startTime = System.currentTimeMillis();
        date = new Date(startTime);
        currentDate = simpleDateFormat.format(date);
        if (timeRecord == null) {
            timeRecord = new ArrayList<>();
        }
        if (videoRecord == null) {
            videoRecord = new ArrayList<>();
        }

    }

    public void getTimeRecord() {
        loadTimeList();
        loadTimeLegacy();
    }

    public void saveTimeRecord() {
        endTime = System.currentTimeMillis();
        currentDuration = String.valueOf(legacyTime + endTime - startTime);
        if (!hasLegacy) {
            hasLegacy = true;
        } else {
            timeRecord.remove(timeRecord.size() - 1);
        }
        timeRecord.add(currentDate + "," + currentDuration);
        saveTimeList();
        return;
    }

    public ArrayList<String> getTimeList() {
        saveTimeRecord();
        return timeRecord;
    }

    public void getVideoRecord() {
        loadVideoList();
    }

    public ArrayList<String> getVideoList() {
        saveVideoList();
        return videoRecord;
    }

    public void saveVideoRecord(String title) {
        String inputRecord = currentDate + "," + title;
        if (!videoRecord.contains(inputRecord)) {
            videoRecord.add(inputRecord);
        }
        saveVideoList();
    }

    private void loadTimeList() {
        File file = new File(filePath, timeFile);
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String readline = "";
                while ((readline = br.readLine()) != null) {
                    timeRecord.add(readline);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (timeRecord.size() > 0) {
                lastTimeRecord = timeRecord.get(timeRecord.size() - 1);
            }
        }
    }

    private void loadTimeLegacy() {
        if (!lastTimeRecord.isEmpty() && lastTimeRecord.contains(",") && lastTimeRecord.split(",")[0].equals(currentDate)) {
            legacyTime = Long.valueOf(lastTimeRecord.split(",")[1]);
            hasLegacy = true;
        } else {
            legacyTime = 0;
            hasLegacy = false;
        }
    }

    private void saveTimeList() {
        File file = new File(filePath, timeFile);
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
            e.printStackTrace();
        }
    }

    private void loadVideoList() {
        File file = new File(filePath, videoFile);
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String readline = "";
                while ((readline = br.readLine()) != null) {
                    videoRecord.add(readline);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveVideoList() {
        File file = new File(filePath, videoFile);
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
            e.printStackTrace();
        }
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}
