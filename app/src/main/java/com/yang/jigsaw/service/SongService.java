package com.yang.jigsaw.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by YangHaiPing on 2016/3/2.
 */
public class SongService extends Service {
    private Binder mSongBinder = new SongBinder();
    private MediaPlayer mPlayer;
    private OnSongCompleteListener mCompleteListener;
    private boolean isRelease = false;
    private TelephonyManager mTelephonyManager;

    public void setOnSongCompleteListener(OnSongCompleteListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
    }

    public interface OnSongCompleteListener {
        void onComplete();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSongBinder;
    }

    public class SongBinder extends Binder {
        public SongService getService() {
            return SongService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    }
    //初始化多媒体播放器
    public void initMediaPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mCompleteListener != null) {
                    mCompleteListener.onComplete();
                }
            }
        });
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                return false;
            }
        });
        listenPhoneState();
    }
    //监听来电事件
    private void listenPhoneState() {
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING://来电
                        if (mPlayer.isPlaying()) {
                            mPlayer.pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://接听
                        if (mPlayer.isPlaying()) {
                            mPlayer.pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE://挂断
                        if (!mPlayer.isPlaying()) {
                            mPlayer.start();
                        }
                        break;

                }
                super.onCallStateChanged(state, incomingNumber);
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }


    public boolean isPlayerInitFinish() {
        return mPlayer != null;
    }


    public boolean isPlaying() {
        if (mPlayer != null && !isRelease) {
            return mPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void reset() {
        if (mPlayer != null) {
            mPlayer.reset();
        }
    }

    public void setDataSource(String path) {
        try {
            mPlayer.setDataSource(this, Uri.fromFile(new File(path)));
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        } else {
            return -1;
        }
    }

    public int getCurrentPosition() {
        if (mPlayer != null && !isRelease && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void release() {
        if (mPlayer != null) {
            isRelease = true;
            mPlayer.release();
            mPlayer = null;
        }
    }


    public void seekTo(int progress) {
        if (mPlayer != null) {
            mPlayer.seekTo(progress);
        }
    }

    public void start() {
        if (mPlayer != null) {
            mPlayer.start();
        }
    }


    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    public void prepare() {
        if (mPlayer != null) {
            try {
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
