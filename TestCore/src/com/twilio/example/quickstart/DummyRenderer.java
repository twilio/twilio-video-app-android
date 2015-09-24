package com.twilio.example.quickstart; 

import com.twilio.signal.VideoRenderer;
import com.twilio.signal.I420Frame;
import android.util.Log;

public class DummyRenderer implements VideoRenderer {
	String name;

	public DummyRenderer(String name) {
		this.name = name;
	}

	@Override
	public void setSize(int width, int height) {
		Log.i("Dummy", name + " setSize " + width + " " + height);
	}

	@Override
	public void renderFrame(I420Frame frame) {
		Log.i("Dummy", name + " renderFrame");
	}

}
