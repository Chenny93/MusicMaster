package com.android.musicclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.android.musicclient.R;

public class WelcomeActivity extends Activity {
	
	private TextView tvShow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		tvShow = (TextView) findViewById(R.id.tv_Show);
		tvShow.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/test.ttf"));

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
				WelcomeActivity.this.finish();
			}
		},4000);
	}
}
