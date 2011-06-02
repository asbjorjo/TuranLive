package no.turan.live.android.sensors;

import static no.turan.live.android.Constants.DEAD_SAMPLE_THRESHOLD;
import static no.turan.live.android.Constants.SAMPLE_CADENCE_KEY;
import static no.turan.live.android.Constants.SAMPLE_SPEED_KEY;
import static no.turan.live.android.Constants.TAG;
import no.turan.live.android.Constants;
import android.content.Intent;
import android.util.Log;

import com.wahoofitness.api.WFHardwareConnectorTypes.WFSensorType;
import com.wahoofitness.api.data.WFBikeSpeedCadenceData;
import com.wahoofitness.api.data.WFHeartrateData;

public class SpeedCadenceSensor extends Sensor implements ICadenceSensor,
		ISpeedSensor {
	private long mPreviousSpeedTime;
	private int mDeadSpeedSamples;
	private long mPreviousCadenceTime;
	private int mDeadCadenceSamples;

	public SpeedCadenceSensor() {
		super(WFSensorType.WF_SENSORTYPE_BIKE_SPEED_CADENCE);
	}
	
	@Override
	public int getSpeed() {
		int speed = -1;
		if (mSensor != null && mSensor.isConnected()) {
			Log.v(TAG, "SpeedCadence.getSpeed - good sensor");
			WFBikeSpeedCadenceData data = (WFBikeSpeedCadenceData) mSensor.getData();
			
			Log.d(TAG, "SpeedCadenceSensor.getSpeed - " + data.speedTimestamp + " - " + data.instantWheelRPM);
			
			if (data.speedTimestamp != mPreviousSpeedTime) {
				Log.d(TAG, "SpeedCadenceSensor.getSpeed - good data");
				speed = data.instantWheelRPM;
				mPreviousSpeedTime = data.speedTimestamp;
				mDeadSpeedSamples = 0;
			} else {
				deadSpeedSample();
			}
		} else {
			Log.w(TAG, "SpeedCadenceSensor.getSpeed - no sensor");
		}

		return speed;
	}

	private void deadSpeedSample() {
		Log.d(TAG, "SpeedCadenceSensor.deadSpeedSample: " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber());
		mDeadSpeedSamples++;
		deadSample();
	}

	@Override
	public int getCadence() {
		int cadence = -1;
		if (mSensor != null && mSensor.isConnected()) {
			Log.v(TAG, "SpeedCadence.getCadence - good sensor");
			WFBikeSpeedCadenceData data = (WFBikeSpeedCadenceData) mSensor.getData();
			
			Log.d(TAG, "SpeedCadenceSensor.getCadence - " + data.cadenceTimestamp + " - " + data.instantCrankRPM);
			
			if (data.cadenceTimestamp != mPreviousCadenceTime) {
				Log.d(TAG, "SpeedCadenceSensor.getCadence - good data");
				cadence = data.instantCrankRPM;
				mPreviousCadenceTime = data.cadenceTimestamp;
				mDeadCadenceSamples = 0;
			} else {
				deadCadenceSample();
			}
		} else {
			Log.w(TAG, "SpeedCadenceSensor.getCadence - no sensor");
		}

		return cadence;
	}

	private void deadCadenceSample() {
		Log.d(TAG, "SpeedCadenceSensor.deadCadenceSample: " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber());
		mDeadCadenceSamples++;
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
	protected void deadSample() {
		if (mDeadCadenceSamples > DEAD_SAMPLE_THRESHOLD && mDeadSpeedSamples > DEAD_SAMPLE_THRESHOLD) {
			Log.d(TAG, "SpeedCadenceSensor.deadCadenceSample threshold exceeded: " + mSensor.getSensorType() + " - " + mSensor.getDeviceNumber());
			disconnectSensor();
			connectSensor();
			mDeadCadenceSamples = 0;
			mDeadSpeedSamples = 0;
		} else {
			Log.v(TAG, "SpeedCadenceSensor.deadSample - speed: " + mDeadSpeedSamples + " cadence: " + mDeadCadenceSamples);
		}
	}
}
