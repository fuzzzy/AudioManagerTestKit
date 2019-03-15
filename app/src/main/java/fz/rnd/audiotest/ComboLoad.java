package fz.rnd.audiotest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;

class ComboLoad extends IMediaLoad {

    private AudioTrackRecordLoad mTrackRecordLoad;
    private MediaPlayerLoad mMediaPlayerLoad;

    ComboLoad(Context c, LoadConfig configuration) {
        super(c, configuration);
    }

    @Override
    public synchronized void start() {
        LoadConfig trackRecordConfig = new LoadConfig();
        trackRecordConfig.playback_audio_stream = AudioManager.STREAM_VOICE_CALL;
        trackRecordConfig.record_audio_source = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        trackRecordConfig.use_recording = true;
        mTrackRecordLoad = new AudioTrackRecordLoad(mContext, trackRecordConfig);
        mMediaPlayerLoad = new MediaPlayerLoad(mContext, mConfig);

        mTrackRecordLoad.start();
        mMediaPlayerLoad.start();
    }


    @Override
    public synchronized void stop() {
        mMediaPlayerLoad.start();
        mTrackRecordLoad.stop();
    }
}
