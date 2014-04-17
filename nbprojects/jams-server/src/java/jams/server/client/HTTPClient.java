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
import java.io.IOException;
import java.net.URLEncoder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class HTTPClient {

    String sessionID = null;
    Client client = ClientBuilder.newClient();       
    
    public Object httpGet(String urlStr, Class responseType) throws IOException, JAXBException {
        return httpRequest(urlStr, null, null, responseType);
    }

    public Object httpFileUpload(String urlStr, File f, Class clazz) throws IOException, JAXBException {        
        client.register(MultiPartFeature.class);
        // MediaType of the body part will be derived from the file.
        final FileDataBodyPart filePart = new FileDataBodyPart("file", f);
 
        MultiPart multipart = new FormDataMultiPart().bodyPart(filePart);
 
        Response response = null;
        response = client.target(urlStr).request().
                header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).                   
                post(Entity.entity(multipart, multipart.getMediaType()));
        
        if (response.getStatus() != 200 ){
            if (response.getStatus() == 203){
                return "Error: Request is forbidden";
            }
            return "Error: Request was not successful";
        }
        
        if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)){            
            return response.readEntity(String.class);
        }else if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
            return response.readEntity(clazz);
        }else
            return response.readEntity(String.class);        
    }
    
    public Object httpRequest(String urlStr, String requestMethod, Object param, Class clazz) throws IOException, JAXBException {
        Response response = null;
        if (requestMethod != null){
            response = client.target(urlStr).request().
                header("Access-Control-Request-Method", requestMethod).
                header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).                   
                method(requestMethod, Entity.entity(param, MediaType.APPLICATION_XML));
        }else{
            response = client.target(urlStr).request().
                header("Access-Control-Request-Method", "GET").
                header("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionID == null ? "0" : sessionID, "UTF-8")).               
                get();
        }
                
        if (sessionID == null){
            String result = response.getHeaders().get("set-cookie").toString();//conn.getHeaderField("set-cookie");
            result = result.split(";")[0];
            sessionID = result.split("=")[1];
        }
        
        if (response.getStatus() != 200 ){
            if (response.getStatus() == 203){
                return "Error: Request is forbidden";
            }
            if (response.getStatus() == 404){
                return "Error: Resource not found";
            }
            if (response.getStatus() == 500){
                return "Error: Internal server error";
            }
            return "Error: Request was not successful";
        }
        
        if (response.getMediaType().equals(MediaType.TEXT_HTML_TYPE)){            
            return response.readEntity(String.class);
        }else if (response.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
            Object o = response.readEntity(clazz);
            response.close();
            return o;
        }else
            return response.readEntity(String.class);
    }
    
    public void close(){
        this.client.close();        
    }
}
