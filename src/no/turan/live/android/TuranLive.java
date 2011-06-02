package no.turan.live.android;

import static no.turan.live.android.Constants.SAMPLE_CADENCE_KEY;
import static no.turan.live.android.Constants.SAMPLE_HR_KEY;
import static no.turan.live.android.Constants.SAMPLE_POWER_KEY;
import static no.turan.live.android.Constants.SAMPLE_SPEED_KEY;
import static no.turan.live.android.Constants.SETTINGS_NAME;
import static no.turan.live.android.Constants.TAG;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TuranLive extends Activity {
	ICollectorService mCollector;
	boolean mCollectorBound;
	
	private ServiceConnection mCollectorConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "ServiceConnection.onServiceDisconnected - CollectorService");
			mCollectorBound = false;
			updateMain();
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "ServiceConnection.onServiceConnected -  CollectorService");
			mCollector =  (ICollectorService) service;
			mCollectorBound = true;
			updateMain();
		}
	};
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, intent.getAction());
			if (intent.getAction().equals("no.turan.live.android.COLLECTOR_STARTED")) {
				updateMain();
			} else if (intent.getAction().equals("no.turan.live.android.COLLECTOR_STOPPED")) {
				updateDisplay(new Bundle());
				updateMain();
			} else if (intent.getAction().equals("no.turan.live.android.SAMPLE")) {
				Bundle sample = new Bundle();
				sample.putAll(intent.getExtras());
				updateDisplay(sample);
			} else if (intent.getAction().equals("no.turan.live.android.ANT_STATE")) {
				TextView antStatus = (TextView) findViewById(R.id.antStatus);
				antStatus.setText(intent.getStringExtra("no.turan.live.android.ANT_STATE_KEY"));
			}
		}
	};
	
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();

		Intent intent = new Intent(this, CollectorService.class);
		bindService(intent, mCollectorConnection, Context.BIND_AUTO_CREATE);
	}

	protected void updateDisplay(Bundle values) {
		Log.d(TAG, "updateDisplay");
		//SharedPreferences preferences = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Log.d(TAG, preferences.toString());
		boolean hrOn = preferences.getBoolean("hr_enable", false);
		boolean speedOn = preferences.getBoolean("speed_enable", false);
		boolean cadenceOn = preferences.getBoolean("cadence_enable", false);
		boolean powerOn = preferences.getBoolean("power_enable", false);
		
		int hr = values.getInt(SAMPLE_HR_KEY, -1);
		int speed = values.getInt(SAMPLE_SPEED_KEY, -1);
		int cadence = values.getInt(SAMPLE_CADENCE_KEY, -1);
		int power = values.getInt(SAMPLE_POWER_KEY, -1);
		
		TextView textView;
		textView = (TextView) findViewById(R.id.displayHR);
		if (hrOn && hr>=0) {
			textView.setText(Integer.toString(hr));
		} else  {
			textView.setText("HR");
		}
		textView = (TextView) findViewById(R.id.displaySpeed);
		if (speedOn && speed>=0) {
			textView.setText(Integer.toString(speed));
		} else {
			textView.setText("Speed");
		}
		textView = (TextView) findViewById(R.id.displayCadence);
		if (cadenceOn && cadence >= 0) {
			textView.setText(Integer.toString(cadence));
		} else {
			textView.setText("Cadence");
		}
		textView = (TextView) findViewById(R.id.displayPower);
		if (powerOn && power >= 0) {
			textView.setText(Integer.toString(power));
		} else {
			textView.setText("Power");
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        //SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);

    	String antStatus = "";

        if(!this.isFinishing())
        {
        	IntentFilter filter = new IntentFilter("no.turan.live.android.COLLECTOR_STARTED");
        	filter.addAction("no.turan.live.android.SAMPLE");
        	filter.addAction("no.turan.live.android.ANT_STATE");
        	filter.addAction("no.turan.live.android.COLLECTOR_STOPPED");
        	registerReceiver(broadcastReceiver, filter);
            setContentView(R.layout.main);
            ((TextView)findViewById(R.id.antStatus)).setText(antStatus);
        }
    }
    
    public void onExit(View view) {
    	Log.d(TAG, "onExit");
		  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		  
		builder.setMessage(this.getResources().getString(R.string.exit_verify));
		builder.setCancelable(false);

		builder.setPositiveButton(this.getResources().getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.i(TAG, "exitApplication - exit");
				exitTuran();
			}
		});

		builder.setNegativeButton(this.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.i(TAG, "exitApplication - cancel");
				dialog.cancel();
			}
		});

		AlertDialog exitDialog = builder.create();
		exitDialog.show();
    }
    
    private void updateMain() {
    	Log.d(TAG, "updateMain");
    	Button start = (Button) findViewById(R.id.antCollect);
    	Button live  = (Button) findViewById(R.id.goLive);
    	EditText exercise = (EditText) findViewById(R.id.exerciseIdBox);
    	
    	if (mCollectorBound && mCollector.isCollecting()) {
			start.setText(R.string.stop);

			if (mCollector.isLive()) {
				exercise.setInputType(InputType.TYPE_NULL);
				exercise.setEnabled(false);
				live.setText(R.string.go_off);
			} else {
				exercise.setInputType(InputType.TYPE_CLASS_NUMBER);
				exercise.setEnabled(true);
				live.setText(R.string.go_live);
			}
			live.setEnabled(true);
    	} else {
    		start.setText(R.string.start);
    		live.setText(R.string.go_live);
    		live.setEnabled(false);
    		exercise.setInputType(InputType.TYPE_CLASS_NUMBER);
			exercise.setEnabled(true);
    	}
    }
    
    private void exitTuran() {
    	Intent service = new Intent(this, CollectorService.class);
    	stopService(service);
    	finish();
	}

	public void onStartCollector(View view) {
    	Log.d(TAG, "onStartCollector");
    	Intent service = new Intent(this, CollectorService.class);
    	
    	if (mCollectorBound && mCollector.isCollecting()) {
    		mCollector.goOff();
    		unbindService(mCollectorConnection);
    		mCollectorBound = false;
    		stopService(new Intent(this, CollectorService.class));
    		bindService(service, mCollectorConnection, BIND_AUTO_CREATE);
        } else {
        	startService(service);
    		bindService(service, mCollectorConnection, Context.BIND_AUTO_CREATE);
    	}
    }
    
    public void onGoLive(View view) {
    	Log.d(TAG, "onGoLive");
    	
    	if (mCollector.isLive()) {
    		mCollector.goOff();
    	} else {
    		EditText exerciseIdBox = (EditText) findViewById(R.id.exerciseIdBox);
        	int exerciseId = Integer.parseInt(exerciseIdBox.getText().toString());
        	mCollector.goLive(exerciseId);
    	}
    	updateMain();
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_options, menu);
		return true;
	}

    /* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case R.id.preference_menu:
			startActivity(new Intent(this, TuranPreferences.class));
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		if (mCollectorBound) {
			unbindService(mCollectorConnection);
			mCollectorBound = false;
		}
		
		//SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
		//SharedPreferences.Editor editor = settings.edit();
		//editor.commit();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

		updateMain();
	}

	@Override
	protected void onPostResume() {
		Log.d(TAG, "onPostResume");
		super.onPostResume();
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		super.onBackPressed();
	}    
}