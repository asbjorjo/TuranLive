package no.turan.live.android.sensors;

import android.content.Intent;

import com.wahoofitness.api.WFHardwareConnectorTypes.WFSensorType;
import com.wahoofitness.api.data.WFBikeSpeedCadenceData;

public class SpeedCadenceSensor extends Sensor implements ICadenceSensor,
		ISpeedSensor {
	public SpeedCadenceSensor() {
		super(WFSensorType.WF_SENSORTYPE_BIKE_SPEED_CADENCE);
	}
	
	@Override
	public int getSpeed() {
		int speed = -1;
		return speed;
	}

	@Override
	public int getCadence() {
		int cadence = -1;
		return cadence;
	}

	@Override
	public void retrieveData(Intent intent) {
		// TODO Auto-generated method stub
		
	}

}
