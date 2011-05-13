package no.turan.live.android;

import static no.turan.live.Constants.TAG;

import com.wahoofitness.api.WFAntException;
import com.wahoofitness.api.WFAntNotSupportedException;
import com.wahoofitness.api.WFAntServiceNotInstalledException;
import com.wahoofitness.api.WFDisplaySettings;
import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.WFHardwareConnectorTypes.WFAntError;
import com.wahoofitness.api.WFHardwareConnectorTypes.WFHardwareState;
import com.wahoofitness.api.comm.WFSensorConnection;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TuranLive extends Activity implements WFHardwareConnector.Callback {
	private WFHardwareConnector mHardwareConnector;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate");

    	Context context = this.getApplicationContext();
    	String statusText = "";
    	
    	// check for ANT hardware support.
        if (WFHardwareConnector.hasAntSupport(context)) {
	        	
	        try {
	        	boolean bResumed = false;
	        	
	        	// attempt to retrieve the previously suspended WFHardwareConnector instance.
	        	//
	        	// see the onRetainNonConfigurationInstance method.
	        	mHardwareConnector = (WFHardwareConnector) getLastNonConfigurationInstance();
	        	if (mHardwareConnector != null) {
	        		// attempt to resume the WFHardwareConnector instance.
	        		if (!(bResumed = mHardwareConnector.resume(this))) {
	        			// if the WFHardwareConnector instance failed to resume,
	        			// it must be re-initialized.
	        			mHardwareConnector.connectAnt();
	        		}
	        	}
	        	
	        	// if there is no suspended WFHardwareConnector instance,
	        	// configure the singleton instance.
	        	else {
			         // get the hardware connector singleton instance.
			        mHardwareConnector = WFHardwareConnector.getInstance(this, this);
					mHardwareConnector.connectAnt();
	        	}
		        
		        // restore connection state only if the previous
		        // WFHardwareConnector instance was not resumed.
		        if (!bResumed) {
			        // the connection state is cached in the state
			        // bundle (onSaveInstanceState).  this is used to
			        // restore previous connections.  if the Bundle
			        // is null, no connections are configured.
			        mHardwareConnector.restoreInstanceState(savedInstanceState);
		        }
		        
		        // configure the display settings.
		        //
		        // this demonstrates how to use the display
		        // settings.  if this step is skipped, the
		        // default settings will be used.
		        WFDisplaySettings settings = mHardwareConnector.getDisplaySettings();
		        settings.staleDataTimeout = 5.0f;          // seconds, default = 5
		        settings.staleDataString = "--";           // string to display when data is stale, default = "--"
		        settings.useMetricUnits = true;            // display metric units, default = false
		        settings.bikeWheelCircumference = 2.07f;   // meters, default = 2.07
		        settings.bikeCoastingTimeout = 3.0f;       // seconds, default = 3    
		        mHardwareConnector.setDisplaySettings(settings);
		        statusText = "ANT OK";
	        }
	        catch (WFAntNotSupportedException nse) {
	        	// ANT hardware not supported.
	        	statusText = "ANT not supported.";
	        }
	        catch (WFAntServiceNotInstalledException nie) {

				Toast installNotification = Toast.makeText(context, this.getResources().getString( R.string.ant_service_required), Toast.LENGTH_LONG);
				installNotification.show();

				// open the Market Place app, search for the ANT Radio service.
				mHardwareConnector.destroy();
				mHardwareConnector = null;
				WFHardwareConnector.installAntService(this);

				// close this app.
				finish();
	        }
			catch (WFAntException e) {
				statusText = "ANT initialization error.";
			}
       }
        else {
        	// ANT hardware not supported.
        	statusText = "ANT not supported.";
        }
        
        if(!this.isFinishing())
        {
            setContentView(R.layout.main);
        	((TextView)findViewById(R.id.antStatus)).setText(statusText);
        	((TextView)findViewById(R.id.antStatus)).setVisibility(TextView.VISIBLE);

        }
    }
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy enter");

		try {
			if (mHardwareConnector != null) {
				mHardwareConnector.destroy();
				mHardwareConnector = null;
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception in onDestroy", e);
		}

		super.onDestroy();

		Log.d(TAG, "onDestroy exit");
	}

	public void onRecordStart(View view) {
    	Log.d(TAG, "onRecordStart");    	
    }
    
    public void onExit(View view) {
    	Log.d(TAG, "onExit");
    	finish();
    }

	@Override
	public void hwConnAntError(WFAntError error) {
		switch (error) {
		case WF_ANT_ERROR_CLAIM_FAILED:
        	((TextView)findViewById(R.id.antStatus)).setText("ANT radio in use.");
			mHardwareConnector.forceAntConnection(getResources().getString(R.string.app_name));
			break;
		}
	}

	@Override
	public void hwConnConnectedSensor(WFSensorConnection arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnConnectionRestored() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnDisconnectedSensor(WFSensorConnection arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnHasData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnStateChanged(WFHardwareState arg0) {
		// TODO Auto-generated method stub
		
	}
}