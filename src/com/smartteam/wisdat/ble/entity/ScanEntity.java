package com.smartteam.wisdat.ble.entity;

import java.util.Arrays;

import android.bluetooth.BluetoothDevice;

public class ScanEntity {
	private String mac;
	private int rssi;
	private byte[] scanRecoder;
	private String name;
	private BluetoothDevice device;
	private String[] scanRecodestr;
	private int bindbyte;
	private int passCode;
	
	public int getBindbyte() {
		return bindbyte;
	}
	public void setBindbyte(int bindbyte) {
		this.bindbyte = bindbyte;
	}
	public int getPassCode() {
		return passCode;
	}
	public void setPassCode(int passCode) {
		this.passCode = passCode;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	public byte[] getScanRecoder() {
		return scanRecoder;
	}
	public void setScanRecoder(byte[] scanRecoder) {
		this.scanRecoder = scanRecoder;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BluetoothDevice getDevice() {
		return device;
	}
	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}
	public String[] getScanRecodestr() {
		return scanRecodestr;
	}
	public void setScanRecodestr(String[] scanRecodestr) {
		this.scanRecodestr = scanRecodestr;
	}
	@Override
	public String toString() {
		return "ScanEntity [mac=" + mac + ", rssi=" + rssi + ", scanRecoder="
				+ scanRecoder + ", name=" + name + ", device=" + device
				+ ", scanRecodestr=" + scanRecodestr + ", bindbyte=" + bindbyte
				+ ", passCode=" + passCode + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
//		result = prime * result + (isPass ? 1231 : 1237);
		result = prime * result + ((mac == null) ? 0 : mac.hashCode());
//		result = prime * result + ((name == null) ? 0 : name.hashCode());
//		result = prime * result + rssi;
//		result = prime * result + Arrays.hashCode(scanRecoder);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanEntity other = (ScanEntity) obj;
		if (mac == null) {
			if (other.mac != null)
				return false;
		} else if (!mac.equals(other.mac)){
			return false;
		}
		return true;
	}
	
	
}
