package no.turan.live.android.sensors;

import static no.turan.live.android.Constants.DEAD_SAMPLE_THRESHOLD;
import static no.turan.live.android.Constants.TAG;
import android.util.Log;

import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.comm.WFConnectionParams;
import com.wahoofitness.api.comm.WFSensorConnection;
import com.wahoofitness.api.comm.WFSensorConnection.WFSensorConnectionStatus;

public abstract class Sensor implements ISensor {
	protected WFHardwareConnector mHardwareConnector;
	protected WFSensorConnection mSensor;
	protected short mSensorType;
	protected int mDeadSamples = 0;
	protected long mPreviousSampleTime = 0;
	
	protected Sensor(short sensorType) {
		mSensorType = sensorType;
	}
	
	public int getSensorId() {
		return mSensor.getDeviceNumber();
	}

	@Override
	public void connectionStateChanged(WFSensorConnectionStatus status) {
		if (mSensor != null) {
			Log.d(TAG, "Sensor.connectionStateChanged - " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber() + " - " + status.name());
		} else {
			Log.d(TAG, "Sensor.connectionStateChanged - " + mSensorType + " - null");
		}
		if (mSensor != null && !mSensor.isValid()) {
			Log.d(TAG, "Sensor.connectionStateChanged - " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber() + " - invalid");
			mSensor.setCallback(null);
			mSensor = null;
			connectSensor();
		}
	}
	
	public void setupSensor(WFHardwareConnector hardwareConnector) {
		Log.d(TAG, "Sensor.setupSensor - " + mSensorType);
		mHardwareConnector = hardwareConnector;
		connectSensor();
	}
	
	protected void connectSensor() {
		Log.d(TAG, "Sensor.connectSensor - " + mSensorType);
		if (mHardwareConnector != null) {
			if (mSensor != null) {
				Log.d(TAG, "Sensor.connectSensor - " + mSensorType + " - " + mSensor.getDeviceNumber() + " - " + mSensor.getConnectionStatus());
				switch (mSensor.getConnectionStatus())
				{
					case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
					case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
						break;
					case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
					case WF_SENSOR_CONNECTION_STATUS_IDLE:
						mSensor = null;
						break;
				}
			}
			if (mSensor == null) {
				Log.d(TAG, "Sensor.connectSensor - " + mSensorType + " - no sensor defined");
				WFConnectionParams params = new WFConnectionParams();
				params.sensorType = mSensorType;
				mSensor = mHardwareConnector.initSensorConnection(params);
				if (mSensor != null) {
					mSensor.setCallback(this);
				} else {
					Log.d(TAG, "Sensor.connectSensor - " + mSensorType + " - no sensor found");
				}
			}
		} else {
			Log.e(TAG, "Sensor.connectSensor - no hardware connector - " + mSensorType);
		}
	}

	protected void disconnectSensor() {
		if (mSensor != null) {
			Log.d(TAG, "Sensor.disconnectSensor - " + mSensorType + " - " + mSensor.getDeviceNumber());
			switch (mSensor.getConnectionStatus())
			{
				case WF_SENSOR_CONNECTION_STATUS_IDLE:
					if (mSensor != null) {
						mSensor.setCallback(null);
						mSensor = null;
						break;
					}
				case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
				case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
				case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
					mSensor.disconnect();
					break;
			}
		} else {
			Log.d(TAG, "Sensor.disconnectSensor - " + mSensorType + " - no sensor");
		}
	}

	protected void deadSample() {
		Log.d(TAG, "Sensor.deadSample - " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber());
		if (++mDeadSamples > DEAD_SAMPLE_THRESHOLD) {
			Log.d(TAG, "Sensor.deadSample threshold exceeded: " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber());
			disconnectSensor();
			connectSensor();
			mDeadSamples = 0;
		}
	}
}
