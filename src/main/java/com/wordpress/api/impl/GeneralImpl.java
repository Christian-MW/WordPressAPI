package com.wordpress.api.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.wordpress.api.dto.model.ItemsByPage;
import com.wordpress.api.dto.model.ItemsWordPressModel;
import com.wordpress.api.dto.request.GoogleGetDataRequest;
import com.wordpress.api.dto.request.ItemsByPageRequest;
import com.wordpress.api.dto.request.UploadImageRequest;
import com.wordpress.api.dto.response.GoogleGetDataResponse;
import com.wordpress.api.service.GeneralService;
import com.wordpress.api.util.Utilities;

@Service("GeneralImpl")
public class GeneralImpl implements GeneralService {
	private static Logger log = Logger.getLogger(GeneralImpl.class);
    @Value("${wordpress.username}")
    private String WordpressUser;
    @Value("${wordpress.password}")
    private String WordpressPassword;
    @Value("${wordpress.filelocation}")
    private String WordpressFileLocation;
    @Value("${file.headers.wordpress.config}")
    private String HEADERS_WP_CONFIG;
    @Value("${file.wordpress.name.config}")
    private String NAME_SHEET_WP_CONFIG;
    @Value("${file.wordpress.name.col}")
    private String NAME_COLUMN_SITE;
	@Autowired
	Utilities utilities;

	@Override
	public ResponseEntity<?> addItemsToWP(ItemsByPageRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		log.info("#########----Subiendo el post al servidor WordPress----#########");
		ResponseEntity<?> res = ResponseEntity.ok().build();
		try {
			log.info("##############=========>REQUEST POST FROM EXTENSIÓN : " + new Gson().toJson(request));
			//Obtener los accesos del sitio WordPress
			List<String> access = findAccessByUser(request.getUser(), request.getSite(), request.getSpreadsheet_id());
			if(access.size() > 0) {
				if (request.getItems().size() > 0) {
					for (ItemsWordPressModel itemGeneral : request.getItems()) {
						if (request.getItems().size() > 0) {
							for (ItemsByPage item : itemGeneral.getResult()) {
								String apiUrl = access.get(0) + "/wp-json/wp/v2/posts";
								String username = access.get(1);
								String password = access.get(2);
								Map<Object, Object> data = new HashMap<>();
								data.put("title", cleanText(item.getTitle(), "title"));
								data.put("content", cleanText(item.getContent(), "content"));
								data.put("excerpt", cleanText(item.getTitle(), "excerpt"));
								data.put("status", "publish");
								data.put("featured_media",uploadImageAndGetMediaId(item.getImageUrl(), username, password, access.get(0)));

								log.info("=>DATA TO SEND WORDPRESS_ : " + new Gson().toJson(data));
								HttpClient client = HttpClient.newHttpClient();
								HttpRequest requestPOST = HttpRequest.newBuilder().uri(URI.create(apiUrl))
										.header("Content-Type", "application/json")
										.header("Authorization",
												"Basic " + java.util.Base64.getEncoder()
														.encodeToString((username + ":" + password).getBytes()))
										.POST(HttpRequest.BodyPublishers.ofString(mapToJson(data))).build();

								HttpResponse<String> response = client.send(requestPOST,
										HttpResponse.BodyHandlers.ofString());
								log.info("=>response WORDPRESS_ : " + new Gson().toJson(response.body()));
								Thread.sleep(2000);
								if (response.statusCode() == 201 || response.statusCode() == 200) {
									log.info("###############__SE INSERTÓ CORRECTAMENTE");
									map.put("code", response.statusCode());
									map.put("message", "CREATED");
									res = utilities.getResponseEntity(map);
								} else {
									log.error("ERROR AL INSERTAR EL POST EN WORDPRESS Code: " + response.statusCode());
									map.put("code", 500);
									map.put("message", "ERROR");
									res = utilities.getResponseEntity(map);
								}
							}
						}
					}
				} else {
					log.error("ERROR AL INSERTAR EL POST EN WORDPRESS Code: " + 500);
					map.put("code", 401);
					map.put("message", "NO EXISTEN ELEMENTOS A INSERTAR");
					res = utilities.getResponseEntity(map);
				}
			}
			else {
				log.error("No existe el usuario en el archivo de Configuración de WordPress: " + 404);
				map.put("code", 404);
				map.put("message", "NO EXISTE EL SITIO: " + request.getSite() 
				+", EN EL ARCHIVO DE CONFIGURACIÓN WORDPRESS: " + request.getSpreadsheet_id());
				res = utilities.getResponseEntity(map);
			}

			return res;
		} catch (Exception ex) {
			map.put("code", 500);
			map.put("message", "ERROR");
			res = utilities.getResponseEntity(map);
			return res;
		}
	}
    private static String mapToJson(Map<Object, Object> data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        json.deleteCharAt(json.length() - 1);
        json.append("}");
        return json.toString();
    }
    private static String mapToJsonImg(Map<String, Object> data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            json.append(",");
        }
        json.deleteCharAt(json.length() - 1);
        json.append("}");
        return json.toString();
    }
    private String uploadImageAndGetMediaId(String imagePath, String username, String password, String urlMedia) {
    	 log.info("##########__Procesando la imágen para almacenarla en el servidor WordPress___");
    	 log.info("=> Path: " + imagePath);
    	 String imgURL = "";
        try {
        	//String uploadUrl = WordpressUrlImage;
        	String uploadUrl = urlMedia + "/wp-json/wp/v2/media";
        	org.apache.http.client.HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(uploadUrl);

            if (username != null && password != null) 
                httpPost.setHeader("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
            
            //Desacargar la imágen
            //String directory="C:/Users/hk/Pictures/Wallpapers/";
            String directory = WordpressFileLocation;
            log.info("=> Path de la imágen: " + directory + "image.jpg");
            URL url = new URL(imagePath);
            InputStream inputStream = url.openStream();
            Path destinationPath = Paths.get(directory, "image.jpg");
            Files.copy(inputStream, destinationPath);
            inputStream.close();
            imgURL = destinationPath.toString();
            log.info("Imagen almacenada correctamente");
            
            File imageFile = new File(imgURL);
            FileBody fileBody = new FileBody(imageFile);
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("file", fileBody)
                    .build();
            httpPost.setEntity(entity);
            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
            
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 201) {
            	log.info("#####__La imagen "+ imagePath+" se ha almacenado correctamente en el servidor de WordPress__#####");
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(responseBody);
                //int id = Integer.valueOf(jsonObject.get("id").toString());
                boolean status = deleteImage(imgURL);
                return jsonObject.get("id").toString();
            } else {
            	log.error("Error al subir la imagen al servidor de WordPress. Código de estado: " + statusCode);
                System.err.println("Error al subir la imagen. Código de estado: " + statusCode);
                return "";
            }
        } catch (IOException e) {
        	log.error("#######_PROBLEMAS AL PROCESAR LA IMÁGEN_ servidor de WordPress #####");
        	log.error(e.getMessage());
            return "";
        }
    }
    private String cleanText(String text, String Type) {
    	try {
    		text = text.replaceAll("\n", "<br>");
    		text = text.replaceAll("\"", "'").replaceAll("[^a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ!<>/'!¡¿?.,()#]", "");
    		int size = text.length();
    		switch (Type) {
			case "content":
				if(size > 2501) 
					return text.substring(0, 2501);
				else 
					return text;
				
			case "excerpt":
				if(size > 150) 
					return text.substring(0, 150);
				else 
					return text;
				
			default:
				return text;
			}
		} catch (Exception ex) {
			log.error("PROBLEMAS AL LIMPIAR EL TEXTO: " + text);
			log.error(ex.getMessage());
			return text;
		}
    }
    public boolean deleteImage(String filePath) {
    	try {
    		Thread.sleep(1000);
    		File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    log.info("El archivo se ha eliminado correctamente.");
                    return true;
                } else {
                	log.error("No se pudo eliminar el archivo.");
                	return false;
                }
            } else {
            	log.error("El archivo no existe en la ubicación especificada.");
            	return false;
            }
		} catch (Exception ex) {
			return false;
		}
    }
	@Override
	public ResponseEntity<?> UploadImage(UploadImageRequest request) {
		log.info("");
		String res = uploadImageAndGetMediaId(request.getImagePath(), request.getUsername(), request.getPassword(), "");
		return null;
	}

	//Buscar en el servicio de google el los accesos al WordPress
	public List<String> findAccessByUser(String user, String site, String sheetID) {
		List<String> access = new ArrayList<>();
		try {
	        if (site.endsWith("/")) {
	        	site = site.substring(0, site.length() - 1);
	        }
			ObjectMapper objectMapper = new ObjectMapper();
			org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
			GoogleGetDataRequest request = new GoogleGetDataRequest();
			request.setColumns(HEADERS_WP_CONFIG);
			if(NAME_SHEET_WP_CONFIG.equals("Configuracion sitios"))
				NAME_SHEET_WP_CONFIG = "Configuración sitios"; 
			request.setRange(NAME_SHEET_WP_CONFIG);
			request.setSpreadsheet_id(sheetID);
			String url = "http://3.138.108.174:8081/GoogleData/getData/sheet";
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        org.springframework.http.HttpEntity<GoogleGetDataRequest> entity = new org.springframework.http.HttpEntity<>(request, headers);
	        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

	        String res = response.getBody();
	        GoogleGetDataResponse responseObj = objectMapper.readValue(res, GoogleGetDataResponse.class);
	        if(responseObj.getCode() == 200) {
		        boolean finished = false;;
		        for (int i = 0; i < responseObj.getObjectResult().size() && !finished; i++) {
		        	for(int j = 0; j < responseObj.getObjectResult().get(i).size() && !finished; j++) {
		        		String s = responseObj.getObjectResult().get(i).get(j).toString();
		                if (s.endsWith("/")) 
		                    s = s.substring(0, s.length() - 1);
		        		if(s.equals(site)) {
		        			access.add(s);
		        			access.add(responseObj.getObjectResult().get(i).get(j+1));
		        			access.add(responseObj.getObjectResult().get(i).get(j+2));
		        			finished = true;
		        			break;
		        		}
		        	}
				}
	        }
	        return access;
	        
		} catch (Exception ex) {
			System.out.println(ex);
			return access;
		}
	}
}
