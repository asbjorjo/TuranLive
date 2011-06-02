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
		Log.v(TAG, "HRSensor.getHR");
		int hr = -1;
		
		if (mSensor != null && mSensor.isConnected()) {
			Log.v(TAG, "HRSensor.getHR - good sensor");
			WFHeartrateData data = (WFHeartrateData) mSensor.getData();
			
			Log.i(TAG, "HRSensor.getHR - " + data.timestamp + " - " + data.computedHeartrate);

			if (data.timestamp == 0) {
				Log.d(TAG, "HRSensor.getHR - null time");
				deadSample();
			} else if (data.timestamp != mPreviousSampleTime) {
				Log.d(TAG, "HRSensor.getHR - good data");
				hr = data.computedHeartrate;
				mPreviousSampleTime = data.timestamp;
				mDeadSamples = 0;
			} else {
				deadSample();
				if (mDeadSamples < 2) {
					Log.d(TAG, "HRSensor.getHR - first dead sample");
					hr = data.computedHeartrate;
				}
			}
		} else {
			Log.w(TAG, "HRSensor.getHR - no HRSensor");
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
}
