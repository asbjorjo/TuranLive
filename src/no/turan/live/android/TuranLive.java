package no.turan.live.android;

import static no.turan.live.Constants.TAG;
import no.turan.live.android.services.RecordingService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TuranLive extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onRecordStart(View view) {
    	Log.d(TAG, "onRecordStart");
    	Intent startIntent = new Intent(this, RecordingService.class);
    	startService(startIntent);	    	
    }
    
    public void onAntEnable(View view) {
    	Log.d(TAG, "onAntEnable");
    }
}