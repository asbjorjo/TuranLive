package no.turan.live.android.sensors;

import static no.turan.live.android.Constants.SAMPLE_HR_KEY;
import static no.turan.live.android.Constants.TAG;
import android.content.Intent;
import android.util.Log;

import com.wahoofitness.api.WFHardwareConnectorTypes.WFSensorType;
import com.wahoofitness.api.data.WFHeartrateData;


public class HRSensor extends Sensor implements IHRSensor {
	public HRSensor() {
		super(WFSensorType.WF_SENSORTYPE_HEARTRATE);
	}
	
	public HRSensor(short sensorId) {
		super(WFSensorType.WF_SENSORTYPE_HEARTRATE, sensorId);
	}
	
	@Override
	public int getHR() {
		Log.v(TAG, "HRSensor.getHR");
		int hr = -1;
		
		if (sensor_ != null && sensor_.isConnected()) {
			Log.v(TAG, "HRSensor.getHR - good sensor");
			WFHeartrateData data = (WFHeartrateData) sensor_.getData();
			
			Log.d(TAG, "HRSensor.getHR - " + data.timestamp + " - " + data.accumBeatCount + " - " + data.computedHeartrate);

			if (data.timestamp == 0) {
				Log.v(TAG, "HRSensor.getHR - null time");
				deadSample();
			} else if (data.timestamp != previousSampleTime_) {
				Log.v(TAG, "HRSensor.getHR - good data");
				hr = data.computedHeartrate;
				previousSampleTime_ = data.timestamp;
				deadSamples_ = 0;
			} else {
				deadSample();
				if (deadSamples_ < 2) {
					Log.v(TAG, "HRSensor.getHR - first dead sample");
					hr = data.computedHeartrate;
				}
			}
		} else if (sensor_ != null) {
			Log.w(TAG, "HRSensor.getHR - sensor not connected");
			if (reconnectable_) {
				Log.d(TAG, "HRSensor.getHR - reconnecting");
				connectSensor();
			}
		} else {
			Log.w(TAG, "HRSensor.getHR - no sensor");
			Log.d(TAG, "HRSensor.getHR - reconnecting");
			connectSensor();
		}

		return hr;
	}

	@Override
	public void retrieveData(Intent intent) {
		Log.v(TAG, "HRSensor.retrieveData");
		int HR = getHR();
		if (HR >= 0) {
			Log.v(TAG, "HRSensor.retrieveData - good data - " + HR);
			intent.putExtra(SAMPLE_HR_KEY, HR);
		}
	}

	@Override
	public void retrieveData(SensorData sensorData) {
		int HR = getHR();
		
		if (HR >= 0) {
			Log.v(TAG, "HRSensor.retrieveData - good data - " + HR);
			sensorData.setHr(HR);
		}
	}
}
