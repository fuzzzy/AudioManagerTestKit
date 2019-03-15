package fz.rnd.audiotest;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

/**
 * Created by fz on 15.02.16.
 */
public class SoundPoolLoad extends IMediaLoad{
    SoundPool player;
    int streamId = -1;

    int soundId = -1;

    SoundPoolLoad(Context c, LoadConfig configuration) {
        super(c, configuration);
    }

    @Override
    public synchronized void start() {
        stop();

        Log.d("AudioTestKit", "starting SoundPool on stream " + mConfig.playback_audio_stream);

        player = new SoundPool(1, mConfig.playback_audio_stream, 0);
        //soundId = mPlayer.load(this, R.raw.pop_test_l, 1);
        soundId = player.load(mContext, R.raw.test_short, 1);
        player.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                streamId = player.play(soundId,  1, 1, 1, -1, 1);
            }
        });
    }

    @Override
    public synchronized void stop() {
        if(streamId != -1) {
            player.stop(streamId);
            streamId = -1;
            player.release();
            player = null;
        }
    }
}
