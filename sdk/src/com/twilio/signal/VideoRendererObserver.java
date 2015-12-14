package com.twilio.signal;

import android.util.Log;

public interface VideoRendererObserver {

	public void onFirstFrame();

	public void onFrameDimensionsChanged(int width, int height);

}

