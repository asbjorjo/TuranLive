package no.turan.live.android;

import static no.turan.live.Constants.TAG;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wahoofitness.api.WFHardwareConnector;

public class TuranLive extends Activity {

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		Context context = this.getApplicationContext();
		
		context.stopService(new Intent(this, AntService.class));
		super.onDestroy();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate");

    	Context context = this.getApplicationContext();
    	String antStatus = "";
    	
    	// check for ANT hardware support.
    	if (WFHardwareConnector.hasAntSupport(context)) {
        	context.startService(new Intent(this, AntService.class));
    	}
        else {
        	// ANT hardware not supported.
        	antStatus = "ANT not supported.";
        }

        if(!this.isFinishing())
        {
            setContentView(R.layout.main);
            ((TextView)findViewById(R.id.antStatus)).setText(antStatus);
        }
    }
    
    public void onExit(View view) {
    	Log.d(TAG, "onExit enter");
		  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		  
		builder.setMessage(this.getResources().getString(R.string.exit_verify));
		builder.setCancelable(false);

		builder.setPositiveButton(this.getResources().getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.i(TAG, "exitApplication: Exit");
				finish();
			}
		});

		builder.setNegativeButton(this.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.i(TAG, "exitApplication: Cancelled");
				dialog.cancel();
			}
		});

		AlertDialog exitDialog = builder.create();
		exitDialog.show();
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
		case R.id.sensor_setup:
			Log.d(TAG, "Starting Sensor Setup");
			Intent intent = new Intent(this, SensorSetup.class);
			startActivity(intent);
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
    
}