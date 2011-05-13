package no.turan.live.android.services;

import static no.turan.live.Constants.TAG;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.turan.live.android.R;
import no.turan.live.android.TuranLive;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RecordingService extends Service {
		
	
	private ExecutorService executorService;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "RecordingService.onCreate");
	    showNotification();
	    executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Shows the notification message and icon in the notification bar.
	 */
	public void showNotification() {
		Notification notification = new Notification(
				R.drawable.icon, null /* tickerText */,
				System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(
				this, 0 /* requestCode */, new Intent(this, TuranLive.class),
				0 /* flags */);
		notification.setLatestEventInfo(this, getString(R.string.app_name),
				getString(R.string.app_name), contentIntent);
		notification.flags += Notification.FLAG_NO_CLEAR;
		startForeground(1, notification);
	}
}
