package no.turan.live.android;


import static no.turan.live.Constants.TAG;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.wahoofitness.api.WFAntException;
import com.wahoofitness.api.WFAntNotSupportedException;
import com.wahoofitness.api.WFAntServiceNotInstalledException;
import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.WFHardwareConnectorTypes.WFAntError;
import com.wahoofitness.api.WFHardwareConnectorTypes.WFHardwareState;
import com.wahoofitness.api.comm.WFSensorConnection;

public class AntService extends Service implements WFHardwareConnector.Callback {
	private final IBinder mBinder = new AntBinder();
	private WFHardwareConnector mHardwareConnector;
	
	@Override
	public void hwConnAntError(WFAntError error) {
		switch (error) {
		case WF_ANT_ERROR_CLAIM_FAILED:
        	Log.d(TAG,"ANT radio in use.");
			mHardwareConnector.forceAntConnection(getResources().getString(R.string.app_name));
			break;
		}
	}

	@Override
	public void hwConnConnectedSensor(WFSensorConnection connection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnConnectionRestored() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnDisconnectedSensor(WFSensorConnection connection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnHasData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hwConnStateChanged(WFHardwareState state) {
		switch (state) {
		case WF_HARDWARE_STATE_DISABLED:
        	if (WFHardwareConnector.hasAntSupport(this)) {
        		Log.d(TAG,"HW Connector DISABLED.");
        	}
        	else {
        		Log.d(TAG,"ANT Radio NOT supported.");
        	}
			break;
			
		case WF_HARDWARE_STATE_SERVICE_NOT_INSTALLED:
        	Log.d(TAG,"ANT Radio Service NOT installed.");
			break;
			
		case WF_HARDWARE_STATE_SUSPENDED:
        	Log.d(TAG,"HW Connector SUSPENDED.");
        	break;
        	
		case WF_HARDWARE_STATE_READY:
        	Log.d(TAG,"ANT OK");
        	break;
		default:
        	Log.d(TAG,state.name());
			break;
	}
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
    	Context context = this.getApplicationContext();
    	String antStatus = "";

    	try {
    		mHardwareConnector = WFHardwareConnector.getInstance(this, this);
    		mHardwareConnector.connectAnt();
    			        
            antStatus = "ANT OK";
        }
        catch (WFAntNotSupportedException nse) {
        	// ANT hardware not supported.
        	antStatus = "ANT not supported.";
        }
        catch (WFAntServiceNotInstalledException nie) {

			Toast installNotification = Toast.makeText(context, this.getResources().getString( R.string.ant_service_required), Toast.LENGTH_LONG);
			installNotification.show();

			// open the Market Place app, search for the ANT Radio service.
			mHardwareConnector.destroy();
			mHardwareConnector = null;
			WFHardwareConnector.installAntService(this);

			// close this app.
			stopSelf();
        }
		catch (WFAntException e) {
			antStatus = "ANT initialization error.";
		}
		Log.d(TAG,antStatus);
	}
	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "AntService.onStartCommand");
		return START_STICKY;
	}
	public class AntBinder extends Binder {
        AntService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AntService.this;
        }
    }
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
