package no.turan.live.android;

import static no.turan.live.Constants.SAMPLE_EXERCISE_KEY;
import static no.turan.live.Constants.SAMPLE_HR_KEY;
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
		Long TIME = intent.getLongExtra(SAMPLE_TIME_KEY, -1L);
		Short HR = intent.getShortExtra(SAMPLE_HR_KEY, (short) -1);
		
		if (intent.getExtras() != null) {
			Log.d(TAG, intent.getExtras().toString());
		}
		
		if (TIME > 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good TIME");
			try {
				json.put("time", TIME);
			} catch (JSONException e) {
				Log.e(TAG, "UploadService.onHandleIntent - Error adding TIME to JSON", e);
			}
		}
		if (HR >= 0) {
			Log.d(TAG, "UploadService.onHandleIntent - good HR");
			try {
				json.put("hr", HR);
			} catch (JSONException e) {
				Log.e(TAG, "UploadService.onHandleIntent - Error adding HR to JSON", e);
			}
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

}
