package com.wordpress.api.dto.model;

public class ItemsByPage {
	private String link;
	private String title;
	private String imageUrl;
	private String date;
	private String content;
	private String imageB64;
	private boolean addImage;
	
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getImageB64() {
		return imageB64;
	}
	public void setImageB64(String imageB64) {
		this.imageB64 = imageB64;
	}
	public boolean isAddImage() {
		return addImage;
	}
	public void setAddImage(boolean addImage) {
		this.addImage = addImage;
	}
}
