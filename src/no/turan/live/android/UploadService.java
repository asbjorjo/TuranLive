package no.turan.live.android;

import static no.turan.live.Constants.SAMPLE_ALTITUDE_KEY;
import static no.turan.live.Constants.SAMPLE_CADENCE_KEY;
import static no.turan.live.Constants.SAMPLE_EXERCISE_KEY;
import static no.turan.live.Constants.SAMPLE_HR_KEY;
import static no.turan.live.Constants.SAMPLE_LATITUDE_KEY;
import static no.turan.live.Constants.SAMPLE_LONGITUDE_KEY;
import static no.turan.live.Constants.SAMPLE_POWER_KEY;
import static no.turan.live.Constants.SAMPLE_SPEED_KEY;
import static no.turan.live.Constants.SAMPLE_TIME_KEY;
import static no.turan.live.Constants.TAG;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import no.turan.live.Constants;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UploadService extends IntentService {
	public UploadService() {
		super("TuranService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "UploadService.onHandleIntent");
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		
		Integer exerciseId = intent.getIntExtra(SAMPLE_EXERCISE_KEY, -1);
		long time = intent.getLongExtra(SAMPLE_TIME_KEY, -1L);
		int hr = intent.getIntExtra(SAMPLE_HR_KEY, -1);
		int power = intent.getIntExtra(SAMPLE_POWER_KEY, -1);
		int cadence = intent.getIntExtra(SAMPLE_CADENCE_KEY, -1);
		float speed = intent.getFloatExtra(SAMPLE_SPEED_KEY, -1);
		double altitude = intent.getDoubleExtra(SAMPLE_ALTITUDE_KEY, -1);
		double latitude = intent.getDoubleExtra(SAMPLE_LATITUDE_KEY, -1);
		double longitude = intent.getDoubleExtra(SAMPLE_LONGITUDE_KEY, -1);
		
		
		if (intent.getExtras() != null) {
			Log.d(TAG, intent.getExtras().toString());
		}
		
		if (time > 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good TIME");
			addToJSON(json, time, "time");
		}
		if (hr >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good HR");
			addToJSON(json, hr, "hr");
		}
		if (speed >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good SPEED");
			addToJSON(json, speed, "speed");
		}
		if (cadence >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good CADENCE");
			addToJSON(json, cadence, "cadence");
		}
		if (power >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good POWER");
			addToJSON(json, power, "power");
		}
		if (altitude >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good ALTITUDE");
			addToJSON(json, altitude, "altitude");
		}
		if (latitude >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good LATITUDE");
			addToJSON(json, latitude, "lat");
		}
		if (longitude >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good LONGITUDE");
			addToJSON(json, longitude, "lon");
		}
		
		jsonArray.put(json);

		if (exerciseId > 0) {
			try {
				String URL = "http://turan.no/exercise/update/live/" + exerciseId;
				Log.d(TAG, "UploadService.onHandleIntent - Posting update to " + URL);
				Log.d(TAG, jsonArray.toString());
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(URL);
				StringEntity se = new StringEntity(jsonArray.toString());
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				post.setEntity(se);
				client.execute(post);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UploadService.onHandleIntent - StringEntity failed", e);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "UploadService.onHandleIntent - Error while posting data", e);
			} catch (IOException e) {
				Log.e(TAG, "UploadService.onHandleIntent - Error while posting data", e);
			}
		} else {
			Log.d(TAG, "UploadService.onHandleIntent - invalid excerciseId - " + exerciseId);
		}
	}

	private void addToJSON(JSONObject json, Object value, String key) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Log.e(TAG, "UploadService.onHandleIntent - Error adding HR to JSON", e);
		}
	}
}
