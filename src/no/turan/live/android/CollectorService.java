package no.turan.live.android;


import static no.turan.live.Constants.TAG;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.wahoofitness.api.WFAntException;
import com.wahoofitness.api.WFAntNotSupportedException;
import com.wahoofitness.api.WFAntServiceNotInstalledException;
import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.WFHardwareConnectorTypes.WFAntError;
import com.wahoofitness.api.WFHardwareConnectorTypes.WFHardwareState;
import com.wahoofitness.api.WFHardwareConnectorTypes.WFSensorType;
import com.wahoofitness.api.comm.WFConnectionParams;
import com.wahoofitness.api.comm.WFHeartrateConnection;
import com.wahoofitness.api.comm.WFSensorConnection;
import com.wahoofitness.api.comm.WFConnectionParams.WFDeviceParams;
import com.wahoofitness.api.comm.WFSensorConnection.WFSensorConnectionStatus;
import com.wahoofitness.api.data.WFHeartrateData;

public class CollectorService extends Service implements WFHardwareConnector.Callback, WFSensorConnection.Callback, LocationListener {
	private final IBinder mBinder = new AntBinder();
	private WFHardwareConnector mHardwareConnector;
	private WFHeartrateConnection heartRate;
	private Long previousHRtime = 0L;
	private short deadHRcount = 0;
	
	
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
		Log.d(TAG, "Connected sensor:" + connection.getSensorType() + " - " + connection.getDeviceNumber());
	}

	@Override
	public void hwConnConnectionRestored() {
		Log.d(TAG, "hwConnConnectionRestored");
	}

	@Override
	public void hwConnDisconnectedSensor(WFSensorConnection connection) {
		Log.d(TAG, "Disconnected sensor:" + connection.getSensorType() + " - " + connection.getDeviceNumber());
	}

	@Override
	public void hwConnHasData() {
		Log.d(TAG, "hwConnHasData");
		if (heartRate != null) {
			if (heartRate.isConnected() && heartRate.isValid()) {
				if (deadHRcount > 10) {
					Log.d(TAG, "HR got over 10 dead samples, disconnecting");
					heartRate.disconnect();
					deadHRcount = 0;
				}
				Log.d(TAG, "Connected to sensor: " + heartRate.getDeviceNumber());
				WFHeartrateData data = heartRate.getHeartrateData();
				if (data.timestamp != previousHRtime) { 
					previousHRtime = data.timestamp;
					deadHRcount = 0;
					Log.d(TAG, data.timestamp  + " HR: " +data.computedHeartrate);
					Intent intent = new Intent(this, UploadService.class);
					intent.putExtra("no.turan.live.android.TIME", System.currentTimeMillis()/1000L);
					intent.putExtra("no.turan.live.android.HR", data.computedHeartrate);
					intent.putExtra("no.turan.live.android.EXERCISE", "4589");
					startService(intent);
				} else {
					Log.d(TAG, "No new HR");
					deadHRcount++;
				}
			} else {
				Log.d(TAG, "heartRate not connected");
				if (heartRate.getConnectionStatus() == WFSensorConnectionStatus.WF_SENSOR_CONNECTION_STATUS_CONNECTING) {
					Log.d(TAG, "sensor connecting");
				} else {
					WFConnectionParams connectionParams = new WFConnectionParams();
					connectionParams.sensorType = WFSensorType.WF_SENSORTYPE_HEARTRATE;
					heartRate = (WFHeartrateConnection)mHardwareConnector.initSensorConnection(connectionParams);
					if (heartRate != null)
					{
						heartRate.setCallback(this);
					}
				}
			}
		} else {
			Log.d(TAG, "No heartRate");
			WFConnectionParams connectionParams = new WFConnectionParams();
			connectionParams.sensorType = WFSensorType.WF_SENSORTYPE_HEARTRATE;
			heartRate = (WFHeartrateConnection)mHardwareConnector.initSensorConnection(connectionParams);
			if (heartRate != null)
			{
				heartRate.setCallback(this);
			}
		}
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

	@Override
	public void onDestroy() {
		Log.d(TAG, "AntService.onDestroy");
		if (heartRate != null) {
			if (heartRate.isConnected()) {
				heartRate.disconnect();
			}
			heartRate.setCallback(null);
			heartRate = null;
		}
		if (mHardwareConnector != null) {
			mHardwareConnector.destroy();
			mHardwareConnector = null;
		}
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
		
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "AntService.onCreate");
		
    	Context context = this.getApplicationContext();
    	
    	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	
    	try {
    		mHardwareConnector = WFHardwareConnector.getInstance(this, this);
    		mHardwareConnector.connectAnt();
    			        
            Log.d(TAG, "ANT Connected");
        }
        catch (WFAntNotSupportedException nse) {
        	// ANT hardware not supported.
        	Log.e(TAG, "ANT Not Supported");
        }
        catch (WFAntServiceNotInstalledException nie) {
        	Log.e(TAG, "ANT Not Installed");

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
			Log.e(TAG, "ANT Initialization error", e);
		}
		
		Log.d(TAG, "AntService created");
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
        CollectorService getService() {
            // Return this instance of AntService so clients can call public methods
            return CollectorService.this;
        }
    }
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void connectionStateChanged(WFSensorConnectionStatus status) {
		Log.d(TAG, "connectionStateChanged enter");
		if (heartRate != null && !heartRate.isValid())
		{
			Log.d(TAG, "heartRate invalid, reconnect");
			heartRate.setCallback(null);
			heartRate = null;
			WFConnectionParams connectionParams = new WFConnectionParams();
			connectionParams.sensorType = WFSensorType.WF_SENSORTYPE_HEARTRATE;
			heartRate = (WFHeartrateConnection)mHardwareConnector.initSensorConnection(connectionParams);
			if (heartRate != null)
			{
				Log.d(TAG, "found sensor: " + heartRate.getDeviceNumber());
				heartRate.setCallback(this);
			}
		}
		Log.d(TAG, "connectionStateChanged exit");
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location changed: " + location.toString());
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "Location provider disabled: " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Location provider enabled: " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
