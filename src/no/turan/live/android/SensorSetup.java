package no.turan.live.android;

import static no.turan.live.Constants.TAG;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class SensorSetup extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "SensorSetup.onCreate");
    				
        if(!this.isFinishing())
        {
        	setContentView(R.layout.sensors);
        }
	}
	
    /* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy enter");

		super.onDestroy();

		Log.d(TAG, "onDestroy exit");
	}
	
	public void onEnableHR(View view) {
		Log.d(TAG, "onEnableHR");
		setButtonClickable(R.id.pairHR, ((CheckBox)view).isChecked());
	}
	
	public void onPairHR(View view) {
		Log.d(TAG, "onPairHR");
		startActivity(new Intent(this, PairHRSensor.class));
	}
	
	public void onEnableCadence(View view) {
		Log.d(TAG, "onEnableCadence");
		CheckBox cadBox = (CheckBox) findViewById(R.id.enableCadence);
		CheckBox spdBox = (CheckBox) findViewById(R.id.enableSpeed);
		
		if(cadBox.isChecked() || spdBox.isChecked()) {
			setBoxClickable(R.id.enableCadSpd, false);
		} else {
			setBoxClickable(R.id.enableCadSpd, true);
		}
		
		setButtonClickable(R.id.pairSpeed, spdBox.isChecked());
		setButtonClickable(R.id.pairCadence, cadBox.isChecked());
	}

	public void onEnableCadSpd(View view) {
		Log.d(TAG, "onEnableCadSpd");
		toggleBoxClickable(R.id.enableCadence);
		toggleBoxClickable(R.id.enableSpeed);
		setButtonClickable(R.id.pairCadSpd, ((CheckBox)view).isChecked());
	}
	public void onEnableSRM(View view) {
		Log.d(TAG, "onEnableSRM");
		setButtonClickable(R.id.pairSRM, ((CheckBox)view).isChecked());
	}
	
	private void toggleBoxClickable(Integer id) {
		CheckBox box = (CheckBox)findViewById(id);
		box.setClickable(!box.isClickable());
	}
	
	private void setBoxClickable(Integer id, boolean clickable) {
		CheckBox box = (CheckBox) findViewById(id);
		box.setClickable(clickable);
	}

	private void setButtonClickable(Integer id, boolean clickable) {
		Button box = (Button) findViewById(id);
		box.setClickable(clickable);
	}
}
