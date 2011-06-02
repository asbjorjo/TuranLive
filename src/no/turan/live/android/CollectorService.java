package no.turan.live.android;


import static no.turan.live.android.Constants.EXERCISE_ID;
import static no.turan.live.android.Constants.MIN_GPS_ACCURACY;
import static no.turan.live.android.Constants.MPS_TO_KPH;
import static no.turan.live.android.Constants.SAMPLE_ALTITUDE_KEY;
import static no.turan.live.android.Constants.SAMPLE_EXERCISE_KEY;
import static no.turan.live.android.Constants.SAMPLE_LATITUDE_KEY;
import static no.turan.live.android.Constants.SAMPLE_LONGITUDE_KEY;
import static no.turan.live.android.Constants.SAMPLE_TIME_KEY;
import static no.turan.live.android.Constants.TAG;
import no.turan.live.android.sensors.HRSensor;
import no.turan.live.android.sensors.ICadenceSensor;
import no.turan.live.android.sensors.IHRSensor;
import no.turan.live.android.sensors.IPowerSensor;
import no.turan.live.android.sensors.ISpeedSensor;
import no.turan.live.android.sensors.PowerSensor;
import android.app.Notification;
import android.app.PendingIntent;
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
import com.wahoofitness.api.comm.WFSensorConnection;
import com.wahoofitness.api.comm.WFSensorConnection.WFSensorConnectionStatus;

public class CollectorService extends Service implements WFHardwareConnector.Callback, WFSensorConnection.Callback, LocationListener {
	private final IBinder mBinder = new CollectorBinder();
	private WFHardwareConnector mHardwareConnector;
	private IHRSensor hrSensor;
	private IPowerSensor powerSensor;
	private ICadenceSensor cadenceSensor;
	private ISpeedSensor speedSensor;
	private boolean mLive;
	private boolean mCollecting;
	private long sampleTime;
	private Intent sampleIntent;

	@Override
	public void hwConnAntError(WFAntError error) {
		switch (error) {
		case WF_ANT_ERROR_CLAIM_FAILED:
        	Log.d(TAG,".hwConnAntError - ANT radio in use.");
			mHardwareConnector.forceAntConnection(getResources().getString(R.string.app_name));
			break;
		}
	}

	@Override
	public void hwConnConnectedSensor(WFSensorConnection connection) {
		Log.d(TAG, "CollectorService.hwConnConnectedSensor - " + connection.getSensorType() + " - " + connection.getDeviceNumber());
	}

	@Override
	public void hwConnConnectionRestored() {
		Log.d(TAG, "CollectorService.hwConnConnectionRestored");
	}

	@Override
	public void hwConnDisconnectedSensor(WFSensorConnection connection) {
		Log.d(TAG, "CollectorService.hwConnDisconnectedSensor - " + connection.getSensorType() + " - " + connection.getDeviceNumber());
	}

	@Override
	public void hwConnHasData() {
		Log.v(TAG, "CollectorService.hwConnHasData");
		
		if (sampleTime < System.currentTimeMillis()/1000L) {
			// Send a sample every second.
			if (sampleIntent.hasExtra(SAMPLE_TIME_KEY)) {
				Log.d(TAG, "CollectorService.hwConnHasData - sample for processing");
				Log.v(TAG, "CollectorSerivce.hwConnHasData - " + sampleIntent.getExtras().toString());
				if (mLive) {
					Intent uploadIntent = new Intent(this, UploadService.class);
					uploadIntent.putExtras(sampleIntent.getExtras());
					startService(uploadIntent);
				}
				sendBroadcast(sampleIntent);
			}

			sampleTime = System.currentTimeMillis()/1000L;
			sampleIntent = new Intent("no.turan.live.android.SAMPLE");
			sampleIntent.putExtra(SAMPLE_TIME_KEY, sampleTime);
			sampleIntent.putExtra(SAMPLE_EXERCISE_KEY, EXERCISE_ID);
		}
		
		if (hrSensor != null) {
			Log.v(TAG, "CollectorService.hwConnHasData - HR");
			hrSensor.retrieveData(sampleIntent);
		}
		if (powerSensor != null) {
			Log.v(TAG, "CollectorService.hwConnHasData - power");
			powerSensor.retrieveData(sampleIntent);
		}
		if (cadenceSensor != null) {
			Log.v(TAG, "CollectorService.hwConnHasData - cadence");
			cadenceSensor.retrieveData(sampleIntent);
		}
		if (speedSensor != null) {
			Log.v(TAG, "CollectorService.hwConnHasData - speed");
			speedSensor.retrieveData(sampleIntent);
		}
	}

	@Override
	public void hwConnStateChanged(WFHardwareState state) {
		String antStatus = "";
		switch (state) {
		case WF_HARDWARE_STATE_DISABLED:
        	if (WFHardwareConnector.hasAntSupport(this)) {
        		Log.d(TAG,"CollectorService.hwConnStateChanged - HW Connector DISABLED.");
        		antStatus = "DISABLED";
        	}
        	else {
        		Log.d(TAG,"CollectorService.hwConnStateChanged - ANT Radio NOT supported.");
        		antStatus = "Not Supported";
        	}
			break;
			
		case WF_HARDWARE_STATE_SERVICE_NOT_INSTALLED:
        	Log.d(TAG,"CollectorService.hwConnStateChanged - ANT Radio Service NOT installed.");
        	antStatus = "Not Installed";
			break;
			
		case WF_HARDWARE_STATE_SUSPENDED:
        	Log.d(TAG,"CollectorService.hwConnStateChanged - HW Connector SUSPENDED.");
        	antStatus = "Suspended";
        	break;
        	
		case WF_HARDWARE_STATE_READY:
        	Log.d(TAG,"CollectorService.hwConnStateChanged - ANT OK");
        	antStatus = "Ready";
        	setupSensors();
        	break;
		default:
        	Log.d(TAG,"CollectorService.hwConnStateChanged - " + state.name());
        	antStatus = state.name();
			break;
		}
		Intent antState = new Intent("no.turan.live.android.ANT_STATE");
		antState.putExtra("no.turan.live.android.ANT_STATE_KEY", antStatus);
		sendBroadcast(antState);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "CollectorService.onDestroy");

		hrSensor = null;
		powerSensor = null;
		cadenceSensor = null;
		speedSensor = null;
		
		if (mHardwareConnector != null) {
			mHardwareConnector.destroy();
			mHardwareConnector = null;
		}
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
		
		mLive = false;
		mCollecting = false;
		
		Intent antState = new Intent("no.turan.live.android.ANT_STATE");
		antState.putExtra("no.turan.live.android.ANT_STATE", "Disconnected");
		Intent startedIntent = new Intent("no.turan.live.android.COLLECTOR_STOPPED");
		sendBroadcast(startedIntent);
		sendBroadcast(antState);
		
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "CollectorService.onCreate");
		
		sampleIntent = new Intent(this, UploadService.class);
		sampleTime = System.currentTimeMillis()/1000L;
		
    	mLive = false;
    	mCollecting = false;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "CollectorService.onStartCommand");
		
		Context context = this.getApplicationContext();
		
    	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	
    	try {
    		mHardwareConnector = WFHardwareConnector.getInstance(this, this);
    		mHardwareConnector.connectAnt();
    			        
            Log.d(TAG, "CollectorService.onStartCommand - ANT Connected");
        }
        catch (WFAntNotSupportedException nse) {
        	Log.e(TAG, "CollectorService.onStartCommand - ANT Not Supported");
        	stopSelf();
        }
        catch (WFAntServiceNotInstalledException nie) {
        	Log.e(TAG, "CollectorService.onStartCommand - ANT Not Installed");

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
			Log.e(TAG, "CollectorService.onStartCommand - ANT Initialization error", e);
			stopSelf();
		}
		
		Notification notification = new Notification(R.drawable.turan, getText(R.string.app_name), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, TuranLive.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, getText(R.string.app_name), getText(R.string.app_name), pendingIntent);
		startForeground(R.id.running_live, notification);
		
		mCollecting = true;
		
		Intent startedIntent = new Intent("no.turan.live.android.COLLECTOR_STARTED");
		sendBroadcast(startedIntent);
		
		return START_STICKY;
	}
	
	private void setupSensors() {
		hrSensor = new HRSensor();
		hrSensor.setupSensor(mHardwareConnector);
		PowerSensor power = new PowerSensor();
		power.setupSensor(mHardwareConnector);
		powerSensor = power;
		cadenceSensor = power;
	}

	public class CollectorBinder extends Binder implements ICollectorService {
		@Override
		public boolean isLive() {
			Log.d(TAG, "CollectorBinder.isLive - " + mLive);
			return mLive;
		}

		@Override
		public void goLive() {
			Log.d(TAG, "CollectorBinder.goLive");
			mLive = true;
		}

		@Override
		public boolean isCollecting() {
			Log.d(TAG, "CollectorBinder.isCollecting - " + mCollecting);
			return mCollecting;
		}

		@Override
		public void goOff() {
			Log.d(TAG, "CollectorBinder.goOff");
			mLive = false;
		}
    }

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "CollectorService.onBind");
		return mBinder;
	}

	@Override
	public void connectionStateChanged(WFSensorConnectionStatus status) {
		Log.d(TAG, "CollectorService.connectionStateChanged enter");

		Log.d(TAG, "CollectorService.connectionStateChanged exit");
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "CollectorService.onLocationChanged - " + location.toString());
		if (location.hasAccuracy() && location.getAccuracy() < MIN_GPS_ACCURACY) {
			sampleIntent.putExtra(SAMPLE_LATITUDE_KEY, location.getLatitude());
			sampleIntent.putExtra(SAMPLE_LONGITUDE_KEY, location.getLongitude());
			if (location.hasAltitude()) {
				sampleIntent.putExtra(SAMPLE_ALTITUDE_KEY, location.getAltitude());
			}
			if (location.hasSpeed()) {
				sampleIntent.putExtra(Constants.SAMPLE_SPEED_KEY, location.getSpeed() * MPS_TO_KPH);
			}
		}
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
		Log.d(TAG, "Location provider state changed: " + provider + " - " + status);
	}
}
