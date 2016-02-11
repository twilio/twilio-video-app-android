package com.twilio.conversations.impl.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.twilio.conversations.TwilioConversations;


public final class PlatformInfo {

        private static final String PLATFORM_NAME = "Android";

        private PlatformInfo(){
        }

        public static String getPlatfomName() {
            return PLATFORM_NAME;
        }

        public static String getPlatformVersion() {
        	return android.os.Build.VERSION.RELEASE;
        }

        public static String getHwDeviceManufacturer() {
        	return android.os.Build.MANUFACTURER;
        }

        public static String getHwDeviceModel() {
        	return android.os.Build.MODEL;
        }

        public static String getHwDeviceUUID() {
        	return android.provider.Settings.Secure.ANDROID_ID;
        }

        public static String getHwDeviceIPAddress() {
        	try {
        		for (Enumeration<NetworkInterface> en =
        				NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
        			NetworkInterface intf = en.nextElement();
        			for (Enumeration<InetAddress> enumIpAddr =
        					intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
        				InetAddress inetAddress = enumIpAddr.nextElement();
        				if (!inetAddress.isLoopbackAddress() &&
        						InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
        					String ipaddress = inetAddress .getHostAddress().toString();
        					return ipaddress;
        				}
        			}
        		}
        	} catch (SocketException ex) {
        		// TODO: Ignoring exception for now
        	}
        	return "";
        }

        public static String getHwDeviceConnectionType(Context context) {
        	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        	NetworkInfo info = cm.getActiveNetworkInfo();
        	String connType = "";
        	if(info==null || !info.isConnected()) {
        		connType = "No connection"; //not connected
        	} else if(info.getType() == ConnectivityManager.TYPE_WIFI) {
        		connType = info.getTypeName();
        	} else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
        		connType = info.getSubtypeName();
        	} else {
        		connType = "Unable to detect connection type";
        	}
        	return connType;
        }

        public static int getHwDeviceNumCores() {
        	return Runtime.getRuntime().availableProcessors();
        }

        public static double getTimeStamp() {
        	return System.currentTimeMillis() / 1000L;
        }

        public static String getRtcPlatformSdkVersion() {
        	return TwilioConversations.getVersion();
        }

        public static String getOsArch() {
        	return System.getProperty("os.arch");
        }

}
