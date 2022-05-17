package com.ellcie_healthy.ble_library.ble.streaming;


import android.content.Context;

import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StreamingOpticianStrategy {
    private static final String STREAMING_OPTICIAN_PATH = "optician";
    private static final String TAG = "StreamingOpticianStrategy";
    private final Context mContext;
    private FileOutputStream mStreamingFos;
    private File mStreamingFile;
    private String mTimestamp = "";
    private String mUserId = "";
    private String mSerialNumber = "";

    public StreamingOpticianStrategy(@NotNull Context context, String userId, String serialNumber, String timestamp) {
        mTimestamp = timestamp;
        mUserId = userId;
        mSerialNumber = serialNumber;
        mContext = context;
    }

    public void initStreamingFile() {
        //create trip file with the related fos

//        String filename = StreamingUtils.generateStreamingWithoutTripId(mUserId, mTimestamp, mSerialNumber);
//        if (filename == null || filename.equals("")) {
//            return;
//        }
//        mStreamingFile = StreamingUtils.createFileInternal(mContext, filename, STREAMING_OPTICIAN_PATH);
//        mStreamingFos = StreamingUtils.createStreamingFos(mStreamingFile);
    }

    public void writeStreamingData(byte[] data) {
        if (mStreamingFos != null) {
            try {
                mStreamingFos.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadStreamingOpticianFile(File file) {
        //TODO this code miust be in application part, not in the lib
        /*Logger.d(TAG, "uploadStreamingOpticianFile()");
        if (file == null || (!file.exists())) {
            Logger.e(LogEnum.SEZ003, TAG);
            return;
        }
        String fileName = file.getName();
        Logger.d(TAG, "optician file name " + fileName);
        String userId = "";
        String serialNumber = "";
        String[] fileSplit = fileName.split("_");
        Logger.d(TAG, "uploadStreamingOpticianFile: file splitted: " + Arrays.deepToString(fileSplit));
        if (fileSplit.length == 3) {
            // filename: userId_serialNumber_timestamp
            userId = fileSplit[0];
            serialNumber = fileSplit[1];
            //upload file to firebase
            IFirebaseDb firebaseDb = FirebaseDataHelper.getInstance(mContext);
            firebaseDb.uploadStreamingOpticianFile(file.getAbsolutePath(), userId, serialNumber);
        }*/

    }

    public void checkFilesPresence() {
        //Function to check if there is a optician files in a specific directory
        Logger.d(TAG, "checkOpticianfilesPresence()");
        File directory = new File(mContext.getFilesDir().getAbsolutePath() + "/" + STREAMING_OPTICIAN_PATH);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            Logger.d(TAG, "Files present in Directory path :" + directory.getAbsolutePath());
            for (int i = 0; i < files.length; i++) {
                Logger.d(TAG, "FileName :" + files[i].getName());
                //Sending the optician file to firebase !
                uploadStreamingOpticianFile(files[i]);
            }
        } else {
            Logger.e(TAG, "Directory : " + mContext.getFilesDir().getAbsolutePath() + "/" + STREAMING_OPTICIAN_PATH + " does not exist");
        }
    }

    public void onStreamingStopped() {
        //Close streaming file
        Logger.d(TAG, "closeSensorDataFos()");
        if (mStreamingFos != null) {
            Logger.d(TAG, "closeSensorDataFos != null");
            try {
                //Closing the stream will flush the file
                mStreamingFos.close();
                mStreamingFos = null;
            } catch (IOException e) {
                e.printStackTrace();
                mStreamingFos = null;
            }
            //upload to firebase
            uploadStreamingOpticianFile(mStreamingFile);
        }
    }
}
