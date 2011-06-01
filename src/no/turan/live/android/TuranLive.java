package no.turan.live.android;

import static no.turan.live.Constants.TAG;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TuranLive extends Activity {
	ICollectorService mCollector;
	boolean mCollectorBound;
	
	private ServiceConnection mCollectorConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "ServiceConnection.onServiceDisconnected - CollectorService");
			mCollectorBound = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "ServiceConnection.onServiceConnected -  CollectorService");
			mCollector =  (ICollectorService) service;
			mCollectorBound = true;
		}
	};
	
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		
		Intent intent = new Intent(this, CollectorService.class);
		bindService(intent, mCollectorConnection, Context.BIND_AUTO_CREATE);
		
		Log.d(TAG, "onStart - checking if CollectorService is running");
		if (mCollectorBound) {
			Button start = (Button) findViewById(R.id.start);
			if (mCollector.isRunning()) {
				start.setText(R.string.stop);
			} else {
				start.setText(R.string.start);
			}
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		Context context = this.getApplicationContext();
		
		context.stopService(new Intent(this, CollectorService.class));
		super.onDestroy();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate");

    	String antStatus = "";

        if(!this.isFinishing())
        {
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
				finish();
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
    
    public void onStartCollector(View view) {
    	Log.d(TAG, "onStartCollector");
    	Button button = (Button) view;
    	Context context = this.getApplicationContext();
    	Intent service = new Intent(this, CollectorService.class);
    	
    	if (mCollectorBound && mCollector.isRunning()) {
    		unbindService(mCollectorConnection);
    		mCollectorBound = false;
    		context.stopService(new Intent(this, CollectorService.class));
    		button.setText(R.string.start);
    	} else {
    		context.startService(service);
    		bindService(service, mCollectorConnection, Context.BIND_AUTO_CREATE);
    		button.setText(R.string.stop);
    	}
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
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (mCollectorBound) {
			unbindService(mCollectorConnection);
		}
	}
    
}