package com.twilio.signal;

import android.graphics.SurfaceTexture;

public interface Media {
	
	/** Read-only representation of the current UIView video container. */
	public SurfaceTexture[] getViews();
	
	public void attachViews(SurfaceTexture[] views);

}
