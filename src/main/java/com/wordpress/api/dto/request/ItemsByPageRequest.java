package com.wordpress.api.dto.request;

import java.util.List;

import com.wordpress.api.dto.model.ItemsByPage;
import com.wordpress.api.dto.model.ItemsWordPressModel;

public class ItemsByPageRequest {
	private String user;
	private String site;
	private String spreadsheet_id;
	private List<ItemsWordPressModel> items;
	
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
	public String getSpreadsheet_id() {
		return spreadsheet_id;
	}
	public void setSpreadsheet_id(String spreadsheet_id) {
		this.spreadsheet_id = spreadsheet_id;
	}
	public List<ItemsWordPressModel> getItems() {
		return items;
	}
	public void setItems(List<ItemsWordPressModel> items) {
		this.items = items;
	}
}
