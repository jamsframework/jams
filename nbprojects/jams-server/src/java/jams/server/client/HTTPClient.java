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
public class HTTPClient {

    String serverUrl = "";
    String userName = "";
    String password = "";

    String sessionID = null;

    public static String httpGet(String urlStr, String sessionID) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn
                = (HttpURLConnection) url.openConnection();

        if (sessionID != null) {
            conn.setRequestProperty("Cookie", "JSESSIONID="
                    + URLEncoder.encode(sessionID, "UTF-8"));
        }

        conn.connect();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        conn.disconnect();
        return sb.toString();
    }
    
    public static boolean httpRequest(String urlStr, String sessionID, String requestMethod, Object param, Class clazz) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn
                = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(requestMethod);        
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Content-Type",
                "application/xml; charset=\"utf-8\"");

        if (sessionID != null) {
            conn.setRequestProperty("Cookie", "JSESSIONID="
                    + URLEncoder.encode(sessionID, "UTF-8"));
        }
        
        // Create the form content
        if (param==null){
            conn.setDoOutput(false);
        } else {
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            JAXBContext context = JAXBContext.newInstance(clazz);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(param, out);
            out.flush();
            conn.connect();
        }
                       
        conn.connect();

        //200 = ok
        //204 = no content
        if (conn.getResponseCode() != 200 && conn.getResponseCode() != 204) {
            throw new IOException(conn.getResponseMessage());
        }
        
        conn.disconnect();
        return true;
    }
}
