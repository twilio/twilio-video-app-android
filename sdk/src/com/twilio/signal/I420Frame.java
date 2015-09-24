package com.twilio.signal;

import java.nio.ByteBuffer;

public interface I420Frame {

	public int getWidth();

	public int getHeight();

	public int[] getYuvStrides();

	public ByteBuffer[] getYuvPlanes();

}

