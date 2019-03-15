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

	CheckBox mCheckBox16K;
	RadioGroup mStreamGroup;
	TextView mRouteText;
	TextView mModeText;
	TextView mStateText;

	AudioManager mManager;
	IMediaLoad mLoad;
	LoadConfig mConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_test);

        mConfig = new LoadConfig();

		mCheckBox16K = (CheckBox) findViewById(R.id.checkBox_16k);
		mStreamGroup = (RadioGroup) findViewById(R.id.radioGroup_stream);
		mRouteText = (TextView) findViewById(R.id.text_route);
		mModeText = (TextView) findViewById(R.id.text_mode);
		mStateText = (TextView) findViewById(R.id.textView_state);

		mManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mStreamGroup.setOnCheckedChangeListener(new
			RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup radioGroup, int i) {
					mConfig.playback_audio_stream = getStreamByRadioButton();
				}
			});

		mCheckBox16K.setChecked(false);
		mCheckBox16K.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mConfig.playback_sample_rate = b ? 16000 : 44100;
				mConfig.record_sample_rate = mConfig.playback_sample_rate;
			}
		});

		mRouteText.setText("wefwef:  " + mManager.getStreamVolume(9));


		mLoad = new AudioTrackRecordLoad(this, mConfig);
		mRouteText.setText("sco:" + mManager.isBluetoothScoOn() + " a2dp:" + mManager.isBluetoothA2dpOn() + " sco a:" + mManager
                .isBluetoothScoAvailableOffCall());
	}
	
	@Override 
	protected void onPause() {
		mLoad.stop();
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
                if (mLoad != null) {
                    mLoad.stop();
                }
                mLoad = new MediaPlayerLoad(this, mConfig);
                break;
            case R.id.action_soundpool:
                if (mLoad != null) {
                    mLoad.stop();
                }
                mLoad = new SoundPoolLoad(this, mConfig);
                break;
            case R.id.action_trackrecord:
            default:
                if (mLoad != null) {
                    mLoad.stop();
                }
                mLoad = new AudioTrackRecordLoad(this, mConfig);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void onStopClick(View v) {
		mLoad.stop();
	}
	
	private int getStreamByRadioButton() {
		switch(mStreamGroup.getCheckedRadioButtonId()) {
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
		logMessage.append("Bt A2DP: ").append(mManager.isBluetoothA2dpOn() ? "ON" : "off").append("\n");
		logMessage.append("Bt SCO: ").append(mManager.isBluetoothScoOn() ? "ON" : "off").append("\n");
		logMessage.append("Speaker: ").append(mManager.isSpeakerphoneOn() ? "ON" : "off").append("\n");
		mStateText.setText(logMessage);
	}
	 
	public void onPlayClick(View v) {	
		mRouteText.setText("sco:" + mManager.isBluetoothScoOn() + " a2dp:" + mManager.isBluetoothA2dpOn() + " sco available:" + mManager
                .isBluetoothScoAvailableOffCall());
		mLoad.start();
	}
	
	public void onNormalClick(View v) {
		mManager.setMode(AudioManager.MODE_NORMAL);
		mModeText.setText(R.string.mode_normal);
		updateAudioState();
	}
	
	public void onInCallClick(View v) {	
		mManager.setMode(AudioManager.MODE_IN_CALL);
		mModeText.setText(R.string.mode_incall);
		updateAudioState();
	}
	
	public void onInCommClick(View v) {		
		mManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		mModeText.setText(R.string.mode_incomm);
		updateAudioState();
	}

    public void onModeRingClick(View v) {
        mManager.setMode(AudioManager.MODE_RINGTONE);
        mModeText.setText(R.string.mode_ring);
        updateAudioState();
    }
	
	public void onSpeakerClick(View v) {
		//mManager.setBluetoothScoOn(false);
		mManager.setSpeakerphoneOn(true);
		updateAudioState();
	}
	
	public void onSpeakerOffClick(View v) {
		mManager.setSpeakerphoneOn(false);
		//mManager.setBluetoothScoOn(true);
		updateAudioState();
	}
	
	public void onStartBtClick(View v) {
		mManager.startBluetoothSco();
		updateAudioState();
	}
	public void onStopBtClick(View v) {
		mManager.stopBluetoothSco();
		updateAudioState();
	}
	
	public void onBtScoClick(View v) {
		mManager.setBluetoothScoOn(true);
		updateAudioState();
	}
	public void onBtScoOffClick(View v) {
		mManager.setBluetoothScoOn(false);
		updateAudioState();
	}
	
	public void onEarpieceClick(View v) {	
		if(mManager.isBluetoothScoOn()) {
			mManager.setBluetoothScoOn(false);
		}
		
		if(mManager.isSpeakerphoneOn()) {
			mManager.setSpeakerphoneOn(false);
		}
		updateAudioState();
	}

    public void onAudioFocusGainClick(View v) {
	    mManager.requestAudioFocus(this, mConfig.playback_audio_stream,  AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    public void onAudioFocusLoseClick(View v) {
        mManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int i) {

    }
}
