package com.twilio.example.testcore;

import com.twilio.signal.Test;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TestActivity extends Activity {
	
	SignalPhone phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		Button button = (Button)findViewById(R.id.load);
		phone = SignalPhone.getInstance(getApplicationContext());
		button.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {		
				TestActivity.this.phone.login("Kumkum");
			}
		});
	}
}
