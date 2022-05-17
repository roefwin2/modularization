package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.Nullable;

import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetGeneric;

public class SequenceGathering {
    private int mTotalSize;
    private int mCurrentSize;
    private byte[] mByteList;
    private  String mFilename;
    private int mErrorCounter;
    private int mSequenceCounter;
    private int mNbFrame;
    private int mEventId;
    private EllcieCommonCallbackGetGeneric<SequenceGathering> mCbFinished;


    public void initSequenceGathering(int totalSize, int nbFrame, int eventId){
        this.mTotalSize = totalSize;
        this.mCurrentSize = 0;
        mByteList = new byte[mTotalSize];
        mErrorCounter = 0;
        mNbFrame = nbFrame;
        mEventId = eventId;
        mSequenceCounter = 0;
    }

    public void setFilename(String filename) {
        this.mFilename = filename;
    }

    public void setEllcieCallback(EllcieCommonCallbackGetGeneric<SequenceGathering> cbFinished) {
        this.mCbFinished = cbFinished;
    }

    public EllcieCommonCallbackGetGeneric<SequenceGathering> getEllcieCallback() {
        return mCbFinished;
    }

    public void addData(byte[] datas) {
        int length = datas.length;
        if ((length + mCurrentSize) > mTotalSize) {
            length = mTotalSize - mCurrentSize;
        }

        if (length >= 0){
            System.arraycopy(datas, 0, mByteList, mCurrentSize, length);
        }

        mCurrentSize += length;
    }

    public void incrementErrorCounter(){
        mErrorCounter ++;
    }

    public int getErrorCounter() {
        return mErrorCounter;
    }

    public void resetErrorCounter(){
        mErrorCounter = 0;
    }

    public int getSequenceCounter() {
        return mSequenceCounter;
    }

    public int incrementSequenceCounter(){
        return ++mSequenceCounter;
    }

    public String getFilename() {
        return mFilename;
    }

    public int getNbFrame() {
        return mNbFrame;
    }

    public void done(){
        if (mCbFinished != null) {
            mCbFinished.done(this);
        }
    }

    public byte[] getByteList() {
        return mByteList;
    }

    public int getEventId() {
        return mEventId;
    }

    public boolean isValidSize(){
        return mCurrentSize == mTotalSize;
    }
}

