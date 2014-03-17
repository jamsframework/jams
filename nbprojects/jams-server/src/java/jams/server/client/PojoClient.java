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

import jams.server.entities.User;
import jams.server.entities.Users;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class PojoClient {

    String serverUrl = "";
    String userName = "";
    String password = "";

    String sessionID = null;
    
    private InputStream httpGet(String url){
        if (!isLoggedIn()){
            log("user not logged in, not sending request!");
            return null;
        }
                        
        log("sending http request:" + url);
        
        try{
            String xmlResult = HTTPClient.httpGet(url, sessionID);
            //System.out.println(xmlResult);
            log("request successful!");
            return new ByteArrayInputStream(xmlResult.getBytes("UTF-8"));
        }catch(IOException ioe){
            log("request failed!");
            log(ioe.toString());
            return null;
        }                
    }
                
    public boolean connect(String serverUrl, String userName, String password) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;

        try {
            sessionID = HTTPClient.httpGet(serverUrl + "/login?login=" + userName + "&password=" + password, "");
        } catch (IOException ioe) {
            sessionID = null;
            log("Login failed:");
            log(ioe.getMessage());
            return false;
        }
        log("Login successful!");
        return true;
    }

    public boolean isLoggedIn() {
        return sessionID != null;
    }
    
    //Client Methods
    
    public Users getAllUsers() {
        InputStream stream = httpGet(serverUrl + "/all");
        
        if (stream == null){
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(Users.class);
            Unmarshaller unMarshaller = context.createUnmarshaller();
            return (Users) unMarshaller.unmarshal(stream);
        } catch (JAXBException ex) {
            log("conversion failed!");
            log(ex.toString());
            return null;
        }
    }
    
    public Users getUsersInRange(int from, int to) {
        InputStream stream = httpGet(serverUrl + "/" + from + "/" + to);        
        if (stream == null){
            return null;
        }

        try {
            JAXBContext context = JAXBContext.newInstance(Users.class);
            Unmarshaller unMarshaller = context.createUnmarshaller();
            return (Users) unMarshaller.unmarshal(stream);
        } catch (JAXBException ex) {
            log("conversion failed!");
            log(ex.toString());
            return null;
        }
    }
    
    public User getUser(int id) {
        InputStream stream = httpGet(serverUrl + "/" + id);        
        if (stream == null){
            return null;
        }

        try {
            JAXBContext context = JAXBContext.newInstance(User.class);
            Unmarshaller unMarshaller = context.createUnmarshaller();
            return (User) unMarshaller.unmarshal(stream);
        } catch (JAXBException ex) {
            log("conversion failed!");
            log(ex.toString());
            return null;
        }
    }
    
    public boolean removeUser(int id) {
        log("sending remove request:" + id);
        
        try{            
            return HTTPClient.httpRequest(serverUrl + "/" + id,sessionID, "DELETE",null,null );                
        }catch(Exception ioe){
            log("request failed");
            log(ioe.toString());
            return false;
        }
    }
    
    public boolean createUser(User user) {     
        log("sending create user request:" + user.getId());
        try{
            HTTPClient.httpRequest(serverUrl + "/",sessionID, "POST", user, User.class);            
            return true;
        }catch(Exception ioe){
            log("request failed");
            log(ioe.toString());
            return false;
        }
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) throws IOException, JAXBException {
//        System.out.println(httpGet("http://localhost:8080/jams-server/webresources/user"));
        PojoClient client = new PojoClient();
        client.connect("http://localhost:8080/jams-server/webresources/user", "chris", "test");
        
        User user = new User(5);
        user.setAdmin(1);
        user.setEmail("blubb@gmx.de");
        user.setId(5);
        user.setLogin("Blubb");
        user.setName("Blubb der Erste");
        user.setPassword("test");
        
        client.createUser(user);        
        client.getAllUsers();
        client.getUsersInRange(0, 1);
        //client.removeUser(5);

    }

}
