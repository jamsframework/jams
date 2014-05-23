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

import java.io.File;
import java.io.FileOutputStream;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class HTTPClient {

    String SEPARATOR = "**********************";
    String sessionID = null;
    Client client = ClientBuilder.newClient();       
                
    public HTTPClient(){
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }
    
    private void log(String msg){
        Logger.getLogger(this.getClass().toString()).log(Level.FINER, msg);
    }
    
    private void log(Level lvl, String msg){
        Logger.getLogger(this.getClass().toString()).log(lvl, msg);
    }
    
    private void log(Level lvl, String msg, Throwable t){
        Logger.getLogger(this.getClass().toString()).log(lvl, msg, t);
    }
    
    public Object connect(String urlStr, Class responseType) {
        log(Level.FINER, SEPARATOR + "\nSENDING LOGIN REQUEST:" + urlStr);
        
        Object o = httpRequest(urlStr, null, null, responseType);        
        log(Level.FINER,"INFO: Request send successful!");
        return o;
    }
    
    public Object httpGet(String urlStr, Class responseType) {
        log(Level.FINER, SEPARATOR + "\nSENDING GET REQUEST:" + urlStr);
        if (!isConnected()) {
            log(Level.SEVERE, "User not logged in!");
            return null;
        }

        Object o = httpRequest(urlStr, null, null, responseType);        
        log(Level.FINER,"INFO: Request send successful!");
        return o;
    }
    
    public Object httpPost(String urlStr, String method, Object o, Class type) {
        log(Level.FINER, SEPARATOR + "\nSENDING "+method+" REQUEST:" + urlStr);
        if (!isConnected()) {
            log(Level.SEVERE, "User not logged in");
        }

        Object obj = httpRequest(urlStr, method, o, type);        
        log(Level.FINER,"INFO: Request send successful!");
        return obj;
    }

    public boolean isConnected(){
        return sessionID != null;
    }
    
    private boolean respondToResponse(int status){
        if (status != 200 ){
            if (status == 203){
                log(Level.SEVERE, "Request is forbidden.");
            }
            if (status == 404){
                log(Level.SEVERE, "Resource not found.");
            }
            if (status == 500){
                log(Level.SEVERE, "Internal server error.");
            }
            log(Level.SEVERE, "Request was not successful.");
            return false;
        }
        return true;
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
            log(Level.SEVERE, uee.toString(), uee);
        }
        if (!respondToResponse(response.getStatus())){
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
        DefaultHttpClient httpclient = new DefaultHttpClient();                        
        HttpGet get = new HttpGet(urlStr);
        //get.addHeader(new BasicHeader("Accept", "application/OCTET_STREAM"));
        try{
            get.addHeader(new BasicHeader("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")));
        }catch(UnsupportedEncodingException uee){
            uee.printStackTrace();
        }
        try{
            HttpResponse response = httpclient.execute(get);
            this.respondToResponse(response.getStatusLine().getStatusCode());

            InputStream is = response.getEntity().getContent();
            Header name[] = response.getHeaders("fileName");
            if (!location.exists()){
                    location.mkdirs();
                }
                if (location.isDirectory()){
                    if (name.length>0 && name[0].getValue()!=null){
                        location = new File(location,name[0].getValue());                
                    }else{
                        location = new File(location,"unnamed");                
                }
            
                
            }
            FileOutputStream writer = new FileOutputStream(location);
            byte[] buffer = new byte[65535];
            int fread = 0;
            while((fread=is.read(buffer))>0){
                writer.write(buffer,0,fread);
            }
            writer.flush();
            writer.close();
            is.close();
        }catch(Exception jee){
            log(Level.SEVERE, jee.toString(), jee);
            return null;
        }
        return location;               
    }
    
    public InputStream getStream(String urlStr) {
        DefaultHttpClient httpclient = new DefaultHttpClient();                        
        HttpGet get = new HttpGet(urlStr);
        //get.addHeader(new BasicHeader("Accept", "application/OCTET_STREAM"));
        try{
            get.addHeader(new BasicHeader("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")));
        }catch(UnsupportedEncodingException uee){
            uee.printStackTrace();
        }
        try{
            HttpResponse response = httpclient.execute(get);
            if (!this.respondToResponse(response.getStatusLine().getStatusCode())){
                return null;
            }

            InputStream is = response.getEntity().getContent();     
            return is;            
        }catch(IOException | IllegalStateException jee){
            log(Level.SEVERE, jee.toString(), jee);
            return null;
        }
    }
    
    public Object httpRequest(String urlStr, String requestMethod, Object param, Class clazz) {
        Response response;
                            
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
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, uee.toString(), uee);
            return null;
        }

        if (response == null){
            return null;
        }
        if (sessionID == null){
            if (response.getHeaders()==null || response.getHeaders().get("set-cookie")==null)
                return null;
            String result = response.getHeaders().get("set-cookie").toString();
            result = result.split(";")[0];
            sessionID = result.split("=")[1];
        }

        respondToResponse(response.getStatus());
        
        if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)){    
            if (clazz.isAssignableFrom(String.class))
                return response.readEntity(String.class);
            else{
                log(Level.SEVERE, "Conversion Error: Expeceted Class: " + clazz + " found class " + String.class);
                return null;
            }
        }else if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){            
            Object o = response.readEntity(clazz);
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
