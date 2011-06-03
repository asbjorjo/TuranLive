package no.turan.live.android.sensors;

import static no.turan.live.android.Constants.SAMPLE_POWER_KEY;
import static no.turan.live.android.Constants.TAG;
import no.turan.live.android.Constants;
import android.content.Intent;
import android.util.Log;

import com.wahoofitness.api.WFHardwareConnectorTypes.WFSensorType;
import com.wahoofitness.api.comm.WFBikePowerConnection;
import com.wahoofitness.api.data.WFBikePowerCTFData;
import com.wahoofitness.api.data.WFBikePowerCrankTorqueData;
import com.wahoofitness.api.data.WFBikePowerData;
import com.wahoofitness.api.data.WFBikePowerPowerOnlyData;
import com.wahoofitness.api.data.WFBikePowerRawData;
import com.wahoofitness.api.data.WFBikePowerWheelTorqueData;

public class PowerSensor extends Sensor implements IPowerSensor, ICadenceSensor, ISpeedSensor {
	private int nullValues_ = 0;
	
	public PowerSensor() {
		super(WFSensorType.WF_SENSORTYPE_BIKE_POWER);
	}

	@Override
	public int getPower() {
		int power = -1;

		if (sensor_ != null && sensor_.isConnected()) {
			WFBikePowerRawData rawData = (WFBikePowerRawData) sensor_.getRawData();
			long timestamp = -1;
			long newPower = -1;
			/*
			 * Check which type of sensor is connected and get data accordingly.
			 */
			switch (rawData.sensorType) {
			case WF_BIKE_POWER_TYPE_UNIDENTIFIED:
				Log.d(TAG, "PowerSensor.getPower - unidentified");
				break;
			case WF_BIKE_POWER_TYPE_CTF:
				Log.d(TAG, "PowerSensor.getPower - crank torque frequency");
				WFBikePowerCTFData ctfdata = rawData.crankTorqueFreqData;
				timestamp = ctfdata.accumulatedCrankTicks;
				newPower = ctfdata.averagePower;
				break;
			case WF_BIKE_POWER_TYPE_CRANK_TORQUE:
				Log.d(TAG, "PowerSensor.getPower - crank torque");
				WFBikePowerCrankTorqueData ctdata = rawData.crankTorqueData;
				timestamp = ctdata.accumulatedCrankTicks;
				newPower = ctdata.averagePower;
				break;
			case WF_BIKE_POWER_TYPE_WHEEL_TORQUE:
				Log.d(TAG, "PowerSensor.getPower - wheel torque");
				WFBikePowerWheelTorqueData wtdata = rawData.wheelTorqueData;
				timestamp = wtdata.accumulatedWheelTicks;
				newPower = wtdata.averagePower;
				break;
			case WF_BIKE_POWER_TYPE_POWER_ONLY:
				Log.d(TAG, "PowerSensor.getPower - power only");
				WFBikePowerPowerOnlyData powerdata = rawData.powerOnlyData;
				timestamp = powerdata.eventCount;
				newPower = powerdata.instantPower;
				break;
			}
			if (timestamp != previousSampleTime_) {
				power = (int) newPower;
				previousSampleTime_ = timestamp;
				deadSamples_ = 0;
			} else {
				deadSample();
				if (deadSamples_ < Constants.POWER_DEAD_SAMPLES_ACCEPTED) {
					Log.v(TAG, "PowerSensor.getPower - first dead sample");
					power = (int) newPower;
				}
			}
		} else {
			Log.w(TAG, "PowerSensor.getPower - no sensor");
		}
		
		Log.d(TAG, "PowerSensor.getPower - " + power);
		return power;
	}

	@Override
	public int getCadence() {
		Log.v(TAG, "PowerSensor.getCadence");
		int cadence = -1;
		
		if (sensor_ != null && sensor_.isConnected()) {
			WFBikePowerRawData rawData = (WFBikePowerRawData) sensor_.getRawData();
			long timestamp = -1;
			long newCadence = -1;
			/*
			 * Check which type of sensor is connected and get data accordingly.
			 */
			switch (rawData.sensorType) {
			case WF_BIKE_POWER_TYPE_UNIDENTIFIED:
				Log.d(TAG, "PowerSensor.getCadence - unidentified");
				break;
			case WF_BIKE_POWER_TYPE_CTF:
				Log.d(TAG, "PowerSensor.getCadence - crank torque frequency");
				WFBikePowerCTFData ctfdata = rawData.crankTorqueFreqData;
				timestamp = ctfdata.timestamp;
				newCadence = ctfdata.averageCadence;
				break;
			case WF_BIKE_POWER_TYPE_CRANK_TORQUE:
				Log.d(TAG, "PowerSensor.getCadence - crank torque");
				WFBikePowerCrankTorqueData ctdata = rawData.crankTorqueData;
				timestamp = ctdata.accumulatedCrankTicks;
				if (ctdata.instantCadence != 0xFF) {
					newCadence = ctdata.instantCadence;
				} else {
					newCadence = ctdata.averageCadence;
				}
				break;
			case WF_BIKE_POWER_TYPE_WHEEL_TORQUE:
				Log.d(TAG, "PowerSensor.getCadence - wheel torque");
				WFBikePowerWheelTorqueData wtdata = rawData.wheelTorqueData;
				timestamp = wtdata.accumulatedWheelTicks;
				if (wtdata.instantCadence != 0xFF) {
					newCadence = wtdata.instantCadence;
				}
				break;
			case WF_BIKE_POWER_TYPE_POWER_ONLY:
				Log.d(TAG, "PowerSensor.getCadence - power only");
				WFBikePowerPowerOnlyData powerdata = rawData.powerOnlyData;
				timestamp = powerdata.eventCount;
				if (powerdata.instantCadence != 0xFF) {
					newCadence = powerdata.instantCadence;
				}
				break;
			}
			if (deadSamples_ < Constants.POWER_DEAD_SAMPLES_ACCEPTED) {
				Log.d(TAG, "PowerSensor.getCadence - last power sapmle was good");
				cadence = (int) newCadence;
			}
		} else {
			Log.w(TAG, "PowerSensor.getCadence - no sensor");
		}
		
		return cadence;
	}

	@Override
	public int getSpeed() {
		Log.v(TAG, "PowerSensor.getSpeed");
		int speed = -1;
		
		if (sensor_ != null && sensor_.isConnected()) {
			WFBikePowerRawData rawData = (WFBikePowerRawData) sensor_.getRawData();
			long timestamp = -1;
			int newSpeed = -1;
			/*
			 * Check which type of sensor is connected and get data accordingly.
			 */
			switch (rawData.sensorType) {
			case WF_BIKE_POWER_TYPE_UNIDENTIFIED:
				Log.d(TAG, "PowerSensor.getSpeed - unidentified");
				break;
			case WF_BIKE_POWER_TYPE_CTF:
				Log.d(TAG, "PowerSensor.getSpeed - crank torque frequency");
				break;
			case WF_BIKE_POWER_TYPE_CRANK_TORQUE:
				Log.d(TAG, "PowerSensor.getSpeed - crank torque");
				break;
			case WF_BIKE_POWER_TYPE_WHEEL_TORQUE:
				Log.d(TAG, "PowerSensor.getSpeed - wheel torque");
				WFBikePowerWheelTorqueData wtdata = rawData.wheelTorqueData;
				timestamp = wtdata.accumulatedWheelTicks;
				newSpeed = wtdata.wheelRPM;
				break;
			case WF_BIKE_POWER_TYPE_POWER_ONLY:
				Log.d(TAG, "PowerSensor.getSpeed - power only");
				break;
			}
			if (deadSamples_ < Constants.POWER_DEAD_SAMPLES_ACCEPTED) {
				Log.d(TAG, "PowerSensor.getSpeed - last power sapmle was good");
				speed = newSpeed;
			}
		} else {
			Log.w(TAG, "PowerSensor.getSpeed - no sensor");
		}
		
		return speed;
	}

	@Override
	public void retrieveData(Intent intent) {
		int power = getPower();
		int cadence = getCadence();
		int speed = getSpeed();
		
		if (power >= 0) {
			Log.v(TAG, "PowerSensor.retrieveData - good power");
			intent.putExtra(SAMPLE_POWER_KEY, power);
			nullValues_ = 0;
		}
		if (cadence >= 0) {
			intent.putExtra(Constants.SAMPLE_CADENCE_KEY, cadence);
		}
		if (speed >= 0) {
			intent.putExtra(Constants.SAMPLE_SPEED_KEY, speed);
		}
	}

	@Override
	public void retrieveData(SensorData sensorData) {
		int power = getPower();
		int cadence = getCadence();
		int speed = getSpeed();

		if (power >= 0) {
			Log.v(TAG, "PowerSensor.retrieveData - good power");
			sensorData.setPower(power);
			nullValues_ = 0;
		}
		if (cadence >= 0) {
			sensorData.setCadence(cadence);
		}
		if (speed >= 0) {
			sensorData.setSpeed(speed);
		}
	}

	@Override
	public void connectSensor() {
		super.connectSensor();
		if (sensor_ != null && sensor_.isConnected()) {
			WFBikePowerConnection bpc = (WFBikePowerConnection) sensor_;
			WFBikePowerData bpd = bpc.getBikePowerData();
			Log.d(TAG, "PowerSensor.connectSensor - " + bpd.sensorType);
		}
	}

}
