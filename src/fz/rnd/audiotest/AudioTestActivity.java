package fz.rnd.audiotest;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AudioTestActivity extends Activity {

	CheckBox checkBox16k;
	RadioGroup streamGroup;
	TextView routeText;
	TextView modeText;
	TextView stateText;
	
	//AudioTrack player;
	SoundPool player;
	AudioManager manager;
	
	int soundId = -1;
	int streamId = -1;
    
	@Override
	public boolean dispatchKeyEvent(KeyEvent evt) {
    	Log.d("lolca", " Key: " + evt.toString());
		
		return super.dispatchKeyEvent(evt); 
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d("lolca", keyCode + " Key 2: " + event.toString());
		return super.onKeyUp(keyCode, event);
	};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_test);
		
		checkBox16k = (CheckBox) findViewById(R.id.checkBox_16k);
		streamGroup = (RadioGroup) findViewById(R.id.radioGroup_stream);
		routeText = (TextView) findViewById(R.id.text_route);
		modeText = (TextView) findViewById(R.id.text_mode);
		stateText = (TextView) findViewById(R.id.textView_state);
		//player = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 1024 * 1024, AudioTrack.MODE_STATIC );
		
		manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
		//player.write(audioData, offsetInShorts, sizeInShorts)
		//player.play();
		//player = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		
		routeText.setText("sco:" + manager.isBluetoothScoOn() + " a2dp:" + manager.isBluetoothA2dpOn() + " sco a:" + manager.isBluetoothScoAvailableOffCall());
	}
	
	@Override 
	protected void onPause() {
		stopPlayer();
		super.onPause();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.audio_test, menu);
		return true;
	}
	
	public void onStopClick(View v) {
		stopPlayer();
	}

	private void stopPlayer() {
		if(streamId != -1) {
			player.stop(streamId);
			streamId = -1;
			player.release();
			player = null;
		}
	}
	
	private int getStreamByRadioButton() {
		switch(streamGroup.getCheckedRadioButtonId()) {
		case R.id.radio_voicecall:
			return AudioManager.STREAM_VOICE_CALL;
		case R.id.radio_music: 
			return AudioManager.STREAM_MUSIC;
		case R.id.radio_default:
		default:
			return AudioManager.USE_DEFAULT_STREAM_TYPE;
		}
	}
	
	private void updateAudioState() {
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("Bt A2DP: ").append(manager.isBluetoothA2dpOn() ? "ON" : "off").append("\n");
		logMessage.append("Bt SCO: ").append(manager.isBluetoothScoOn() ? "ON" : "off").append("\n");
		logMessage.append("Speaker: ").append(manager.isSpeakerphoneOn() ? "ON" : "off").append("\n");
		stateText.setText(logMessage);
	}
	 
	public void onPlayClick(View v) {	
		routeText.setText("sco:" + manager.isBluetoothScoOn() + " a2dp:" + manager.isBluetoothA2dpOn() + " sco available:" + manager.isBluetoothScoAvailableOffCall());
		
		stopPlayer();
		
		player = new SoundPool(1, getStreamByRadioButton(), 0);
		//soundId = player.load(this, R.raw.pop_test_l, 1);
		soundId = player.load(this, R.raw.test_short, 1);
		player.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				streamId = player.play(soundId,  1, 1, 1, -1, 1);
			}
		});
		
	}
	
	public void onNormalClick(View v) {
		manager.setMode(AudioManager.MODE_NORMAL);
		modeText.setText(R.string.mode_normal);
		updateAudioState();
	}
	
	public void onInCallClick(View v) {	
		manager.setMode(AudioManager.MODE_IN_CALL);
		modeText.setText(R.string.mode_incall);
		updateAudioState();
	}
	
	public void onInCommClick(View v) {		
		manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		modeText.setText(R.string.mode_incomm);
		updateAudioState();
	}
	
	public void onSpeakerClick(View v) {
		manager.setBluetoothScoOn(false);
		manager.setSpeakerphoneOn(true);
		updateAudioState();
	}
	
	public void onSpeakerOffClick(View v) {
		manager.setSpeakerphoneOn(false);
		manager.setBluetoothScoOn(true);
		updateAudioState();
	}
	
	public void onStartBtClick(View v) {
		manager.startBluetoothSco();
		updateAudioState();
	}
	public void onStopBtClick(View v) {
		manager.stopBluetoothSco();
		updateAudioState();
	}
	
	public void onBtScoClick(View v) {
		manager.setBluetoothScoOn(true);
		updateAudioState();
	}
	public void onBtScoOffClick(View v) {
		manager.setBluetoothScoOn(false);
		updateAudioState();
	}
	
	public void onEarpieceClick(View v) {	
		if(manager.isBluetoothScoOn()) {
			manager.setBluetoothScoOn(false);
		}
		
		if( manager.isSpeakerphoneOn()) {
			manager.setSpeakerphoneOn(false);
		}
		updateAudioState();
	}

}
