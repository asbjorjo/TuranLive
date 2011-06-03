package no.turan.live.android;

import static no.turan.live.android.Constants.SAMPLE_EXERCISE_KEY;
import static no.turan.live.android.Constants.TAG;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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

public class TuranUploadService extends IntentService {
	public TuranUploadService() {
		super("TuranUploadService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "TuranUploadService.onHandleIntent");
		
		JSONArray jsonArray = new JSONArray();
		Integer exerciseId = intent.getIntExtra(SAMPLE_EXERCISE_KEY, -1);

		ArrayList<String> samples = intent.getStringArrayListExtra(Constants.SAMPLES_KEY);
		
		for (String sample : samples) {
			try {
				JSONObject jsonSample = new JSONObject(sample);
				jsonArray.put(jsonSample);	
			} catch (JSONException e) {
				Log.e(TAG, "TuranUploadService.onHandleIntent", e);
			}
		}

		if (exerciseId > 0) {
			try {
				String URL = "http://turan.no/exercise/update/live/" + exerciseId;
				Log.d(TAG, "TuranUploadService.onHandleIntent - Posting update to " + URL);
				Log.d(TAG, "TuranUploadService.onHandleIntent - " + jsonArray.toString());
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(URL);
				StringEntity se = new StringEntity(jsonArray.toString());
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				post.setEntity(se);
				client.execute(post);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "TuranUploadService.onHandleIntent - StringEntity failed", e);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "TuranUploadService.onHandleIntent - Error while posting data", e);
			} catch (IOException e) {
				Log.e(TAG, "TuranUploadService.onHandleIntent - Error while posting data", e);
			}
		} else {
			Log.d(TAG, "TuranUploadService.onHandleIntent - invalid excerciseId - " + exerciseId);
		}
	}
}
