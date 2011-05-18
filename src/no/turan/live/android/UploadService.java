package no.turan.live.android;

import static no.turan.live.Constants.TAG;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
		Log.d(TAG, "TuranService.onHandleIntent");
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		
		String exerciseId = intent.getStringExtra("no.turan.live.android.EXERCISE");
		Long TIME = intent.getLongExtra("no.turan.live.android.TIME", -1L);
		Short HR = intent.getShortExtra("no.turan.live.android.HR", (short) -1);
		
		Log.d(TAG, intent.toString());
		
		if (TIME > 0) {
			try {
				json.put("time", TIME);
			} catch (JSONException e) {
				Log.e(TAG, "Error adding TIME to JSON", e);
			}
		}
		
		if (HR >= 0) {
			try {
				json.put("hr", HR);
			} catch (JSONException e) {
				Log.e(TAG, "Error adding HR to JSON", e);
			}
		}
		
		jsonArray.put(json);

		if (exerciseId != null) {
			try {
				String URL = "http://turan.no/exercise/update/live/" + exerciseId;
				Log.d(TAG, "Posting update to " + URL);
				Log.d(TAG, jsonArray.toString());
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(URL);
				StringEntity se = new StringEntity(jsonArray.toString());
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				post.setEntity(se);
				client.execute(post);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "StringEntity failed", e);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "Error while posting data", e);
			} catch (IOException e) {
				Log.e(TAG, "Error while posting data", e);
			}
		} else {
			Log.d(TAG, "jsonString not found in intent");
		}
	}

}
