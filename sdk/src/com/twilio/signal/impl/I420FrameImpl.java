package com.twilio.signal.impl;

import com.twilio.signal.I420Frame;
import java.nio.ByteBuffer;

public class I420FrameImpl implements I420Frame {
	protected int width;
	protected int height;
	protected int[] yuvStrides;
	protected ByteBuffer[] yuvPlanes;

	public I420FrameImpl(int width, int height, int[] yuvStrides, ByteBuffer[] yuvPlanes) {
		this.width = width;
		this.height = height;
		this.yuvStrides = yuvStrides;
		this.yuvPlanes = yuvPlanes;
	} 

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int[] getYuvStrides() {
		return yuvStrides;
	}

	public ByteBuffer[] getYuvPlanes() {
		return yuvPlanes;
	}

}
