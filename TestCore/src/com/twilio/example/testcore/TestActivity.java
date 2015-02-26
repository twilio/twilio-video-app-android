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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		Button button = (Button)findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView text1 = (TextView)findViewById(R.id.text1);
				Test test = Test.getInstance();
				if (!test.isSignalCoreInitialized()) {
					text1.setText("Initializing");
					test.initSignalCore();
				} else {
					text1.setText("IT WORKS !!!!!");
				}
				
			}
		});
	}
}
