package com.twilio.signal;

import android.opengl.GLSurfaceView;

public interface Media {
	
	/** Read-only representation of the current UIView video container. */
	public GLSurfaceView[] getViews();
	
	public void attachViews(GLSurfaceView[] views);

}
