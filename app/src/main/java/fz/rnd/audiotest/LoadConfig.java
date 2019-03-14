package fz.rnd.audiotest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 * Created by fz on 15.02.16.
 */

public class LoadConfig {
    int playback_audio_stream = AudioManager.STREAM_MUSIC; //AudioManager.STREAM_MUSIC,
    int playback_sample_rate = 44100;//        44100,
    int playback_format = AudioFormat.CHANNEL_OUT_MONO;// AudioFormat.CHANNEL_OUT_MONO
    int record_audio_source = MediaRecorder.AudioSource.MIC;
    int record_sample_rate = 44100;//        44100,
    boolean use_recording = false;
}