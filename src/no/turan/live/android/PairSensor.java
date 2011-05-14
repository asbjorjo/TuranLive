package no.turan.live.android;

import static no.turan.live.Constants.TAG;

import com.wahoofitness.api.WFHardwareConnector;
import com.wahoofitness.api.comm.WFConnectionParams;
import com.wahoofitness.api.comm.WFSensorConnection;
import com.wahoofitness.api.comm.WFSensorConnection.WFSensorConnectionStatus;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public abstract class PairSensor extends Activity implements WFSensorConnection.Callback {
	protected WFHardwareConnector mHardwareConnector;
	protected WFSensorConnection mConnection;
	protected short mSensorType;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pairing);
	}

	public WFSensorConnectionStatus getState() {
		Log.d(TAG, "PairSensor.getState");
		WFSensorConnectionStatus retVal = WFSensorConnectionStatus.WF_SENSOR_CONNECTION_STATUS_IDLE;
		if (mConnection != null) {
			Log.d(TAG, "Has Connection");
			retVal = mConnection.getConnectionStatus();
		}
		Log.d(TAG, retVal.name());
		return retVal;
	}

	public void onClick(View v) {
		Log.d(TAG, "PairSensor.onClick");
		switch( getState() )
		{
		case WF_SENSOR_CONNECTION_STATUS_IDLE:
			Log.d(TAG, "Connect Sensor");
			connectSensor();
			break;
		case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
		case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
		case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
			Log.d(TAG, "Disconnect Sensor");
			disconnectSensor();
			break;              
    	}
    }

	public void connectionStateChanged(WFSensorConnectionStatus connState) {
		if ( mConnection != null && !mConnection.isValid() ) {
			mConnection.setCallback(null);
			mConnection = null;
		}
		
	}

	protected boolean connectSensor() {
		Log.d(TAG, "PairSensor.connectSensor");
		
		boolean retVal = (mHardwareConnector != null);
		Log.d(TAG, ""+retVal);
		
		if (retVal) {
			// set the button state based on the connection state.
			switch ( getState() )
			{
				case WF_SENSOR_CONNECTION_STATUS_IDLE:
				{
	        		WFConnectionParams connectionParams = new WFConnectionParams();
	        		connectionParams.sensorType = mSensorType;
	        		//connectionParams.device1 = new WFDeviceParams((short)100, (byte)1);
	        		//connectionParams.device2 = new WFDeviceParams((short)200, (byte)1);
	        		mConnection = mHardwareConnector.initSensorConnection(connectionParams);
	        		if (mConnection != null) {
	        			mConnection.setCallback(this);
	        		}
					break;
				}
					
				case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
				case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
					// do nothing.
					break;
					
				case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
					retVal = false;
					break;
			}
		}
		Log.d(TAG, ""+retVal);
		return retVal;
	}

	protected boolean disconnectSensor() {
		Log.d(TAG, "PairSensor.disconnectSensor");
		boolean retVal = (mConnection != null);
		
		if (retVal) {
			// set the button state based on the connection state.
			switch ( getState() )
			{
				case WF_SENSOR_CONNECTION_STATUS_IDLE:
				{
					if (mConnection != null) {
						mConnection.setCallback(null);
						mConnection = null;
					}
					break;
				}
					
				case WF_SENSOR_CONNECTION_STATUS_CONNECTING:
				case WF_SENSOR_CONNECTION_STATUS_CONNECTED:
				case WF_SENSOR_CONNECTION_STATUS_DISCONNECTING:
	        		mConnection.disconnect();
					break;
			}
		}
		
		return retVal;
	}

	public void initControl(WFHardwareConnector hwConn) {
		Log.d(TAG, "PairSensor.initControl");
		mHardwareConnector = hwConn;
	}
	
	public boolean restoreConnectionState() {
		
		WFSensorConnection[] connections = mHardwareConnector.getSensorConnections(mSensorType);
		boolean retVal = (connections != null);
		if (retVal) {
			mConnection = connections[0];
			mConnection.setCallback(this);
		}
		
		return retVal;
	}
	
}
