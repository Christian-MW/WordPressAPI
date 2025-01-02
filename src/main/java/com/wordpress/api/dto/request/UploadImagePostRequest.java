package com.wordpress.api.dto.request;

public class UploadImagePostRequest {
	private String imageB64;
	private String user;
	private String site;
	private String blog;
	private String spreadsheet_id;
	private String email;

	public String getImageB64() {
		return imageB64;
	}
	public void setImageB64(String imageB64) {
		this.imageB64 = imageB64;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getBlog() {
		return blog;
	}
	public void setBlog(String blog) {
		this.blog = blog;
	}
	public String getSpreadsheet_id() {
		return spreadsheet_id;
	}
	public void setSpreadsheet_id(String spreadsheet_id) {
		this.spreadsheet_id = spreadsheet_id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
