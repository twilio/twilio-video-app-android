package com.twilio.video;

abstract class NativeHandle {

    private long nativeHandle;

    public NativeHandle(Object object) {
        this.nativeHandle = nativeCreate(object);
    }

    public long get() {
        return nativeHandle;
    }

    public void release() {
        if (nativeHandle != 0) {
            nativeFree(nativeHandle);
            nativeHandle = 0;
        }
    }

    abstract protected long nativeCreate(Object object);
    abstract protected void nativeFree(long nativeHandle);

}
