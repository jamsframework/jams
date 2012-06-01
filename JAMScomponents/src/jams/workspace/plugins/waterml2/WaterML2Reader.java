/*
 * J2KTSFileReader.java
 * Created on 25. August 2008, 16:50
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.workspace.plugins.waterml2;

import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.workspace.DataReader;
import jams.workspace.DefaultDataSet;
import jams.workspace.Workspace;
import jams.workspace.datatypes.CalendarValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.LongValue;
import jams.workspace.datatypes.StringValue;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class WaterML2Reader implements DataReader {

    private DefaultDataSet[] currentData = null;
    private int iterator = 0;
    private Document wml2Doc;
    private FeatureCollection result = null;
    private String requestedTimeseriesObservation;
    private String sosURL;
    transient private DefaultHttpClient httpclient;
    private TimeValuePair data[];

    private long ID;
    private String offering_name;
    private double x;
    private double y;
    private double z;
    private String unit;
    private String parameter="";

    public ReaderType getReaderType(){
        return ReaderType.ContentAndMetadataReader;
    }

    private static String htmlResponseToString(HttpResponse response) throws Exception {
        HttpEntity entity = response.getEntity();
        BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
        String page = "", html;
        while ((html = buf.readLine()) != null) {
            page += html + "\n";
        }

        //System.out.println(page);
        return page;
    }

    public void setWorkspace(Workspace ws){

    }

    private static String sendGetRequest(DefaultHttpClient client, HttpGet httpget) throws Exception {
        httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.1.8) Gecko/20100202 Firefox/3.5.8 (.NET CLR 3.5.30729)");

        HttpResponse response = null;
        int retry = 0;
        String result = "";
        System.out.println("Sending Get Request ... ");
        do {
            response = client.execute(httpget);
            result = htmlResponseToString(response);
            if (retry++ >= 3) {
                System.out.println("Retried to connect server three times, but was not successful!");
                return null;
            }
        } while (response.getStatusLine().getStatusCode() > 300);
        return result;
    }

    public int init() {
        httpclient = new DefaultHttpClient();

        HttpGet sosGET = new HttpGet(getSosURL());
        String sosResponse = null;
        try {
            sosResponse = sendGetRequest(httpclient, sosGET);
            wml2Doc = jams.tools.XMLTools.getDocumentFromString(sosResponse);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error while accessing web-service:" + getSosURL() + "\nerror:" + e.toString());
        }



        NodeList nodeList = wml2Doc.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeName().matches(".*FeatureCollection")) {
                try{
                    result = new FeatureCollection(node);
                }catch(WaterML2Exception wex){
                    System.out.println(wex.exception);
                    return 0;
                }
            }
        }

        if (result == null) {
            return 0;
        }
        if (result.observations == null) {
            return 0;
        }

        for (ObservationMember m : result.observations) {
            if (m.tsObservation == null) {
                return 0;
            }
            if (m.tsObservation.getId().compareTo(requestedTimeseriesObservation) == 0) {
                data = m.tsObservation.getResult().timeserie.getData();
                this.offering_name = m.tsObservation.getName();
                this.ID = 1;
                this.x = m.tsObservation.getLocation().point.x;
                this.y = m.tsObservation.getLocation().point.y;
                this.z = m.tsObservation.getLocation().point.h;
                for (TimeValuePair entry : data) {
                    System.out.println(Common.WML2_TIME_FORMAT.format(entry.time.getTime()) + "\t" + entry.value + " " + entry.unit);
                    this.unit = entry.getUnit();
                }
                return data.length;
            }
        }

        iterator = 0;
        return 0;
    }

    public int numberOfColumns() {
        return 2;
    }

    public int cleanup() {
        return 0;
    }

    private boolean hasNext() {
        return this.iterator < this.data.length;
    }

    private TimeValuePair next() {
        return this.data[iterator++];
    }

    public DefaultDataSet getMetadata(int index) {
        DefaultDataSet data = new DefaultDataSet(7);
        data.setData(0,new LongValue(this.ID));
        data.setData(1,new StringValue(this.offering_name));
        data.setData(2,new DoubleValue(this.x));
        data.setData(3,new DoubleValue(this.y));
        data.setData(4,new DoubleValue(this.z));
        if (this.unit!=null)
            data.setData(5,new StringValue(this.unit));
        else
            data.setData(5,new StringValue("?"));
        data.setData(6,new StringValue(this.parameter));

        return data;
    }

    private DefaultDataSet getNextDataSet() {
        DefaultDataSet dataSet;

        int numberOfColumns = numberOfColumns();

        if (!hasNext()) {
            return null;
        }
        TimeValuePair tvp = next();
        dataSet = new DefaultDataSet(numberOfColumns);

        Attribute.Calendar cal = JAMSDataFactory.createCalendar();
        cal.setTimeInMillis(tvp.time.getTimeInMillis());
        dataSet.setData(0, new CalendarValue(cal));
        dataSet.setData(1, new DoubleValue(tvp.getValue()));

        return dataSet;
    }

    private DefaultDataSet[] convertData(long count) {
        ArrayList<DefaultDataSet> data = new ArrayList<DefaultDataSet>();

        DefaultDataSet dataSet;

        while ((data.size() < count)) {
            dataSet = getNextDataSet();
            if (dataSet!=null)
                data.add(dataSet);
            else
                break;
        }

        return data.toArray(new DefaultDataSet[data.size()]);
    }
    
    public int fetchValues() {        
            currentData = convertData(Long.MAX_VALUE);
        return 0;

    }

    public int fetchValues(int count) {        
            currentData = convertData(count);
        return 0;
    }

    public DefaultDataSet[] getData() {
        return currentData;
    }

    /**
     * @return the requestedTimeseriesObservation
     */
    public String getRequestedTimeseriesObservation() {
        return requestedTimeseriesObservation;
    }

    /**
     * @param requestedTimeseriesObservation the requestedTimeseriesObservation to set
     */
    public void setRequestedTimeseriesObservation(String requestedTimeseriesObservation) {
        this.requestedTimeseriesObservation = requestedTimeseriesObservation;
    }

    /**
     * @return the sosURL
     */
    public String getSosURL() {
        return sosURL;
    }

    /**
     * @param sosURL the sosURL to set
     */
    public void setSosURL(String sosURL) {
        this.sosURL = sosURL;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public static void main(String arg[]) throws FileNotFoundException, WaterML2Exception {
        WaterML2Reader wml2Reader = new WaterML2Reader();
        wml2Reader.setSosURL("http://localhost:24000/istSOS/sos.py?request=GetObservation&offering=urn:x-fsu:1.0:offering:592&procedure=urn:ogc:object:procedure:x-fsu:1.0:DAILY&eventTime=2002-02-10T16:00:00/2003-02-10T17:00:00&observedProperty=urn:ogc:def:property:x-fsu:1.0:precipitation&responseFormat=text/xml;subtype='WML2'&service=SOS&version=1.0.0");
        wml2Reader.setRequestedTimeseriesObservation("ts_serie1");
        wml2Reader.init();

        /*for (ObservationMember m : wml2Reader.result.observations) {
        TimeValuePair tvp[] = m.tsObservation.getResult().timeserie.getData();
        for (TimeValuePair data : tvp) {
        System.out.println(Common.WML2_TIME_FORMAT.format(data.time.getTime()) + "\t" + data.value + " " + data.unit + "\n");
        }
        }*/
    }
}
