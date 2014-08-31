package com.android.cim.manager;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.android.cim.Constants;

public class RecordManager {
    private MediaRecorder mMediaRecorder;
    private boolean mRecording;
    private Context mContext;
    private static RecordManager sRecordManager;
    public static RecordManager getInstance(Context context) {
        if (sRecordManager == null) {
            sRecordManager = new RecordManager(context);
        }
        return sRecordManager;
    }
    private RecordManager(Context c) {
        mContext = c;
        mMediaRecorder = new MediaRecorder();
    }
    public synchronized void initRecorder(String fileName) {
        mRecording = false;
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File recordDir = new File(Environment.getExternalStorageDirectory() + "/" + Constants.FILE_RECORD_FOLDER);
        if (!recordDir.exists()) {
            recordDir.mkdirs();
            File noMedia = new File(recordDir + "/.nomedia");
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                logv("create .nomedia file failure");
            }
        }
        mMediaRecorder.setOutputFile(fileName);
        logv("RecordManager prepare recorder file : " + fileName);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            logd(e.getLocalizedMessage());
            stopRecorder();
        } catch (IOException e) {
            logd(e.getLocalizedMessage());
            stopRecorder();
        }
    }
    public synchronized void startRecorder() {
        if (mMediaRecorder != null) {
            logv("RecordManager startRecorder");
            mMediaRecorder.start();
            mRecording = true;
        }
    }
    
    public synchronized void stopRecorder() {
        if (mMediaRecorder != null) {
            logv("RecordManager stopRecorder");
            mMediaRecorder.stop();
            mMediaRecorder.reset();
//            mMediaRecorder.release();
            mRecording = false;
        }
    }

    public boolean recording() {
        return mRecording;
    }
    
    private void logd(String msg) {
        Log.d("taugin", msg);
    }
    private void logv(String msg) {
        Log.v("taugin", msg);
    }
    private void logw(String msg) {
        Log.w("taugin", msg);
    }

    public String getProperName(String phoneNumber, long time) {
        String fileName = "recorder_" + time + "_" + phoneNumber + ".amr";
        return Environment.getExternalStorageDirectory() + "/" + Constants.FILE_RECORD_FOLDER + "/" + fileName;
    }
}
