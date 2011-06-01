package no.turan.live.android.sensors;

import android.content.Intent;

import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.comm.WFSensorConnection;

public interface ISensor extends WFSensorConnection.Callback {
	public int getSensorId();
	public void setupSensor(WFHardwareConnector hardwareConnector);
	public void retrieveData(Intent intent);
}
