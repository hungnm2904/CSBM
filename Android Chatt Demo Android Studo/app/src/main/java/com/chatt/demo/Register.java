package com.chatt.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.ConstClass;
import com.chatt.demo.utils.GPSTracker;
import com.chatt.demo.utils.Utils;
import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEGeoPoint;
import com.csbm.BEObject;
import com.csbm.BEUser;
import com.csbm.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;


public class Register extends CustomActivity
{

	/** The username EditText. */
	private EditText user;

	/** The password EditText. */
	private EditText pwd;

	/** The email EditText. */
	private EditText email;

	private BEFile iconUser;

	GPSTracker gps;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		setTouchNClick(R.id.btnReg);

		user = (EditText) findViewById(R.id.user);
		pwd = (EditText) findViewById(R.id.pwd);
		email = (EditText) findViewById(R.id.email);


	}


	@Override
	public void onClick(View v)
	{
		super.onClick(v);

		final String u = user.getText().toString();
		String p = pwd.getText().toString();
		String e = email.getText().toString();
		if (u.length() == 0 || p.length() == 0 || e.length() == 0)
		{
			Utils.showDialog(this, R.string.err_fields_empty);
			return;
		}
		final ProgressDialog dia = ProgressDialog.show(this, null,
				getString(R.string.alert_wait));
		BEGeoPoint currentLocation = new BEGeoPoint(Const.DEFAULT_LATITUDE, Const.DEFAULT_LONGITUDE);
		final BEUser pu = new BEUser();
		pu.setEmail(e);
		pu.setPassword(p);
		pu.setUsername(u);
		pu.put(ConstClass.USER_LOCATION, currentLocation);
		pu.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(BEException e)
			{
				dia.dismiss();
				if (e == null)
				{
					// set default image for new user
					Bitmap imgSelected = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_icon);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					// Compress image to lower quality scale 1 - 100
					imgSelected.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] image = stream.toByteArray();
					BEFile file = new BEFile( image, "image/png");
					file.saveInBackground();
					pu.put(ConstClass.USER_PICTURE_PROFILE, file);
					pu.saveInBackground();
					// set friend for user
					BEObject object = new BEObject(ConstClass.FRIEND_LIST);
					object.put(ConstClass.FRIEND_LIST_CREATED_BY, pu);
					object.put(ConstClass.FRIEND_LIST_FRIENDS, Arrays.asList());
					object.saveInBackground();
					UserList.user = pu;
					startActivity(new Intent(Register.this, UserList.class));
					setResult(RESULT_OK);
					finish();

				}
				else
				{
					Utils.showDialog(
							Register.this,
							getString(R.string.err_singup) + " "
									+ e.getMessage());
					e.printStackTrace();
				}

			}
		});

	}
}
