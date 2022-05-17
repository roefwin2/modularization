package com.ellcie_healthy.ble_library.ble.streaming;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

public class StreamingUtils {

    private static final String TAG = "StreamingUtils";


    public static class StreamingTripInfo {
        private String mTripId = "";
        private String mUserId = "";
        private String mSerialNumber = "";

        public StreamingTripInfo(String tripId, String userId, String serialNumber) {
            mTripId = tripId;
            mUserId = userId;
            mSerialNumber = serialNumber;
        }

        public String getTripId() {
            return mTripId;
        }

        public String getUserId() {
            return mUserId;
        }

        public String getSerialNumber() {
            return mSerialNumber;
        }

        @Override
        @NonNull
        public String toString() {
            return "StreamingTripInfo{" +
                    "mTripId='" + mTripId + '\'' +
                    ", mUserId='" + mUserId + '\'' +
                    ", mSerialNumber='" + mSerialNumber + '\'' +
                    '}';
        }
    }

    public static class StreamingFallInfo {
        private String mTimestamp = "";
        private String mUserId = "";
        private String mSerialNumber = "";

        public StreamingFallInfo(String timestamp, String userId, String serialNumber) {
            mTimestamp = timestamp;
            mUserId = userId;
            mSerialNumber = serialNumber;
        }

        public String getTimestamp() {
            return mTimestamp;
        }

        public String getUserId() {
            return mUserId;
        }

        public String getSerialNumber() {
            return mSerialNumber;
        }
    }

    public static FileOutputStream createStreamingFos(File streamingFile) {
        Logger.d(TAG, "createStreamingFos()");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(streamingFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fos;
    }

    public static File createFileInternal(Context context, String filename, String path) {

        //Create a file in the internal storage (private to app)
        return createFile(context.getFilesDir().getAbsolutePath() + "/" + path, filename);
    }

    public static File getFileDirectoryInternal(Context context, String path) {
        //Get a file in the internal storage (private to app)
        return new File(context.getFilesDir().getAbsolutePath() + "/" + path);
    }

    public static String generateStreamingWithoutTripId(String userId, String timestamp, String serialNumber) {
        if (timestamp.equals("") || userId.equals("") || serialNumber.equals("")) {
            return null;
        }
        Logger.d(TAG, "generateStreamingWithoutTripId : " + userId + "_" + serialNumber + "_" + timestamp);
        return userId + "_" + serialNumber + "_" + timestamp;
    }

    //Crete the file with creating all directory/sub
    private static File createFile(String path, String filename) {
        File fileTemp = new File(path);
        if (!fileTemp.exists()) {
            fileTemp.mkdirs();
        }
        return new File(fileTemp, filename);
    }

    public static StreamingTripInfo retrieveUserIdTripId(String fileName) {
        String tripId = "";
        String userId = "";
        String serialNumber = "";
        String[] fileSplit = fileName.split("_");
        Logger.d(TAG, "uploadFile: file splitted: " + Arrays.deepToString(fileSplit));
        if (fileSplit.length == 3) {
            // filename: userId_serialNumber_tripId-XXX
            // Streaming file of V9
            userId = fileSplit[0];
            serialNumber = fileSplit[1];
            tripId = fileSplit[2].split("-")[0];
        } else if (fileSplit.length == 2) {
            // filename = userId_tripId-XXX
            // Streaming file of V8 - 7 - 6 - 5 - 4 - 3 - 2 - 1
            userId = fileSplit[0];
            tripId = fileSplit[1].split("-")[0];
        }
        return new StreamingTripInfo(tripId, userId, serialNumber);
    }

    public static StreamingFallInfo retrieveFallInfo(String filename) {
        String[] fileSplit = filename.split("_");
        String userId = fileSplit[0];
        String mSerialNumber = fileSplit[1];
        String timestamp = fileSplit[2];
        return new StreamingFallInfo(timestamp, userId, mSerialNumber);
    }
}