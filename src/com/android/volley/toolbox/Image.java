package com.android.volley.toolbox;

public class Image {

	private String formName;
	private String contentType;

	public String getFormName() {
		return formName;
	}

	public String getContentType() {
		return contentType;
	}

	public byte[] getData() {
		return data;
	}

	private byte[] data;

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
