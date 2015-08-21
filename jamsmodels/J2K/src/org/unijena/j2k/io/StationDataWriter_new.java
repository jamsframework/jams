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

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "StationDataWriter",
                          author = "Peter Krause",
                          description = "Writes standard ASCII timeseries data files")
public class StationDataWriter_new extends JAMSComponent{
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

   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "EntitySet"
            )
            public Attribute.EntityCollection entitySet;

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
            description = "the name of the attribute to write",
            defaultValue=EMPTY_CHAR
            )
            public Attribute.String value;

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
        int ent = this.entitySet.getEntityArray().length;
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

            for (int i = 0; i < ent; i++) {
                writer.addColumn((String)this.entitySet.getEntities().get(i).getObject("NAME"));
            }
            writer.writeHeader();
            writer.write("ID");
            for(int i = 0; i < ent; i++){
                writer.write(SEPARATOR + this.entitySet.getEntities().get(i).getInt("ID"));
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("elevation");
            for(int i = 0; i < ent; i++){
                writer.write(SEPARATOR + this.entitySet.getEntities().get(i).getDouble("ELEVATION"));
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("x");
            for(int i = 0; i < ent; i++){
                writer.write(SEPARATOR + this.entitySet.getEntities().get(i).getDouble("X"));
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("y");
            for(int i = 0; i < ent; i++){
                writer.write(SEPARATOR +  this.entitySet.getEntities().get(i).getDouble("Y"));
            }
            writer.writeLine(EMPTY_CHAR);
            writer.write("dataColumn");
            for(int i = 0; i < ent; i++){
                int col = i+1;
                writer.write(SEPARATOR + col);
            }
            writer.writeLine(EMPTY_CHAR);
            writer.writeLine(J2KTSDataStore.TAGNAME_DATAVAL);
        } else {

            //create and write a header from station names
            int cols = ent + 1;
            String[] hdr = new String[cols];
            hdr[0] = "date";
            for(int i = 1; i < cols; i++)
                hdr[i] = (String)this.entitySet.getEntities().get(i-1).getObject("NAME");
            //this.statNames.setValue(hdr);

            //for (int i = 0; i < statNames.getValue().length; i++) {
            //    writer.addColumn(statNames.getValue()[i]);
            //}
            writer.writeHeader();
        }
        getModel().getRuntime().println(" end init " + fileName.getValue() + ".. ", JAMS.VERBOSE);
    }

    @Override
    public void run() {
        int ent = this.entitySet.getEntityArray().length;
        writer.addData(time.toString(dateFormat));
        for(int i = 0; i < ent;i++){
            writer.addData(this.entitySet.getEntities().get(i).getDouble(value.getValue()), precision.getValue());
        }
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().println(jre.getMessage());
        }

    }

    @Override
    public void cleanup() {
        try {
            writer.writer.flush();
            writer.writer.close();
        } catch (IOException ex) {
        }
    }
}
