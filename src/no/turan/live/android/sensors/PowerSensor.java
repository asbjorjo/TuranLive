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

public class PowerSensor extends Sensor implements IPowerSensor, ICadenceSensor {
	public PowerSensor() {
		super(WFSensorType.WF_SENSORTYPE_BIKE_POWER);
	}

	@Override
	public int getPower() {
		int power = -1;

		if (mSensor != null && mSensor.isConnected()) {
			WFBikePowerRawData rawData = (WFBikePowerRawData) mSensor.getRawData();
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
				timestamp = ctfdata.timestamp;
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
			if (timestamp != mPreviousSampleTime) {
				power = (int) newPower;
				mPreviousSampleTime = timestamp;
				mDeadSamples = 0;
			} else {
				deadSample();
			}
		} else {
			Log.w(TAG, "PowerSensor.getPower - no sensor");
		}
		
		return power;
	}

	@Override
	public int getCadence() {
		Log.v(TAG, "PowerSensor.getCadence");
		int cadence = -1;
		
		if (mSensor != null && mSensor.isConnected()) {
			WFBikePowerRawData rawData = (WFBikePowerRawData) mSensor.getRawData();
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
			if (mDeadSamples == 0) {
				Log.d(TAG, "PowerSensor.getCadence - last power sapmle was good");
				cadence = (int) newCadence;
			}
		} else {
			Log.w(TAG, "PowerSensor.getCadence - no sensor");
		}
		
		return cadence;
	}

	@Override
	public void retrieveData(Intent intent) {
		int power = getPower();
		int cadence = getCadence();
		
		if (power >= 0) {
			intent.putExtra(SAMPLE_POWER_KEY, power);
		}
		if (cadence >= 0) {
			intent.putExtra(Constants.SAMPLE_CADENCE_KEY, cadence);
		}
	}

	@Override
	protected void connectSensor() {
		super.connectSensor();
		if (mSensor != null && mSensor.isConnected()) {
			WFBikePowerConnection bpc = (WFBikePowerConnection) mSensor;
			WFBikePowerData bpd = bpc.getBikePowerData();
			Log.d(TAG, "PowerSensor.connectSensor - " + bpd.sensorType);
		}
	}
}
