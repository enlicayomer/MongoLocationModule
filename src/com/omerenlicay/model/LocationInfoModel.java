package com.omerenlicay.model;

public class LocationInfoModel {

	private String corporation;
	
	private String tag;

	private String bilinmeyenKonum="Bilinmeyen Konum";
	
	
	public String getBilinmeyenKonum() {
		return bilinmeyenKonum;
	}

	public void setBilinmeyenKonum(String bilinmeyenKonum) {
		this.bilinmeyenKonum = bilinmeyenKonum;
	}

	public String getCorporation() {
		return corporation;
	}

	public void setCorporation(String corporation) {
		this.corporation = corporation;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	
}
