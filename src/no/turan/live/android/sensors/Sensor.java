package no.turan.live.android.sensors;

import static no.turan.live.android.Constants.DEAD_SAMPLE_THRESHOLD;
import static no.turan.live.android.Constants.TAG;
import android.util.Log;

import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.comm.WFConnectionParams;
import com.wahoofitness.api.comm.WFSensorConnection;
import com.wahoofitness.api.comm.WFConnectionParams.WFDeviceParams;
import com.wahoofitness.api.comm.WFSensorConnection.WFSensorConnectionStatus;

public abstract class Sensor implements ISensor {
	protected WFHardwareConnector hardwareConnector_;
	protected WFSensorConnection sensor_;
	protected short sensorType_;
	protected short sensorId_ = 0;
	protected int deadSamples_ = 0;
	protected long previousSampleTime_ = 0;
	protected boolean reconnectable_ = false;
	
	protected Sensor(short sensorType) {
		sensorType_ = sensorType;
	}
	
	protected Sensor(short sensorType, short sensorId) {
		sensorType_ = sensorType;
		sensorId_ = sensorId;
	}
	
	public int getSensorId() {
		if (sensor_ != null) {
			return sensor_.getDeviceNumber();
		} else {
			return sensorId_;
		}
	}

	@Override
	public void connectionStateChanged(WFSensorConnectionStatus status) {
		if (sensor_ != null) {
			Log.d(TAG, "Sensor.connectionStateChanged - " + sensor_.getSensorType() + " - " + sensor_.getDeviceNumber() + " - " + status.name());
		} else {
			Log.d(TAG, "Sensor.connectionStateChanged - " + sensorType_ + " - null");
		}
		if (sensor_ != null && sensor_.isValid()) {
			switch(sensor_.getConnectionStatus()) {
			case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
			case WF_SENSOR_CONNECTION_STATUS_CONNECTING:	
				reconnectable_ = false;
				deadSamples_ = 0;
				break;
			}
		} else if (sensor_ != null && !sensor_.isValid()) {
			Log.d(TAG, "Sensor.connectionStateChanged - " + sensor_.getSensorType() + " - " + sensor_.getDeviceNumber() + " - invalid");
			sensor_.setCallback(null);
			sensor_ = null;
			connectSensor();
			reconnectable_ = true;
		}
	}
	
	public void setupSensor(WFHardwareConnector hardwareConnector) {
		Log.d(TAG, "Sensor.setupSensor - " + sensorType_);
		hardwareConnector_ = hardwareConnector;
		connectSensor();
	}
	
	public void connectSensor() {
		Log.d(TAG, "Sensor.connectSensor - " + sensorType_);
		if (hardwareConnector_ != null) {
			if (sensor_ != null) {
				Log.d(TAG, "Sensor.connectSensor - " + sensorType_ + " - " + sensor_.getDeviceNumber() + " - " + sensor_.getConnectionStatus());
				switch (sensor_.getConnectionStatus())
				{
					case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
					case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
						break;
					case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
					case WF_SENSOR_CONNECTION_STATUS_IDLE:
						sensor_.setCallback(null);
						sensor_ = null;
						break;
				}
			}
			if (sensor_ == null) {
				Log.d(TAG, "Sensor.connectSensor - " + sensorType_ + " - no sensor defined");
				WFConnectionParams params = new WFConnectionParams();
				WFDeviceParams deviceParam = new WFDeviceParams();
				params.sensorType = sensorType_;
				deviceParam.deviceNumber = sensorId_;
				params.device1 = deviceParam;
				sensor_ = hardwareConnector_.initSensorConnection(params);
				if (sensor_ != null) {
					Log.d(TAG, "Sensor.connectSensor - " + sensorType_ + " - initialised");
					sensor_.setCallback(this);
				} else {
					Log.e(TAG, "Sensor.connectSensor - " + sensorType_ + " - could not initialise");
				}
			}
		} else {
			Log.e(TAG, "Sensor.connectSensor - no hardware connector - " + sensorType_);
		}
	}

	public void disconnectSensor() {
		if (sensor_ != null) {
			Log.d(TAG, "Sensor.disconnectSensor - " + sensorType_ + " - " + sensor_.getDeviceNumber());
			switch (sensor_.getConnectionStatus())
			{
				case WF_SENSOR_CONNECTION_STATUS_IDLE:
					sensor_.setCallback(null);
					sensor_ = null;
					break;
				case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
				case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
				case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
					sensor_.disconnect();
					break;
			}
		} else {
			Log.d(TAG, "Sensor.disconnectSensor - " + sensorType_ + " - no sensor");
		}
	}

	protected void deadSample() {
		Log.d(TAG, "Sensor.deadSample - " + sensor_.getSensorType() + " - " + sensor_.getDeviceNumber());
		if (++deadSamples_ > DEAD_SAMPLE_THRESHOLD) {
			Log.d(TAG, "Sensor.deadSample threshold exceeded: " + sensor_.getSensorType() + " - " + sensor_.getDeviceNumber());
			disconnectSensor();
			connectSensor();
			reconnectable_ = true;
		}
	}
}
