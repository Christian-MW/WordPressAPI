package com.wordpress.api.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordpress.api.dto.request.*;
import com.wordpress.api.service.GeneralService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@RestController
@RequestMapping(value="/WordPress")
@Api(value = "GeneralController")
public class GeneralController {
	private static Logger log = Logger.getLogger(GeneralController.class);
	@Autowired
	GeneralService generalService;
	
	@ApiOperation(value = "Inserta una lista de post al WordPress")
    @ApiResponses(value = { 
        @ApiResponse(code = 500, message = "Server Error")
    })
	@PostMapping(value="/API/V1/AddItems",
	consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> AddDataSearchFile(@RequestBody ItemsByPageRequest request){
		return generalService.addItemsToWP(request);
	}
	@PostMapping(value="/API/V1/uploadImage",
	consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> UploadImage(@RequestBody UploadImageRequest request){
		return generalService.UploadImage(request);
	}
}
