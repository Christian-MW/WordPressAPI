package com.wordpress.api.dto.model;

import java.util.List;

public class ItemsWordPressModel {
	private String name;
	private String url;
	private List<ItemsByPage> result;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<ItemsByPage> getResult() {
		return result;
	}
	public void setResult(List<ItemsByPage> result) {
		this.result = result;
	}
}
