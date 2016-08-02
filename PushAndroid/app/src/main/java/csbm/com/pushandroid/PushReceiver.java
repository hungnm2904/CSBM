package csbm.com.pushandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class PushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		System.out.println("EXTRAS " + extras.toString());
		String message = extras != null ? extras.getString("com.parse.Data")
				: "";
		JSONObject jObject;
		try {
			jObject = new JSONObject(message);
			Toast toast = Toast.makeText(context, jObject.getString("alert")
					+ jObject.getString("title"), Toast.LENGTH_SHORT);
			toast.show();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
