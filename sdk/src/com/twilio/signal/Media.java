package com.twilio.signal;

import android.view.TextureView;

public interface Media {
	
	
	/** Read-only representation of the current UIView video container. */
	public TextureView getView();
	
	public void attachView(TextureView view);

}
