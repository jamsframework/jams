/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.workspace.plugins;

import jams.JAMS;
import jams.data.*;
import jams.data.Attribute.Entity;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import jams.tools.JAMSTools;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 *
 * @author chris
 */
public class OMIInputDataReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "address of omi connection server")
    public Attribute.String ipAddress;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "port for tcp/ip connection")
    public Attribute.Integer port;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "minimum of data value",
    defaultValue = "-99999999")
    public Attribute.Double min;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "minimum of data value",
    defaultValue = "99999999")
    public Attribute.Double max;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "x position of data")
    public Attribute.Double x[];

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "y position of data")
    public Attribute.Double y[];

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "elevation of data point")
    public Attribute.Double elevation[];

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "unit of data")
    public Attribute.String unit[];

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "property type",
    defaultValue = "type")
    public Attribute.String propertyType;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "domainID",
    defaultValue = "1")
    public Attribute.String domainID;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "referenceID",
    defaultValue = "1")
    public Attribute.String referenceID;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "quantityDescription",
    defaultValue = "unknown quantity")
    public Attribute.String quantityDescription;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "spatial domain description",
    defaultValue = "unknown spatial domain")
    public Attribute.String spatialDomainDescription;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "ids of stations")
    public Attribute.Integer[] stationIDs;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "ids of stations")
    public Attribute.String[] stationNames;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "missingDataValue",
    defaultValue = "-9999")
    public Attribute.Double missingDataValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time of data")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "timeinterval of data")
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "data to export")
    public Attribute.Double[] attribute;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entities")
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute of entities")
    public Attribute.String entityAttribute;

    Socket mySocket = null;
    BufferedInputStream socketInStream = null;
    OutputStream socketOutStream = null;
    static final String STOP_CMD = "<STOP>";
    static final String HEADER_CMD = "<HEADER>";
    static final String DATAGRAMM_CMD = "<DATAGRAMM>";
    static final String WAIT_CMD = "<WAIT>";
    static final String DateFormat = "yyyy-MM-dd HH:mm:ss";
    Charset utf8 = Charset.forName("UTF-8");

    int BUFFERSIZE = 65536;
    byte buffer[] = new byte[BUFFERSIZE];
    StringBuilder inContent = null;

    LinkedList<Datagram> datagramBuffer = new LinkedList<Datagram>();

    private class Datagram{
        Date date;
        double values[];
    }

    private void readInStream() throws IOException{
        while (this.socketInStream.available()>0){
            int count = this.socketInStream.read(buffer);
            inContent.append(new String(buffer, 0, count, utf8));
        }
    }

    private Datagram readDatagramm() throws IOException, ParseException {
        if (inContent == null)
            inContent = new StringBuilder();
        readInStream();
        int lastIndex = inContent.lastIndexOf(STOP_CMD);
        if (lastIndex != -1){
            String content = inContent.substring(0, lastIndex);
            inContent.delete(0, lastIndex);
            String datagrams[] = content.split(this.STOP_CMD);
            for (int i=0;i<datagrams.length;i++){
                String datagram = datagrams[i];
                String tokens[] = datagram.split("\t");
                Datagram dg = new Datagram();
                dg.date = J2KHeader.j2kSdf.parse(tokens[0]);
                dg.values = new double[tokens.length-1];
                for (int j=1;j<tokens.length;j++){
                    double v = Double.parseDouble(tokens[j]);
                    //transform external missing data value into internal missing data value
                    if (v == this.missingDataValue.getValue())
                        v = JAMS.getMissingDataValue();
                    dg.values[j-1] = v;
                }
                datagramBuffer.push(dg);
            }
        }
        if (datagramBuffer.isEmpty())
            return null;
        return datagramBuffer.poll();
    }
    
    @Override
    public void init() {        
        String ipAddress = this.ipAddress.getValue();
        int port = this.port.getValue();
        try {
            mySocket = new Socket(ipAddress, port);

            socketInStream = new BufferedInputStream(mySocket.getInputStream());
            socketOutStream = mySocket.getOutputStream();
        } catch (IOException se) {
            se.printStackTrace();
            this.getModel().getRuntime().sendErrorMsg(se.toString());
        }

        J2KHeader header = new J2KHeader();
        header.setDateStart(this.timeInterval.getStart().getTime());
        header.setDateEnd(this.timeInterval.getEnd().getTime());

        //check input
        int k = -1;
        if (this.entities == null) {
            if (this.x == null || this.y == null || this.elevation == null){
                this.getModel().getRuntime().sendHalt("Either x, y or elevation is not specified");
                return;
            }
            k = this.x.length;
            if (x.length != k) {
                this.getModel().getRuntime().sendHalt("Error dimension mismatch length of x array:" + this.x.length + " vs " + k);
                return;
            }
            if (y.length != k) {
                this.getModel().getRuntime().sendHalt("Error dimension mismatch length of y array:" + this.y.length + " vs " + k);
                return;
            }
            if (elevation.length != k) {
                this.getModel().getRuntime().sendHalt("Error dimension mismatch length of elevation array:" + this.elevation.length + " vs " + k);
                return;
            }
            header.setElementCount(k);

            header.setElevation(JAMSTools.convertJAMSArrayToArray(elevation));
            header.setX(JAMSTools.convertJAMSArrayToArray(x));
            header.setY(JAMSTools.convertJAMSArrayToArray(y));
        } else {
            k = this.entities.getEntities().size();
            double xArray[] = new double[k];
            double yArray[] = new double[k];
            double zArray[] = new double[k];

            int i = 0;
            try {
                for (Attribute.Entity e : this.entities.getEntities()) {
                    double x = e.getDouble("X");
                    double y = e.getDouble("Y");

                    xArray[i] = x;
                    yArray[i] = y;
                    zArray[i] = e.getDouble("ELEVATION");
                    i++;
                }
                header.setElevation(zArray);
                header.setX(xArray);
                header.setY(yArray);
            } catch (NoSuchAttributeException nsee) {
                nsee.printStackTrace();
                this.getModel().getRuntime().sendHalt(nsee.toString());
            }
        }

        header.setIsInputFile(true);
        header.setLowerBound(this.min.getValue());
        header.setUpperBound(this.max.getValue());
        header.setProperty(this.propertyType.getValue());
        header.setMissingDataValue(this.missingDataValue.getValue());
        header.setQuantityDescription(this.quantityDescription.getValue());
        header.setSpatialDomainDescription(this.spatialDomainDescription.getValue());
        header.setSpatialDomainID(this.domainID.getValue());
        header.setSpatialReferenceID(this.referenceID.getValue());
        if (stationIDs == null){
            int stationIDs[] = new int[k];
            for (int i=0;i<k;i++){
                stationIDs[i] = i+1;
            }
            header.setStationIDs(stationIDs);
        }else
            header.setStationIDs(JAMSTools.convertJAMSArrayToArray(stationIDs));

        if (stationIDs == null){
            String stationNames[] = new String[k];
            for (int i=0;i<k;i++){
                stationNames[i] = "loc_" + Integer.toString(i+1);
            }
            header.setStationNames(stationNames);
        }else{
            header.setStationNames(JAMSTools.convertJAMSArrayToArray(stationNames));
        }
        header.setTimeStepCount(this.timeInterval.getTimeUnitCount());
        if (this.timeInterval.getTimeUnit() == Calendar.HOUR)
            header.setTimeStepUnit(J2KHeader.TimePeriod.HOUR);

        if (this.timeInterval.getTimeUnit() == Calendar.DAY_OF_YEAR)
            header.setTimeStepUnit(J2KHeader.TimePeriod.DAY);

        if (this.timeInterval.getTimeUnit() == Calendar.MONTH)
            header.setTimeStepUnit(J2KHeader.TimePeriod.MONTH);

        if (this.timeInterval.getTimeUnit() == Calendar.YEAR)
            header.setTimeStepUnit(J2KHeader.TimePeriod.YEAR);

        int dataColumn[] = new int[k];
        for (int i = 0; i < k; i++) {
            dataColumn[i] = k + 1;
        }
        header.setColumn(dataColumn);

        String msg = HEADER_CMD.substring(0);
        msg += header.writeHeader();
        msg += STOP_CMD;
        try {
            if (this.socketOutStream!=null){
                this.socketOutStream.write(msg.getBytes(utf8));
                this.socketOutStream.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            this.getModel().getRuntime().sendErrorMsg(ioe.toString());
        }
    }

    @Override
    public void run() {
        String date = J2KHeader.j2kSdf.format(this.time.getTime());
        Datagram dg = null;
        try{
            while( (dg = this.readDatagramm())==null){
                if (this.mySocket.isClosed())
                    return;
                try{
                    Thread.sleep(100);
                }catch(InterruptedException ie){
                    System.out.println(ie);
                }
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
            getModel().getRuntime().sendHalt("IOError during reading of datagram!" + ioe.toString());
        }catch(ParseException pe){
            pe.printStackTrace();
            getModel().getRuntime().sendHalt("Can't parse date!" + pe.toString());
        }
        
        double value[] = dg.values;
        if (this.entities != null) {
            value = new double[this.entities.getEntities().size()];
            int i = 0;

            for (Entity e : this.entities.getEntities()) {
                e.setDouble(this.entityAttribute.getValue(),value[i]);
                i++;
            }
        }else{
            value = new double[this.attribute.length];
            for (int i=0;i<this.attribute.length;i++){
                attribute[i].setValue(value[i]);
            }
        }        
    }

    @Override
    public void cleanup(){
        try{
            this.mySocket.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            getModel().getRuntime().sendHalt(ioe.toString());
        }
    }
}
