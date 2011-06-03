package no.turan.live.android.sensors;

import static no.turan.live.android.Constants.DEAD_SAMPLE_THRESHOLD;
import static no.turan.live.android.Constants.SAMPLE_CADENCE_KEY;
import static no.turan.live.android.Constants.SAMPLE_SPEED_KEY;
import static no.turan.live.android.Constants.TAG;
import android.content.Intent;
import android.util.Log;

import com.wahoofitness.api.WFHardwareConnectorTypes.WFSensorType;
import com.wahoofitness.api.data.WFBikeSpeedCadenceData;

public class SpeedCadenceSensor extends Sensor implements ICadenceSensor,
		ISpeedSensor {
	private long previousSpeedTime_;
	private int deadSpeedSamples_;
	private long previousCadenceTime_;
	private int deadCadenceSamples_;

	public SpeedCadenceSensor() {
		super(WFSensorType.WF_SENSORTYPE_BIKE_SPEED_CADENCE);
	}
	
	@Override
	public int getSpeed() {
		int speed = -1;
		if (sensor_ != null && sensor_.isConnected()) {
			Log.v(TAG, "SpeedCadence.getSpeed - good sensor");
			WFBikeSpeedCadenceData data = (WFBikeSpeedCadenceData) sensor_.getData();
			
			Log.d(TAG, "SpeedCadenceSensor.getSpeed - " + data.speedTimestamp + " - " + data.instantWheelRPM);
			
			if (data.speedTimestamp != previousSpeedTime_) {
				Log.d(TAG, "SpeedCadenceSensor.getSpeed - good data");
				speed = data.instantWheelRPM;
				previousSpeedTime_ = data.speedTimestamp;
				deadSpeedSamples_ = 0;
			} else {
				deadSpeedSample();
			}
		} else {
			Log.w(TAG, "SpeedCadenceSensor.getSpeed - no sensor");
		}

		return speed;
	}

	private void deadSpeedSample() {
		Log.d(TAG, "SpeedCadenceSensor.deadSpeedSample: " + sensor_.getSensorType() + " - " + sensor_.getDeviceNumber());
		deadSpeedSamples_++;
		deadSample();
	}

	@Override
	public int getCadence() {
		int cadence = -1;
		if (sensor_ != null && sensor_.isConnected()) {
			Log.v(TAG, "SpeedCadence.getCadence - good sensor");
			WFBikeSpeedCadenceData data = (WFBikeSpeedCadenceData) sensor_.getData();
			
			Log.d(TAG, "SpeedCadenceSensor.getCadence - " + data.cadenceTimestamp + " - " + data.instantCrankRPM);
			
			if (data.cadenceTimestamp != previousCadenceTime_) {
				Log.d(TAG, "SpeedCadenceSensor.getCadence - good data");
				cadence = data.instantCrankRPM;
				previousCadenceTime_ = data.cadenceTimestamp;
				deadCadenceSamples_ = 0;
			} else {
				deadCadenceSample();
			}
		} else {
			Log.w(TAG, "SpeedCadenceSensor.getCadence - no sensor");
		}

		return cadence;
	}

	private void deadCadenceSample() {
		Log.d(TAG, "SpeedCadenceSensor.deadCadenceSample: " + sensor_.getSensorType() + " - " + sensor_.getDeviceNumber());
		deadCadenceSamples_++;
		deadSample();
	}

	@Override
	public void retrieveData(Intent intent) {
		int speed = getSpeed();
		int cadence = getCadence();
		
		if (speed >= 0) {
			intent.putExtra(SAMPLE_SPEED_KEY, speed);
		}
		if (cadence >= 0) {
			intent.putExtra(SAMPLE_CADENCE_KEY, cadence);
		}
	}

	@Override
	public void retrieveData(SensorData sensorData) {
		int speed = getSpeed();
		int cadence = getCadence();
		
		if (speed >= 0) {
			sensorData.setSpeed(speed);
		}
		if (cadence >= 0) {
			sensorData.setCadence(cadence);
		}
	}

	@Override
	protected void deadSample() {
		if (deadCadenceSamples_ > DEAD_SAMPLE_THRESHOLD && deadSpeedSamples_ > DEAD_SAMPLE_THRESHOLD) {
			Log.d(TAG, "SpeedCadenceSensor.deadCadenceSample threshold exceeded: " + sensor_.getSensorType() + " - " + sensor_.getDeviceNumber());
			disconnectSensor();
			connectSensor();
			deadCadenceSamples_ = 0;
			deadSpeedSamples_ = 0;
		} else {
			Log.v(TAG, "SpeedCadenceSensor.deadSample - speed: " + deadSpeedSamples_ + " cadence: " + deadCadenceSamples_);
		}
	}
}
