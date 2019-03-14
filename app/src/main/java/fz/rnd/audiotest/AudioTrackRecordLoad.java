package fz.rnd.audiotest;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by fz on 16.02.16.
 */
public class AudioTrackRecordLoad extends IMediaLoad {

    final Object playerLock = new Object();
    AudioTrack player;

    short[] playbackBuffer;
    int playbackDataLen = 0;
    double t = 0;
    double dt = 1;
    int playBufferSize = 0;
    Thread playbackThread;

    short[] recordBuffer;
    int recBufferSize = 0;
    AudioRecord recorder;
    Thread recorderThread;
    int delayInSamples = 0;


    final double TONE_FREQUENCY = 440;
    final double INITIAL_PHASE = 0;
    final double GAIN = 0.3;

    final double DELAY = 0.01; //in seconds
    final double FEEDBACK = 0.7;

    AudioTrackRecordLoad(Context c, LoadConfig configuration) {
        super(c, configuration);
    }

    @Override
    void start() {
        initBuffers();
        startPayback();
        if (_cfg.use_recording) {
            startRecord();
        }
    }

    private void startPayback() {
        synchronized (playerLock) {
            if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ) {
                player = createTrackPost21Api();
            }
            else {
                player = createTrackPre21Api();
            }

            playbackThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean _continue = true;

                    while(_continue) {
                        synchronized (playerLock) {
                           // Log.d("load");
                            if (player == null) {
                                break;
                            }
                            processPlayBuffer();
                            player.write(playbackBuffer, 0, playbackDataLen);
                            _continue = player.getPlayState() != AudioTrack.PLAYSTATE_STOPPED;
                        }
                    }
                }
            });

            player.play();
            playbackThread.start();
        }
    }

    private AudioTrack createTrackPre21Api() {
         return new AudioTrack(_cfg.playback_audio_stream,
                            _cfg.playback_sample_rate,
                            _cfg.playback_format,
                            AudioFormat.ENCODING_PCM_16BIT,
                            playBufferSize,
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
                                aab.setLegacyStreamType(_cfg.playback_audio_stream);
        AudioAttributes at = aab.build();

        AudioFormat af = new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(_cfg.playback_sample_rate)
                            .setChannelMask(_cfg.playback_format)
                            .build();

        return new AudioTrack(at, af, playBufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
    }

    private void processPlayBuffer() {
        for(int i = 0; i < playbackBuffer.length; i++) {
            playbackBuffer[i] = (short) Math.round(Short.MAX_VALUE * (GAIN * Math.sin(INITIAL_PHASE + 2*Math.PI*TONE_FREQUENCY*(t))));
            if(i >= delayInSamples && i < recordBuffer.length - delayInSamples) {
                playbackBuffer[i] += FEEDBACK * recordBuffer[i - delayInSamples];
            }
            t += dt;
        }

        if(t > 2*Math.PI  ) {
            t -= 2*Math.PI;
        }

        playbackDataLen = playbackBuffer.length;
    }

    private void startRecord() {
        recorder = new AudioRecord(_cfg.record_audio_source,
                                    _cfg.record_sample_rate,
                                    AudioFormat.CHANNEL_IN_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    recBufferSize);

        recorderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    synchronized (recordBuffer) {
                        recorder.read(recordBuffer, 0, recBufferSize);
                    }
                }
            }
        });

        recorder.startRecording();
        recorderThread.start();
    }

    private void initBuffers() {
        playBufferSize = AudioTrack.getMinBufferSize(_cfg.playback_sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        dt = 1.0 / _cfg.playback_sample_rate;
        playbackBuffer = new short[playBufferSize];
        t = 0;

        delayInSamples = (int)Math.round(_cfg.record_sample_rate * DELAY);
        recBufferSize = AudioRecord.getMinBufferSize(_cfg.record_sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        recordBuffer = new short[recBufferSize];
    }

    @Override
    void stop() {
        stopPlayback();
        if (_cfg.use_recording){
            stopRecord();
        }
    }

    private void stopPlayback() {
        synchronized (playerLock) {
            if (player != null) {
                player.stop();
                player = null;
            }
        }
    }

    private void stopRecord() {
        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
    }


}
