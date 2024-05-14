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
import java.util.Base64;
import java.util.HashMap;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wordpress.api.dto.model.ItemsByPage;
import com.wordpress.api.dto.request.ItemsByPageRequest;
import com.wordpress.api.dto.request.UploadImageRequest;
import com.wordpress.api.service.GeneralService;
import com.wordpress.api.util.Utilities;

@Service("GeneralImpl")
public class GeneralImpl implements GeneralService {
	private static Logger log = Logger.getLogger(GeneralImpl.class);
    @Value("${wordpress.username}")
    private String WordpressUser;
    @Value("${wordpress.password}")
    private String WordpressPassword;
    @Value("${wordpress.url.post}")
    private String WordpressUrlPost;
    @Value("${wordpress.url.image}")
    private String WordpressUrlImage;
    @Value("${wordpress.filelocation}")
    private String WordpressFileLocation;
	@Autowired
	Utilities utilities;

	@Override
	public ResponseEntity<?> addItemsToWP(ItemsByPageRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		log.info("#########----Subiendo el post al servidor WordPress----#########");
		ResponseEntity<?> res = ResponseEntity.ok().build();
		try {
			for (ItemsByPage item : request.getResult()) {
				String apiUrl = WordpressUrlPost;
				String username = WordpressUser;
				String password = WordpressPassword;
				Map<Object, Object> data = new HashMap<>();
				data.put("title", cleanText(item.getTitle(), "title"));
				data.put("content", cleanText(item.getContent(), "content"));
				data.put("excerpt", cleanText(item.getContent(), "excerpt"));
				data.put("status", "publish");
				data.put("featured_media", uploadImageAndGetMediaId(
						item.getImageUrl(), username, password));
				// data.put("tags", new String[]{"tag1", "tag2"});

				HttpClient client = HttpClient.newHttpClient();
				HttpRequest requestPOST = HttpRequest.newBuilder().uri(URI.create(apiUrl))
						.header("Content-Type", "application/json")
						.header("Authorization",
								"Basic " + java.util.Base64.getEncoder()
										.encodeToString((username + ":" + password).getBytes()))
						.POST(HttpRequest.BodyPublishers.ofString(mapToJson(data))).build();

				HttpResponse<String> response = client.send(requestPOST, HttpResponse.BodyHandlers.ofString());
				Thread.sleep(2000);
				if (response.statusCode() == 201) {
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
    private String uploadImageAndGetMediaId(String imagePath, String username, String password) {
    	 log.info("##########__Procesando la imágen para almacenarla en el servidor WordPress___");
    	 log.info("=> Path: " + imagePath);
    	 String imgURL = "";
        try {
        	String uploadUrl = WordpressUrlImage;
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
    		int size = text.length();
    		switch (Type) {
			case "content":
				if(size < 2500) {
					String res = text.replaceAll("\"", "'").replaceAll("[^a-zA-Z0-9 áéíóúÁÉÍÓÚ!<>/'!¡¿?.,()#]", "");
					return res;
				}
				else if(size > 2501) {
					String res = text.replaceAll("\"", "'").replaceAll("[^a-zA-Z0-9 áéíóúÁÉÍÓÚ!<>/'!¡¿?.,()#]", "").substring(0, 2501);
					return res;
				}
			case "excerpt":
				return text.replaceAll("\"", "'").replaceAll("[^a-zA-Z0-9 áéíóúÁÉÍÓÚ!<>/'!¡¿?.,()#]", "").substring(0, 150);
			default:
				return text.replaceAll("\"", "'").replaceAll("[^a-zA-Z0-9 áéíóúÁÉÍÓÚ!<>/'!¡¿?.,()#]", "");
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
		String res = uploadImageAndGetMediaId(request.getImagePath(), request.getUsername(), request.getPassword());
		return null;
	}

}
