package fz.rnd.audiotest;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

/**
 * Created by fz on 15.02.16.
 */
public class SoundPoolLoad extends IMediaLoad{
    private SoundPool mPlayer;

    private int mStreamId = -1;
    private int mSoundId = -1;

    SoundPoolLoad(Context c, LoadConfig configuration) {
        super(c, configuration);
    }

    @Override
    public synchronized void start() {
        stop();

        Log.d("AudioTestKit", "starting SoundPool on stream " + mConfig.playback_audio_stream);

        mPlayer = new SoundPool(1, mConfig.playback_audio_stream, 0);
        //mSoundId = mPlayer.mLoad(this, R.raw.pop_test_l, 1);
        mSoundId = mPlayer.load(mContext, R.raw.test_short, 1);
        mPlayer.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mStreamId = mPlayer.play(mSoundId,  1, 1, 1, -1, 1);
            }
        });
    }

    @Override
    public synchronized void stop() {
        if(mStreamId != -1) {
            mPlayer.stop(mStreamId);
            mStreamId = -1;
            mPlayer.release();
            mPlayer = null;
        }
    }
}
