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
import static jams.tools.StringTools.format;
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
import static jams.tools.LogTools.log;
import javax.ws.rs.client.ResponseProcessingException;
import org.apache.http.client.HttpResponseException;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class HTTPClient {

    private String sessionID = null;

    /**
     * The Standard-Constructor
     *
     */
    public HTTPClient() {
        Logger.getLogger(this.getClass().getName()).setLevel(Level.ALL);
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    /**
     * connects to a server and saves the session cookie
     * @param <T> the expected type of the result
     * @param urlStr url to server
     * @param responseType the expected type of the result
     * @return the object returned by the request
     */
    public <T> T connect(String urlStr, Class<T> responseType) {
        log(this.getClass(),Level.FINER, JAMS.i18n("LOGIN_TO_{1}"), urlStr);
        sessionID = null;
        return httpRequest(urlStr, "GET", null, responseType);
    }
    
    /**
     * disconnects from the server i.e. the session cookie is deleted
     *     
     */
    public void disconnect() {
        log(this.getClass(),Level.FINER, JAMS.i18n("DISCONNECT"));
        sessionID = null;
    }

     /**
     * sends a http-get request to a given url
     * @param <T> the expected type of the result
     * @param urlStr url to server
     * @param responseType the expected type of the result
     * @return the object returned by the request
     */
    public <T> T httpGet(String urlStr, Class<T> responseType) {        
        return httpRequest(urlStr, "GET", null, responseType);
    }

    /**
     * sends a http-post request to a given url
     *
     * @param <T> the expected type of the result
     * @param urlStr url to server
     * @param method post method (POST, DELETE, ..)
     * @param obj object to be send by this request
     * @param responseType the expected type of the result
     * @return the object returned by the request
     */
    public <T> T httpPost(String urlStr, String method, Object obj, Class<T> responseType) {                
        return httpRequest(urlStr, method, obj, responseType);
    }
            
    /**
     * uploads a file to a given url
     * 
     * @param <T> the expected type of the result
     * @param urlStr url to server
     * @param multipart multipart containing the file (either fileObject or stream)
     * @param type typ of class which should be returned
     * throws ProcessingException under various conditions
     */
    private <T> T httpFileUpload(String urlStr, MultiPart multipart, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        if (!client.getConfiguration().isRegistered(MultiPartFeature.class)) {
            client.register(MultiPartFeature.class);
        }

        Response response = null;
        try {
            response = client.target(urlStr).request().
                    header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).
                    post(Entity.entity(multipart, multipart.getMediaType()));
        } catch (UnsupportedEncodingException uee) {
            throw new ProcessingException(uee);
        }
        
        if (response == null) {            
            throw new ProcessingException(
                    format(JAMS.i18n("There_was_no_response_from_{0}"), urlStr));
        }
        
        try {
            if (response.getStatus()>=400)
                throw new ResponseProcessingException(response, "Request failed!");

            if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
                return response.readEntity(responseType);
            }else if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)) {
                String msg = response.readEntity(String.class);
                throw new ResponseProcessingException(response, format(JAMS.i18n("The_response_from_{0}_was_a_text_message_with_the_content_{1}"), urlStr, msg));
            }else{
                throw new ResponseProcessingException(response, format(JAMS.i18n("The_response_from_{0}_was_a_message_with_not_supported_mediatype_{1}"), urlStr, response.getMediaType()));     
                                
            }
        } finally {
            response.close();
            client.close();
        }
        
    }

    /**
     * uploads a file to a given url
     *
     * @param <T> type of class which should be returned
     * @param urlStr url to server
     * @param f file that should be uploaded
     * @param responseType type of class which should be returned
     * throws ProcessingException under various conditions
     * @return the result of the request
     */
    public <T> T httpFileUpload(String urlStr, File f, Class<T> responseType) {
        if (!f.exists() || f.isDirectory()) {
            throw new ProcessingException(
                format(JAMS.i18n("{0}_cannot_be_uploaded,_since_it_is_either_not_existing_or_a_directory"), f.toString()));
        }
        final FileDataBodyPart filePart = new FileDataBodyPart("file", f);
        MultiPart multipart = new FormDataMultiPart().bodyPart(filePart);

        return httpFileUpload(urlStr, multipart, responseType);
    }
    
    /**
     * uploads a file to a given url
     *
     * @param <T> type of class which should be returned
     * @param urlStr url to server
     * @param in inputstream that should be uploaded
     * @param responseType type of class which should be returned
     * throws ProcessingException under various conditions
     * @return the result of the request
     * @throws java.io.IOException
     */
    public <T> T httpFileUpload(String urlStr, InputStream in, Class<T> responseType) throws IOException {
        final StreamDataBodyPart filePart = new StreamDataBodyPart("file", in);
        MultiPart multipart = new FormDataMultiPart().bodyPart(filePart);
        return httpFileUpload(urlStr, multipart, responseType);
    }

    /**
     * downloads the result of the request to a given location
     *
     * @param urlStr url to server
     * @param location target location     
     * @return the target file
     * @throws java.io.IOException
     * @throws ProcessingException under various conditions
     */
    public File download(String urlStr, File location) throws IOException {
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(urlStr);
        try {
            get.addHeader(new BasicHeader("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")));
        } catch (UnsupportedEncodingException uee) {
            throw new ProcessingException(uee);
        }

        HttpResponse httpResponse = httpclient.execute(get);
        if (httpResponse.getStatusLine().getStatusCode()>=400){
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), urlStr);
        }
        try (InputStream is = httpResponse.getEntity().getContent()) {
            Header name[] = httpResponse.getHeaders("fileName");
            FileTools.assertDirectory(location.getAbsolutePath());
            if (name.length > 0 && name[0].getValue() != null) {
                location = new File(location, name[0].getValue());
            } else {
                location = new File(location, "unnamed");
            }

            FileTools.streamToFile(location, is);
        }
        return location;
    }

     /**
     * return the content stream of a specific url
     *
     * @param urlStr url to server  
     * @return the content stream
     * @throws java.io.IOException
     * @throws HttpResponseException under various conditions
     */
    public InputStream getStream(String urlStr) throws IOException {
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(urlStr);
        try {
            get.addHeader(new BasicHeader("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")));
        } catch (UnsupportedEncodingException uee) {
            throw new ProcessingException(uee);
        }

        HttpResponse httpResponse = httpclient.execute(get);
        if (httpResponse.getStatusLine().getStatusCode() >=400){
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), urlStr);
        }
        return httpResponse.getEntity().getContent();
    }

    /**
     * processes a httpRequest
     *
     * @param <T> the expected type of the result
     * @param urlStr url to server  
     * @param requestMethod  
     * @param param  
     * @param responseType the expected type of the result 
     * @return the response of the server
     * @throws ProcessingException under various conditions
     */
    protected <T> T httpRequest(String urlStr, String requestMethod, Object param, Class<T> responseType) {                        
        Client client = ClientBuilder.newClient();

        Response response;
        log(this.getClass(),Level.FINER, JAMS.i18n("SENDING_{0}-REQUEST_TO_{1}"), requestMethod, urlStr);
        try {            
            if (requestMethod.compareTo("GET")!=0) {                
                response = client.target(urlStr).request().
                        header("Access-Control-Request-Method", requestMethod).
                        header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).
                        method(requestMethod, Entity.entity(param, MediaType.APPLICATION_XML));
            } else {
                response = client.target(urlStr).request().
                        header("Access-Control-Request-Method", requestMethod).
                        header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).
                        get();
            }
        } catch (UnsupportedEncodingException uee) {
            throw new ProcessingException(uee);
        }

        if (response == null) {
            throw new ProcessingException(
                    format(JAMS.i18n("There_was_no_response_from_{0}"), urlStr));
        }
        if (response.getStatus() >= 400){
            throw new ResponseProcessingException(response, "Request failed");
        }
        if (sessionID == null) {
            if (response.getHeaders() == null || response.getHeaders().get("set-cookie") == null) {
                String msg = null;
                try{
                    msg = response.readEntity(String.class);                    
                }catch(Throwable t){
                    throw new ProcessingException(JAMS.i18n("Unable to receive any session id!"),t);
                }
                throw new ProcessingException(msg);                
            }
            String result = response.getHeaders().get("set-cookie").toString();
            result = result.split(";")[0];
            sessionID = result.split("=")[1];
        }

        try {
            
            if (response.getMediaType()==null){
                
            }
            if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
                log(this.getClass(),Level.FINER, JAMS.i18n("Request_send_successful!"));
                return response.readEntity(responseType);
            }else if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)) {
                String msg = response.readEntity(String.class);
                throw new ProcessingException(
                    format(JAMS.i18n("The_response_from_{0}_was_a_text_message_with_the_content_{1}"), urlStr, msg));                                
            }else{
                throw new ProcessingException(
                    format(JAMS.i18n("The_response_from_{0}_was_a_message_with_not_supported_mediatype_{1}"), urlStr, response.getMediaType()));                 
            }            
        } finally {
            response.close();
            client.close();
        }
    }
}
