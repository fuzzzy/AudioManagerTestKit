package fz.rnd.audiotest;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

/**
 * Created by fz on 16.02.16.
 */
public class AudioTrackRecordLoad extends IMediaLoad {

    private final Object mPlayerLock = new Object();
    private AudioTrack mPlayer;

    private short[] mPlaybackBuffer;
    private int mPlaybackDataLen = 0;
    private double mT = 0;
    private double mDt = 1;
    private int mPlayBufferSize = 0;

    private short[] mRecordBuffer;
    private int mRecBufferSize = 0;
    private AudioRecord mRecorder;
    private int mDelayInSamples = 0;

    AudioTrackRecordLoad(Context c, LoadConfig configuration) {
        super(c, configuration);
    }

    @Override
    void start() {
        initBuffers();
        startPayback();
        if (mConfig.use_recording) {
            startRecord();
        }
    }

    private void startPayback() {
        synchronized (mPlayerLock) {
            if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ) {
                mPlayer = createTrackPost21Api();
            }
            else {
                mPlayer = createTrackPre21Api();
            }

            Thread playbackThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean _continue = true;

                    while (_continue) {
                        synchronized (mPlayerLock) {
                            // Log.d("mLoad");
                            if (mPlayer == null) {
                                break;
                            }
                            processPlayBuffer();
                            mPlayer.write(mPlaybackBuffer, 0, mPlaybackDataLen);
                            _continue = mPlayer.getPlayState() != AudioTrack.PLAYSTATE_STOPPED;
                        }
                    }
                }
            });

            mPlayer.play();
            playbackThread.start();
        }
    }

    private AudioTrack createTrackPre21Api() {
         return new AudioTrack(mConfig.playback_audio_stream,
                            mConfig.playback_sample_rate,
                            mConfig.playback_format,
                            AudioFormat.ENCODING_PCM_16BIT,
                 mPlayBufferSize,
                            AudioTrack.MODE_STREAM);
    }

    /*
    //voicecall 0
    mContentType = 1 CONTENT_TYPE_SPEECH
    mFlags = 0
    mSource = -1
    mTags = {java.util.HashSet@4419}  size = 0
    mUsage = 2 USAGE_VOICE_COMMUNICATION

    //default -2147483648
    mContentType = 0 CONTENT_TYPE_UNKNOWN
    mFlags = 0
    mSource = -1
    mTags = {java.util.HashSet@4451}  size = 0
    mUsage = 0 USAGE_UNKNOWN

    //music
    mContentType = 2 CONTENT_TYPE_MUSIC
    mFlags = 0
    mSource = -1
    mTags = {java.util.HashSet@4429}  size = 0
    mUsage = 1 USAGE_MEDIA
     */

    @TargetApi(21)
    private AudioTrack createTrackPost21Api() {
        AudioAttributes.Builder aab = new AudioAttributes.Builder();
                                //.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                //.setUsage(AudioAttributes.USAGE_MEDIA)
                                aab.setLegacyStreamType(mConfig.playback_audio_stream);
        AudioAttributes at = aab.build();

        AudioFormat af = new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(mConfig.playback_sample_rate)
                            .setChannelMask(mConfig.playback_format)
                            .build();

        return new AudioTrack(at, af, mPlayBufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
    }

    private void processPlayBuffer() {
        for(int i = 0; i < mPlaybackBuffer.length; i++) {
            double TONE_FREQUENCY = 440;
            double INITIAL_PHASE = 0;
            double GAIN = 0.3;

            mPlaybackBuffer[i] = (short) Math.round(Short.MAX_VALUE * (GAIN * Math.sin(INITIAL_PHASE + 2*Math.PI*
                    TONE_FREQUENCY *(mT))));
            if(i >= mDelayInSamples && i < mRecordBuffer.length - mDelayInSamples) {
                double FEEDBACK = 0.7;
                mPlaybackBuffer[i] += FEEDBACK * mRecordBuffer[i - mDelayInSamples];
            }
            mT += mDt;
        }

        if(mT > 2*Math.PI  ) {
            mT -= 2*Math.PI;
        }

        mPlaybackDataLen = mPlaybackBuffer.length;
    }

    private void startRecord() {
        mRecorder = new AudioRecord(mConfig.record_audio_source,
                                    mConfig.record_sample_rate,
                                    AudioFormat.CHANNEL_IN_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                mRecBufferSize);

        Thread recorderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    mRecorder.read(mRecordBuffer, 0, mRecBufferSize);
                }
            }
        });

        mRecorder.startRecording();
        recorderThread.start();
    }

    private void initBuffers() {
        mPlayBufferSize = AudioTrack.getMinBufferSize(mConfig.playback_sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        mDt = 1.0 / mConfig.playback_sample_rate;
        mPlaybackBuffer = new short[mPlayBufferSize];
        mT = 0;

        //in seconds
        double DELAY = 0.01;
        mDelayInSamples = (int)Math.round(mConfig.record_sample_rate * DELAY);
        mRecBufferSize = AudioRecord.getMinBufferSize(mConfig.record_sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        mRecordBuffer = new short[mRecBufferSize];
    }

    @Override
    void stop() {
        stopPlayback();
        if (mConfig.use_recording){
            stopRecord();
        }
    }

    private void stopPlayback() {
        synchronized (mPlayerLock) {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer = null;
            }
        }
    }

    private void stopRecord() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder = null;
        }
    }


}
