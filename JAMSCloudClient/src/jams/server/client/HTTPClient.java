/*
 * PojoClient.java
 * Created on 02.03.2014, 20:45:28
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.server.client;

import jams.JAMS;
import jams.tools.FileTools;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class HTTPClient{

    private static final Logger log = Logger.getLogger( HTTPClient.class.getName() );
    
    String SEPARATOR = "**********************\n";
    String sessionID = null;    
    Client client = ClientBuilder.newClient();       
    
    public HTTPClient(){
        log.setLevel(Level.ALL);
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }
            
    public Object connect(String urlStr, Class responseType) {
        log.log(Level.FINER, SEPARATOR + JAMS.i18n("SENDING_LOGIN_REQUEST:") + urlStr);
        
        Object o = httpRequest(urlStr, null, null, responseType);        
        log.log(Level.FINER,JAMS.i18n("Request_send_successful!"));
        return o;
    }
    
    public Object httpGet(String urlStr, Class responseType) {
        log.log(Level.FINER, SEPARATOR + JAMS.i18n("SENDING_GET_REQUEST:") + urlStr);
        if (!isConnected()) {
            log.log(Level.SEVERE, JAMS.i18n("User_not_logged_in!"));
            return null;
        }

        Object o = httpRequest(urlStr, null, null, responseType);        
        log.log(Level.FINER,JAMS.i18n("Request_send_successful!"));
        return o;
    }
    
    public Object httpPost(String urlStr, String method, Object o, Class type) {
        log.log(Level.FINER, SEPARATOR + "\nSENDING "+method+" REQUEST:" + urlStr);
        if (!isConnected()) {
            log.log(Level.SEVERE, JAMS.i18n("User_not_logged_in!"));
        }

        Object obj = httpRequest(urlStr, method, o, type);        
        log.log(Level.FINER,JAMS.i18n("Request_send_successful!"));
        return obj;
    }

    public boolean isConnected(){
        return sessionID != null;
    }
    
    private boolean respondToResponse(String url, int status){
        switch(status){
            case 403: log.log(Level.SEVERE, JAMS.i18n("Error_403:_Request_is_forbidden.") + " " + url); return false;
            case 404: log.log(Level.SEVERE, JAMS.i18n("Error_404:_Resource_not_found.") + " " + url); return false;
            case 500: log.log(Level.SEVERE, JAMS.i18n("Error_500:_Internal_server_error.") + " " + url); return false;
            default: return true;
        }        
    }
            
    public Object httpFileUpload(String urlStr, File f, Class clazz) {        
        if (!client.getConfiguration().isRegistered(MultiPartFeature.class))
            client.register(MultiPartFeature.class);
        // MediaType of the body part will be derived from the file.
        final FileDataBodyPart filePart = new FileDataBodyPart("file", f);
 
        MultiPart multipart = new FormDataMultiPart().bodyPart(filePart);
                
        Response response = null;
        try {            
            response = client.target(urlStr).request().
                    header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).
                    post(Entity.entity(multipart, multipart.getMediaType()));
        } catch (UnsupportedEncodingException uee) {
            log.log(Level.SEVERE, uee.toString(), uee);
        }
        if (response == null || !respondToResponse(urlStr, response.getStatus())){
            return null;
        }
        
        if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)){            
            return response.readEntity(String.class);
        }else if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
            return response.readEntity(clazz);
        }else
            return response.readEntity(String.class);        
    }
    
    public Object httpFileUpload(String urlStr, InputStream in, Class clazz) {        
        if (!client.getConfiguration().isRegistered(MultiPartFeature.class))
            client.register(MultiPartFeature.class);
        // MediaType of the body part will be derived from the file.
        final StreamDataBodyPart filePart = new StreamDataBodyPart("file",in);
 
        MultiPart multipart = new FormDataMultiPart().bodyPart(filePart);
                
        Response response = null;
        try {            
            response = client.target(urlStr).request().
                    header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).
                    post(Entity.entity(multipart, multipart.getMediaType()));
        } catch (UnsupportedEncodingException uee) {
            log.log(Level.SEVERE, uee.toString(), uee);
        }
        if (response == null || !respondToResponse(urlStr, response.getStatus())){
            return null;
        }
        
        if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)){            
            return response.readEntity(String.class);
        }else if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
            return response.readEntity(clazz);
        }else
            return response.readEntity(String.class);        
    }
    
    public File download(String urlStr, File location) {        
        HttpGet get = new HttpGet(urlStr);
        try{
            get.addHeader(new BasicHeader("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")));
        }catch(UnsupportedEncodingException uee){
            uee.printStackTrace();
        }
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse httpResponse = null;
        try{
            httpResponse = httpclient.execute(get);
            if (!this.respondToResponse(urlStr, httpResponse.getStatusLine().getStatusCode())){
                return null;
            }

            InputStream is = httpResponse.getEntity().getContent();
            Header name[] = httpResponse.getHeaders("fileName");
            if (!location.getParentFile().exists()) {
                location.getParentFile().mkdirs();
            }
            if (location.isDirectory()) {
                if (name.length > 0 && name[0].getValue() != null) {
                    location = new File(location, name[0].getValue());
                } else {
                    location = new File(location, "unnamed");
                }
            }           
            FileTools.streamToFile(location, is);
            is.close();            
        }catch(IOException | IllegalStateException jee){
            log.log(Level.SEVERE, jee.toString(), jee);
            return null;
        }
        return location;               
    }
    
    public InputStream getStream(String urlStr) {
        HttpGet get = new HttpGet(urlStr);
        try{
            get.addHeader(new BasicHeader("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")));
        }catch(UnsupportedEncodingException uee){
            uee.printStackTrace();
        }
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse httpResponse = null;
        try{                  
            httpResponse = httpclient.execute(get);
            if (!this.respondToResponse(urlStr, httpResponse.getStatusLine().getStatusCode())){
                return null;
            }

            InputStream is = httpResponse.getEntity().getContent();     
            return is;            
        }catch(IOException | IllegalStateException jee){
            log.log(Level.SEVERE, jee.toString(), jee);
            return null;
        }
    }
    
    public Object httpRequest(String urlStr, String requestMethod, Object param, Class clazz) {

        Response response = null;
        try {
            if (requestMethod != null) {
                response = client.target(urlStr).request().
                        header("Access-Control-Request-Method", requestMethod).
                        header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).
                        method(requestMethod, Entity.entity(param, MediaType.APPLICATION_XML));
            } else {
                response = client.target(urlStr).request().
                        header("Access-Control-Request-Method", "GET").
                        header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).
                        get();
            }
        }catch(  UnsupportedEncodingException | ProcessingException uee){
            log.log(Level.SEVERE, uee.toString(), uee);            
            return null;
        }

        if (response == null){
            log.log(Level.INFO, "There was no response from the server!");            
            return null;
        }
        if (sessionID == null){
            if (response.getHeaders()==null || response.getHeaders().get("set-cookie")==null)
                return null;
            String result = response.getHeaders().get("set-cookie").toString();
            result = result.split(";")[0];
            sessionID = result.split("=")[1];
        }

        if (!respondToResponse(urlStr, response.getStatus())){
            return null;
        }
        
        if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)){    
            if (clazz.isAssignableFrom(String.class))
                return response.readEntity(String.class);
            else{
                log.log(Level.SEVERE, 
                        JAMS.i18n("Conversion_Error:_Expected_class:_%1_found_class_%2_\nContent_is:%3")
                                .replace("%1", clazz.toString())
                                .replace("%2", String.class.toString())
                                .replace("%3", response.readEntity(String.class)));
                return null;
            }
        }else if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
            Object o = null;
            try{
                o = response.readEntity(clazz);
            }catch(ProcessingException pe){
                pe.printStackTrace();
            }
            response.close();
            return o;
        }else if (response.getMediaType().equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)){
            response.readEntity(Object.class);
            response.close();
            return null;
        }else
            return response.readEntity(String.class);
    }
    
    public void close(){
        this.client.close();        
    }
}
