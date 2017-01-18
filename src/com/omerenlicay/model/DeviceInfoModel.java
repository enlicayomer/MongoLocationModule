package com.omerenlicay.model;

public class DeviceInfoModel {
	
	/*
	 * cihaz bssid
	 */
	String deviceBssid;
	
	/*
	 * cihaz ssid
	 */
	String deviceSsid;
	
	/*
	 * bagli cihaz
	 */
	String isConnected;
	
	
	/*
	 * cihaz sinyali
	 */
	String signalRate;

	

	public String getDeviceBssid() {
		return deviceBssid;
	}


	public void setDeviceBssid(String deviceBssid) {
		this.deviceBssid = deviceBssid;
	}


	public String getDeviceSsid() {
		return deviceSsid;
	}


	public void setDeviceSsid(String deviceSsid) {
		this.deviceSsid = deviceSsid;
	}


	public String getIsConnected() {
		return isConnected;
	}


	public void setIsConnected(String isConnected) {
		this.isConnected = isConnected;
	}


	public String getSignalRate() {
		return signalRate;
	}


	public void setSignalRate(String signalRate) {
		this.signalRate = signalRate;
	}


	@Override
	public String toString() {
		return "DeviceInfoModel [deviceBssid=" + deviceBssid + ", deviceSsid=" + deviceSsid + ", isConnected="
				+ isConnected + ", signalRate=" + signalRate + "]";
	}
	
	
	
	

}
