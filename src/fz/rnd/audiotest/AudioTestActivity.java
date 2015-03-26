package fz.rnd.audiotest;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AudioTestActivity extends Activity {

	RadioGroup streamGroup;
	TextView routeText;
	TextView modeText;
	TextView stateText;
	TextView log;
	
	//AudioTrack player;
	SoundPool player;
	MediaPlayer mp;
	AudioManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_test);
		
		streamGroup = (RadioGroup) findViewById(R.id.radioGroup_stream);
		routeText = (TextView) findViewById(R.id.text_route);
		modeText = (TextView) findViewById(R.id.text_mode);
		stateText = (TextView) findViewById(R.id.textView_state);
		log = (TextView) findViewById(R.id.textView_log);
		
		manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
		mp = new MediaPlayer();
		
		
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
	
	private void PostToLog(String s) {
		StringBuilder sb = new StringBuilder();
    	sb.append(s).append('\n').append(log.getText());
    	log.setText(sb.toString());
	}
	
	private int getStreamByRadioButton() {
		switch(streamGroup.getCheckedRadioButtonId()) {
		case R.id.radio_voicecall:
			return AudioManager.STREAM_VOICE_CALL;
		case R.id.radio_ring: 
			return AudioManager.STREAM_RING;
		case R.id.radio_default:
		default:
			return AudioManager.STREAM_MUSIC;
		}
	}
	
	private void updateAudioState() {
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("Bt A2DP: ").append(manager.isBluetoothA2dpOn() ? "ON" : "off").append("\n");
		logMessage.append("Bt SCO: ").append(manager.isBluetoothScoOn() ? "ON" : "off").append("\n");
		logMessage.append("Speaker: ").append(manager.isSpeakerphoneOn() ? "ON" : "off").append("\n");
		stateText.setText(logMessage);
	}

	private void startPlayback() {
		if(!mp.isPlaying()) {
	    	PostToLog("play clicked.");
	    	preparePlayer();
	    	mp.start();
    	}
	}
	
	private void stopPlayer() {
		if(mp.isPlaying()) {
    		PostToLog("stopping.");
    		mp.stop();
    		mp.reset();
    	}
	}
	
	private void preparePlayer() {
    	try {
    		Uri res = Uri.parse("android.resource://" + getApplicationContext().getResources().getResourceName(R.raw.audio_long16).replace(":", "/"));
			mp.setDataSource(this, res);
			mp.setAudioStreamType(getStreamByRadioButton());
			mp.setLooping(true);
			PostToLog(res.toString());
		} catch (IllegalArgumentException e) {
			PostToLog("player prepare fail. 1");
			return;
		} catch (SecurityException e) {
			PostToLog("player prepare fail. 2");
			return;
		} catch (IllegalStateException e) {
			PostToLog("player prepare fail. 3");
			return;
		} catch (IOException e) {
			PostToLog("player prepare fail. 4");
			return;
		}
    	
    	try {
			mp.prepare();
		} catch (IllegalStateException e) {
			PostToLog("player prepare fail. 5");
			return;
		} catch (IOException e) {
			PostToLog("player prepare fail. 6");
			return;
		}
    }
	 
	public void onPlayClick(View v) {	
		routeText.setText("sco:" + manager.isBluetoothScoOn() + " a2dp:" + manager.isBluetoothA2dpOn() + " sco available:" + manager.isBluetoothScoAvailableOffCall());
		startPlayback();
	}
	
	public void onNormalClick(View v) {
		manager.setMode(AudioManager.MODE_NORMAL);
		modeText.setText(R.string.mode_normal);
		updateAudioState();
		PostToLog("Setting mode to NORMAL");
	}
	
	public void onInCallClick(View v) {	
		manager.setMode(AudioManager.MODE_IN_CALL);
		modeText.setText(R.string.mode_incall);
		updateAudioState();
		PostToLog("Setting mode to IN_CALL");
	}
	
	public void onInCommClick(View v) {		
		manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		modeText.setText(R.string.mode_incomm);
		updateAudioState();
		PostToLog("Setting mode to IN_COMMUNICATION");
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
		PostToLog("starting sco");
	}
	public void onStopBtClick(View v) {
		manager.stopBluetoothSco();
		updateAudioState();
		PostToLog("stopping sco");
	}
	
	public void onBtScoClick(View v) {
		manager.setBluetoothScoOn(true);
		updateAudioState();
		PostToLog("enabling sco ");
	}
	
	public void onBtScoOffClick(View v) {
		manager.setBluetoothScoOn(false);
		updateAudioState();
		PostToLog("disabling sco ");
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
