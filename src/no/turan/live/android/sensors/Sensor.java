package no.turan.live.android.sensors;

import static no.turan.live.Constants.TAG;
import static no.turan.live.Constants.DEAD_SAMPLE_THRESHOLD;
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
		Log.d(TAG, "Sensor.connectionStateChanged " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber() + " - " + status.name());
		if (mSensor != null && !mSensor.isValid()) {
			Log.d(TAG, "sensor no longer valid");
			mSensor.setCallback(null);
			mSensor = null;
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
			switch (getState())
			{
				case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
					Log.d(TAG, "Sensor.connectSensor - sensor disconnecting");
				case WF_SENSOR_CONNECTION_STATUS_IDLE:
					Log.d(TAG, "Sensor.connectSensor - sensor idle");
				{
					// Bad states, reconnect.
					WFConnectionParams params = new WFConnectionParams();
					params.sensorType = mSensorType;
					mSensor = mHardwareConnector.initSensorConnection(params);
					if (mSensor != null) {
						mSensor.setCallback(this);
					} else {
						Log.d(TAG, "Sensor.connectSensor - no sensor - " + mSensorType);
					}
					break;
				}
				case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
					Log.d(TAG, "Sensor.connectSensor - sensor connected");
				case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
					Log.d(TAG, "Sensor.connectSensor - sensor connecting");
					// Good states, do nothing.
					break;
			}
		} else {
			Log.e(TAG, "Sensor.connectSensor - no hardware connector - " + mSensorType);
		}
	}

	protected void disconnectSensor() {
		Log.d(TAG, "Sensor.disconnectSensor - " + mSensorType + " - " + mSensor.getDeviceNumber());
		switch (getState())
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
	}

	protected void deadSample() {
		Log.d(TAG, "Sensor.deadSample: " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber());
		if (++mDeadSamples > DEAD_SAMPLE_THRESHOLD) {
			Log.d(TAG, "Sensor.deadSample threshold exceeded: " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber());
			disconnectSensor();
			connectSensor();
		}
	}
	
	private WFSensorConnectionStatus getState() {
		WFSensorConnectionStatus state = WFSensorConnectionStatus.WF_SENSOR_CONNECTION_STATUS_IDLE;
		if (mSensor != null) {
			state = mSensor.getConnectionStatus();
		} else {
			Log.d(TAG, "Sensor.getState: no sensor");
		}
		return state;
	}
}