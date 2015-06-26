package com.twilio.signal.impl;

import com.twilio.signal.EndpointListener;

public class EndpointListenerInternal implements NativeHandleInterface {
	
	private EndpointListener listener;
	private long nativeEndpointObserver;
	
	public EndpointListenerInternal(EndpointListener listener) {
		this.listener = listener;
		this.nativeEndpointObserver = wrapNativeObserver(listener);
	}
	
	private native long wrapNativeObserver(EndpointListener listener);
	//::TODO figure out when to call this - may be Endpoint.release() ??
	private native void freeNativeObserver(long nativeEndpointObserver);

	@Override
	public long getNativeHandle() {
		return nativeEndpointObserver;
	}
	
}
