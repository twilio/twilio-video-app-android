package com.twilio.signal;

import android.view.ViewGroup;
import android.opengl.GLSurfaceView;

public interface Media {

	/** Read-only representation of the current video container. */
	public ViewGroup getContainerView();

	public void attachContainerView(ViewGroup container);

}
