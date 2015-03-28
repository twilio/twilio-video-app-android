package com.twilio.example.testcore;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignalPhoneActivity extends Activity {
	
	SignalPhone phone;
	private EditText clientNameTextBox;
	private Button register;
	private static final String DEFAULT_CLIENT_NAME = "jenny";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		this.clientNameTextBox = (EditText)findViewById(R.id.client_name);
		this.clientNameTextBox.setText(DEFAULT_CLIENT_NAME);
		this.register = (Button)findViewById(R.id.register);
		this.phone = SignalPhone.getInstance(getApplicationContext());
		this.register.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {		
				SignalPhoneActivity.this.phone.login(clientNameTextBox.getText().toString());
			}
		});
	}
}
