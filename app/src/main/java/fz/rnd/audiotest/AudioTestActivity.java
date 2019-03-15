package fz.rnd.audiotest;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AudioTestActivity extends Activity implements AudioManager.OnAudioFocusChangeListener {

	CheckBox checkBox16k;
	RadioGroup streamGroup;
	TextView routeText;
	TextView modeText;
	TextView stateText;

	AudioManager manager;
	IMediaLoad  load;
	LoadConfig config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_test);

        config = new LoadConfig();

		checkBox16k = (CheckBox) findViewById(R.id.checkBox_16k);
		streamGroup = (RadioGroup) findViewById(R.id.radioGroup_stream);
		routeText = (TextView) findViewById(R.id.text_route);
		modeText = (TextView) findViewById(R.id.text_mode);
		stateText = (TextView) findViewById(R.id.textView_state);

		manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		streamGroup.setOnCheckedChangeListener(new
			RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup radioGroup, int i) {
					config.playback_audio_stream = getStreamByRadioButton();
				}
			});

		checkBox16k.setChecked(false);
		checkBox16k.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				config.playback_sample_rate = b ? 16000 : 44100;
				config.record_sample_rate = config.playback_sample_rate;
			}
		});

		routeText.setText("wefwef:  " + manager.getStreamVolume(9));


		load = new AudioTrackRecordLoad(this, config);
		routeText.setText("sco:" + manager.isBluetoothScoOn() + " a2dp:" + manager.isBluetoothA2dpOn() + " sco a:" + manager.isBluetoothScoAvailableOffCall());
	}
	
	@Override 
	protected void onPause() {
		load.stop();
		super.onPause();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.audio_test, menu);
		return true;
	}

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_mediaplayer:
                if (load != null) {
                    load.stop();
                }
                load = new MediaPlayerLoad(this, config);
                break;
            case R.id.action_soundpool:
                if (load != null) {
                    load.stop();
                }
                load = new SoundPoolLoad(this, config);
                break;
            case R.id.action_trackrecord:
            default:
                if (load != null) {
                    load.stop();
                }
                load = new AudioTrackRecordLoad(this, config);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void onStopClick(View v) {
		load.stop();
	}
	
	private int getStreamByRadioButton() {
		switch(streamGroup.getCheckedRadioButtonId()) {
		case R.id.radio_voicecall:
			return AudioManager.STREAM_VOICE_CALL;
		case R.id.radio_music: 
			return AudioManager.STREAM_MUSIC;
        case R.id.radio_ring:
            return AudioManager.STREAM_RING;
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
		load.start();
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

    public void onModeRingClick(View v) {
        manager.setMode(AudioManager.MODE_RINGTONE);
        modeText.setText(R.string.mode_ring);
        updateAudioState();
    }
	
	public void onSpeakerClick(View v) {
		//manager.setBluetoothScoOn(false);
		manager.setSpeakerphoneOn(true);
		updateAudioState();
	}
	
	public void onSpeakerOffClick(View v) {
		manager.setSpeakerphoneOn(false);
		//manager.setBluetoothScoOn(true);
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
		
		if(manager.isSpeakerphoneOn()) {
			manager.setSpeakerphoneOn(false);
		}
		updateAudioState();
	}

    public void onAudioFocusGainClick(View v) {
	    manager.requestAudioFocus(this, config.playback_audio_stream,  AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    public void onAudioFocusLoseClick(View v) {
        manager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int i) {

    }
}
