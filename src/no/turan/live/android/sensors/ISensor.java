package no.turan.live.android.sensors;

import android.content.Intent;

import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.comm.WFSensorConnection;

public interface ISensor extends WFSensorConnection.Callback {
	public int getSensorId();
	public void disconnectSensor();
	public void setupSensor(WFHardwareConnector hardwareConnector);
	@Deprecated
	public void retrieveData(Intent intent);
	public void retrieveData(SensorData sensorData);
}
