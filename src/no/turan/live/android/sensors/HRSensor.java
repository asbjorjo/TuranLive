package no.turan.live.android.sensors;

import static no.turan.live.Constants.SAMPLE_HR_KEY;
import static no.turan.live.Constants.TAG;
import no.turan.live.Constants;
import android.content.Intent;
import android.util.Log;

import com.wahoofitness.api.WFHardwareConnectorTypes.WFSensorType;
import com.wahoofitness.api.data.WFHeartrateData;


public class HRSensor extends Sensor implements IHRSensor {
	public HRSensor() {
		super(WFSensorType.WF_SENSORTYPE_HEARTRATE);
	}
	
	@Override
	public int getHR() {
		Log.d(TAG, "HRSensor.getValue");
		int hr = -1;
		
		if (mSensor != null && mSensor.isConnected()) {
			Log.d(TAG, "HRSensor.getValue - good sensor");
			WFHeartrateData data = (WFHeartrateData) mSensor.getData();
			
			if (data.timestamp != mPreviousSampleTime) {
				Log.d(TAG, "HRSensor.getValue - good data");
				hr = data.computedHeartrate;
				mPreviousSampleTime = data.timestamp;
				mDeadSamples = 0;
			} else {
				deadSample();
			}
		} else {
			Log.d(TAG, "HRSensor.getValue - no HRSensor");
			connectSensor();
		}
		return hr;
	}

	@Override
	public void retrieveData(Intent intent) {
		Log.d(TAG, "HRSensor.retrieveData");
		int HR = getHR();
		if (HR >= 0) {
			Log.d(TAG, "HRSensor.retrieveData - good data");
			intent.putExtra(SAMPLE_HR_KEY, HR);
		}
	}
}
