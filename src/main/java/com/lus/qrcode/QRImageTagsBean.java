package com.lus.qrcode;

import com.itextpdf.text.Rectangle;

public class QRImageTagsBean {

	private String fileName = null;
	private Rectangle rect = null;
	private String imageLabel = null;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Rectangle getRect() {
		return rect;
	}
	public void setRect(Rectangle rect) {
		this.rect = rect;
	}
	public String getImageLabel() {
		return imageLabel;
	}
	public void setImageLabel(String imageLabel) {
		this.imageLabel = imageLabel;
	}
}
