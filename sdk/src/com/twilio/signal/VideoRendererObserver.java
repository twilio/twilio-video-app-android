package com.twilio.signal;

import android.util.Log;

public interface VideoRendererObserver {

	public void onFirstFrame();

	public void onFrameSizeChanged(int width, int height);

}

