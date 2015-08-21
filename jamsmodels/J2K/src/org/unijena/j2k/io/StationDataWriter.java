/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.io;

import jams.JAMS;
import java.io.IOException;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.workspace.stores.*;
import jams.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "StationDataWriter",
                          author = "Peter Krause",
                          description = "Writes standard ASCII timeseries data files")
public class StationDataWriter extends JAMSComponent{
    public static final String EMPTY_CHAR = "";
    public static final String SEPARATOR = "\t";
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time interval"
            )
            public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
                        description = "the data values")
    public Attribute.DoubleArray values;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "the station names")
    public Attribute.StringArray statNames;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "the station Ids")
    public Attribute.IntegerArray statId;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "the station elevation")
    public Attribute.DoubleArray statElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "the station x-coordinates")
    public Attribute.DoubleArray statX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "the station y-coordinates")
    public Attribute.DoubleArray statY;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "data set description [type min max unit]")
    public Attribute.String dataSetDesc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution",
            defaultValue=EMPTY_CHAR
            )
            public Attribute.String tempRes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "missing data value"
            )
            public Attribute.Double missDataValue;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output data precision"
            )
            public Attribute.Integer precision;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Output file name")
    public Attribute.String fileName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "Data store description")
    public Attribute.String xmlDSD;

    /**
     * this attribute controls, whether an input header is written or only 1 simple header-line
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "write file with input header",
            defaultValue="true"
            )
            public Attribute.String withInputHeader;

    private GenericDataWriter writer;
    private DateFormat dateFormat;


    /*
     *  Component run stages
     */
    @Override
    public void init() {
        
        getModel().getRuntime().println(" start init " + fileName.getValue() + ".. ", JAMS.VERBOSE);
        Date dt = new Date();
        int tu = this.timeInterval.getTimeUnit();
        String timeFormat = "%1$tY-%1$tm-%1$td %1$tH:%1$tM";
        //hourly values
        if(tu == 11) {
            timeFormat = "%1$td.%1$tm.%1$tY %1$tH:%1$tM";
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //daily values
        } else if(tu == 6) {
            timeFormat = "%1$td.%1$tm.%1$tY";
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        //monthly values
        } else if(tu == 2) {
            timeFormat = "%1$tm/%1$tY";
            dateFormat = new SimpleDateFormat("MM/yyyy");
        //annual values
        } else if(tu == 1) {
            timeFormat = "%1$tY";
            dateFormat = new SimpleDateFormat("yyyy");
        }
        dateFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);

        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspace().getOutputDataDirectory().getPath(),fileName.getValue()));
        String inputHeader = withInputHeader.getValue();
        if (inputHeader != null && inputHeader.equalsIgnoreCase("true")) {

            //write station meta data as header
            writer.writeLine("#Calculated input data, generated: "+dt);
            writer.writeLine(J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS);
            writer.writeLine(dataSetDesc.getValue());
            writer.writeLine(J2KTSDataStore.TAGNAME_DATASETATTRIBS);
            writer.writeLine(J2KTSDataStore.TAGNAME_MISSINGDATAVAL + SEPARATOR + missDataValue.getValue());
            writer.writeLine(J2KTSDataStore.TAGNAME_DATASTART + SEPARATOR + timeInterval.getStart().toString(dateFormat));
            writer.writeLine(J2KTSDataStore.TAGNAME_DATAEND + SEPARATOR + timeInterval.getEnd().toString(dateFormat));
            writer.writeLine(J2KTSDataStore.TAGNAME_TEMP_RES + SEPARATOR + tempRes);
            writer.writeLine(J2KTSDataStore.TAGNAME_STATATTRIBVAL);
            writer.addColumn("name");
            for (int i = 0; i < statNames.getValue().length; i++) {
                writer.addColumn(statNames.getValue()[i]);
            }
            writer.writeHeader();
            writer.write("ID");
            for(int i = 0; i < statId.getValue().length; i++){
                writer.write(SEPARATOR + statId.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("elevation");
            for(int i = 0; i < statElev.getValue().length; i++){
                writer.write(SEPARATOR + statElev.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("x");
            for(int i = 0; i < statX.getValue().length; i++){
                writer.write(SEPARATOR + statX.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("y");
            for(int i = 0; i < statY.getValue().length; i++){
                writer.write(SEPARATOR + statY.getValue()[i]);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("dataColumn");
            for(int i = 0; i < statX.getValue().length; i++){
                int col = i+1;
                writer.write(SEPARATOR + col);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.writeLine(J2KTSDataStore.TAGNAME_DATAVAL);
        } else {

            //create and write a header from station names
            int cols = this.statNames.getValue().length + 1;
            String[] hdr = new String[cols];
            hdr[0] = "date";
            for(int i = 1; i < cols; i++)
                hdr[i] = this.statNames.getValue()[i-1];
            this.statNames.setValue(hdr);

            for (int i = 0; i < statNames.getValue().length; i++) {
                writer.addColumn(statNames.getValue()[i]);
            }
            writer.writeHeader();
        }
        getModel().getRuntime().println(" end init " + fileName.getValue() + ".. ", JAMS.VERBOSE);
    }

    @Override
    public void run() {
        writer.addData(time.toString(dateFormat));
        for(int i = 0; i < values.getValue().length;i++){
            writer.addData(values.getValue()[i], precision.getValue());
        }
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().println(jre.getMessage());
        }
        double[] n = null;
        values.setValue(n);

    }

    @Override
    public void cleanup() {
        try {
            writer.writer.flush();
            writer.writer.close();
        } catch (IOException ex) {
        }
        StringTokenizer st = new StringTokenizer(this.dataSetDesc.getValue(), " ");
        String dispName = st.nextToken();

        GenericDataWriter xmlWri = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspace().getOutputDataDirectory().getPath(),xmlDSD.getValue()));
        xmlWri.writeLine("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>");
        xmlWri.writeLine("<j2ktsdatastore>");
        xmlWri.writeLine("<parsetime value=\"false\" />");
        xmlWri.writeLine("<dumptimeformat value=\"yyyy-MM-dd HH:mm\" />");
        xmlWri.writeLine("<displayname>"+dispName+"</displayname>");
        xmlWri.writeLine("</j2ktsdatastore>");
        try {
            xmlWri.writer.flush();
            xmlWri.writer.close();
        } catch (IOException ex) {
        }
    }
}
