package no.turan.live.android;


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
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
	private WFHardwareConnector hardwareConnector_;
	private IHRSensor hrSensor_;
	private IPowerSensor powerSensor_;
	private ICadenceSensor cadenceSensor_;
	private ISpeedSensor speedSensor_;
	private boolean live_;
	private boolean collecting_;
	private long sampleTime_;
	private Intent sampleIntent_;
	private int exerciseId_;
	private boolean hrOn_;
	private boolean speedOn_;
	private boolean cadenceOn_;
	private boolean powerOn_;
	private Location lastLocation_;
	private int badLocationCount_ = 0;
	private float distance_;
	
	@Override
	public void hwConnAntError(WFAntError error) {
		switch (error) {
		case WF_ANT_ERROR_CLAIM_FAILED:
        	Log.d(TAG,".hwConnAntError - ANT radio in use.");
			hardwareConnector_.forceAntConnection(getResources().getString(R.string.app_name));
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
		
		if (sampleTime_ < System.currentTimeMillis()/1000L) {
			// Send a sample every second.
			if (sampleIntent_.hasExtra(SAMPLE_TIME_KEY)) {
				Log.v(TAG, "CollectorService.hwConnHasData - sample for processing");
				Log.d(TAG, "CollectorSerivce.hwConnHasData - " + sampleIntent_.getExtras().toString());
				
				if (live_) {
					Intent uploadIntent = new Intent(this, TuranUploadService.class);
					uploadIntent.putExtras(sampleIntent_.getExtras());
					startService(uploadIntent);
				}
				sendBroadcast(sampleIntent_);
			}

			sampleTime_ = System.currentTimeMillis()/1000L;
			sampleIntent_ = new Intent("no.turan.live.android.SAMPLE");
			sampleIntent_.putExtra(SAMPLE_TIME_KEY, sampleTime_);
			sampleIntent_.putExtra(SAMPLE_EXERCISE_KEY, exerciseId_);
		}
		
		if (hrSensor_ != null) {
			Log.v(TAG, "CollectorService.hwConnHasData - HR");
			hrSensor_.retrieveData(sampleIntent_);
		}
		if (powerSensor_ != null) {
			Log.v(TAG, "CollectorService.hwConnHasData - power");
			powerSensor_.retrieveData(sampleIntent_);
		}
		// Don't bother querying sensors twice if they serve more than one type of data.
		if (speedSensor_ != null && speedSensor_ != powerSensor_) {
			Log.v(TAG, "CollectorService.hwConnHasData - speed");
			speedSensor_.retrieveData(sampleIntent_);
		}
		// Don't bother querying sensors twice if they serve more than one type of data.
		if (cadenceSensor_ != null && cadenceSensor_ != powerSensor_ && cadenceSensor_ != speedSensor_) {
			Log.v(TAG, "CollectorService.hwConnHasData - cadence");
			cadenceSensor_.retrieveData(sampleIntent_);
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
        	connectSensors();
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
		super.onDestroy();
		Log.v(TAG, "CollectorService.onDestroy");

		disconnectSensors();
		
		if (hardwareConnector_ != null) {
			hardwareConnector_.destroy();
			hardwareConnector_ = null;
		}
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
		
		live_ = false;
		collecting_ = false;
		exerciseId_ = 0;
		
		Intent antState = new Intent("no.turan.live.android.ANT_STATE");
		antState.putExtra("no.turan.live.android.ANT_STATE", "Disconnected");
		Intent startedIntent = new Intent("no.turan.live.android.COLLECTOR_STOPPED");
		sendBroadcast(startedIntent);
		sendBroadcast(antState);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "CollectorService.onCreate");
		
		sampleIntent_ = new Intent(this, TuranUploadService.class);
		sampleTime_ = System.currentTimeMillis()/1000L;
		
    	live_ = false;
    	collecting_ = false;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "CollectorService.onStartCommand");
		
		Context context = this.getApplicationContext();
		
    	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	
    	try {
    		hardwareConnector_ = WFHardwareConnector.getInstance(this, this);
    		hardwareConnector_.connectAnt();
    		hardwareConnector_.setSampleRate(500);
    		//hardwareConnector_.setSampleTimerDataCheck(true);
    			        
            Log.v(TAG, "CollectorService.onStartCommand - ANT Connected");
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
			hardwareConnector_.destroy();
			hardwareConnector_ = null;
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
		
		collecting_ = true;
		
		Intent startedIntent = new Intent("no.turan.live.android.COLLECTOR_STARTED");
		sendBroadcast(startedIntent);
		
		return START_STICKY;
	}
	
	private void connectSensors() {
		//SharedPreferences preferences = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Log.d(TAG, "CollectorService.connectSensors - " + preferences.getAll().toString());
		hrOn_ = preferences.getBoolean("hr_enable", false);
		speedOn_ = preferences.getBoolean("speed_enable", false);
		cadenceOn_ = preferences.getBoolean("cadence_enable", false);
		powerOn_ = preferences.getBoolean("power_enable", false);

		if (hrOn_) {
			try {
				short hrId = Short.parseShort(preferences.getString("hr_id", "0"));
				hrSensor_ = new HRSensor(hrId);
			} catch (NumberFormatException e) {
				hrSensor_ = new HRSensor();
			}
			hrSensor_.setupSensor(hardwareConnector_);
		}
		if (powerOn_) {
			powerSensor_ = new PowerSensor();
			powerSensor_.setupSensor(hardwareConnector_);
			if (!cadenceOn_) {
				cadenceSensor_ = (ICadenceSensor) powerSensor_;
			}
		}
	}

	private void disconnectSensors() {
		if (hrSensor_!=null) {
			hrSensor_.disconnectSensor();
			hrSensor_ = null;
		}
		if (powerSensor_!=null) {
			powerSensor_.disconnectSensor();
			powerSensor_ = null;
		}
		if (cadenceSensor_!=null) {
			cadenceSensor_.disconnectSensor();
			cadenceSensor_ = null;
		}
		if (speedSensor_!=null) {
			speedSensor_.disconnectSensor();
			speedSensor_ = null;
		}
	}

	public class CollectorBinder extends Binder implements ICollectorService {
		@Override
		public boolean isLive() {
			Log.d(TAG, "CollectorBinder.isLive - " + live_);
			return live_;
		}

		@Override
		public void goLive(int exerciseId) {
			Log.d(TAG, "CollectorBinder.goLive - " + exerciseId);
			if (exerciseId > 0) {
				exerciseId_ = exerciseId;
				live_ = true;
			}
		}

		@Override
		public boolean isCollecting() {
			Log.d(TAG, "CollectorBinder.isCollecting - " + collecting_);
			return collecting_;
		}

		@Override
		public void goOff() {
			Log.d(TAG, "CollectorBinder.goOff");
			live_ = false;
		}

		@Override
		public int getExercise() {
			Log.v(TAG, "CollectorBinder.getExercise - " + exerciseId_);
			return exerciseId_;
		}
    }

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "CollectorService.onBind");
		return mBinder;
	}

	@Override
	public void connectionStateChanged(WFSensorConnectionStatus status) {
		Log.v(TAG, "CollectorService.connectionStateChanged enter");

		Log.v(TAG, "CollectorService.connectionStateChanged exit");
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "CollectorService.onLocationChanged - " + location.toString());
		//SharedPreferences preferences = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Log.v(TAG, preferences.toString());
		
		boolean speedOn = preferences.getBoolean("speed_enable", false);
		
		if (location.hasAccuracy() && location.getAccuracy() < MIN_GPS_ACCURACY) {
			sampleIntent_.putExtra(SAMPLE_LATITUDE_KEY, location.getLatitude());
			sampleIntent_.putExtra(SAMPLE_LONGITUDE_KEY, location.getLongitude());
			if (location.hasAltitude()) {
				sampleIntent_.putExtra(SAMPLE_ALTITUDE_KEY, location.getAltitude());
			}
			if (!speedOn && location.hasSpeed()) {
				sampleIntent_.putExtra(Constants.SAMPLE_SPEED_KEY, location.getSpeed() * MPS_TO_KPH);
			}

			distance_ = location.distanceTo(lastLocation_);
			sampleIntent_.putExtra(Constants.SAMPLE_DISTANCE_KEY, distance_);
			
			badLocationCount_ = 0;
			lastLocation_ = location;
		} else if (++badLocationCount_ > Constants.BAD_LOCATION_THRESHOLD) {
			lastLocation_ = null;
			distance_ = 0;
		} else if (lastLocation_ != null) {
			sampleIntent_.putExtra(SAMPLE_LATITUDE_KEY, lastLocation_.getLatitude());
			sampleIntent_.putExtra(SAMPLE_LONGITUDE_KEY, lastLocation_.getLongitude());
			if (lastLocation_.hasAltitude()) {
				sampleIntent_.putExtra(SAMPLE_ALTITUDE_KEY, lastLocation_.getAltitude());
			}
			if (!speedOn && lastLocation_.hasSpeed()) {
				sampleIntent_.putExtra(Constants.SAMPLE_SPEED_KEY, lastLocation_.getSpeed() * MPS_TO_KPH);
			}
			sampleIntent_.putExtra(Constants.SAMPLE_DISTANCE_KEY, distance_);
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
