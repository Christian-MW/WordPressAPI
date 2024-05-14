package com.wordpress.api.service;

import org.springframework.http.ResponseEntity;

import com.wordpress.api.dto.request.*;

public interface GeneralService {
	ResponseEntity<?> addItemsToWP (ItemsByPageRequest request);
	ResponseEntity<?> UploadImage (UploadImageRequest request);
}
