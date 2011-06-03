package no.turan.live.android;

import static no.turan.live.android.Constants.TAG;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class TuranPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "TuranPreferences.onCreate");
		
		//PreferenceManager.setDefaultValues(this, SETTINGS_NAME, MODE_PRIVATE, R.xml.preferences, false);
		addPreferencesFromResource(R.xml.preferences);
	}
}
