package fz.rnd.audiotest;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

class MediaPlayerLoad extends IMediaLoad
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener{

    private MediaPlayer mPlayer;
    private boolean mStopped = false;

    MediaPlayerLoad(Context c, LoadConfig configuration) {
        super(c, configuration);
    }

    @Override
    public synchronized void start() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return; //already started
        }

        stop();

        mStopped = false;
        Log.d("AudioTestKit", "starting SoundPool on stream " + mConfig.playback_audio_stream);

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);

        mPlayer.setLooping(true);
        mPlayer.setAudioStreamType(mConfig.playback_audio_stream);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(mConfig.playback_audio_stream)
                        .build());
        }

        try {
            mPlayer.setDataSource(mContext, Uri.parse(
                    ContentResolver.SCHEME_ANDROID_RESOURCE+"://" + mContext.getPackageName() + "/" + R.raw.pop_test_l));
        } catch (IOException e) {
            Log.e("MediaPlayerLoad", e.toString());
        }
        mPlayer.prepareAsync();
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mStopped) {
            //already cleaned mPlayer
            return;
        }
        if (mPlayer != null) {
            mPlayer.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }


    @Override
    public synchronized void stop() {
        mStopped = true;
        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    void pause() {
        if(mPlayer != null) {
            mPlayer.pause();
        }
    }
}
