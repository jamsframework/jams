/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.workspace.plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author chris
 */
public class J2KHeader {

    public static SimpleDateFormat j2kSdf = new SimpleDateFormat("dd.MM.yyyy HH:mm"){
        {
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    };

    public enum TimePeriod {

        MINUTE, HOUR, DAY, WEEK, MONTH, YEAR, UNDEFINED
    };
    private boolean isInputFile;

    public boolean getIsInputFile() {
        return isInputFile;
    }

    public void setIsInputFile(boolean isInputFile) {
        this.isInputFile = isInputFile;
    }
    private int timeStepCount;

    public int getTimeStepCount() {
        return timeStepCount;
    }

    public void setTimeStepCount(int timeStepCount) {
        this.timeStepCount = timeStepCount;
    }
    private Date dateStart;

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }
    private Date dateEnd;

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }
    private String property;

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
    private String unit;

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    private double lowerBound;

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getLowerBound() {
        return this.lowerBound;
    }
    private double upperBound;

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public double getUpperBound() {
        return this.upperBound;
    }
    private double missingDataValue;

    public double getMissingDataValue() {
        return missingDataValue;
    }

    public void setMissingDataValue(double missingDataValue) {
        this.missingDataValue = missingDataValue;
    }
    private int elementCount;

    public int getElementCount() {
        return elementCount;
    }

    public void setElementCount(int elementCount) {
        this.elementCount = elementCount;
    }
    private String[] stationNames;

    public String[] getStationNames() {
        return stationNames;
    }

    public void setStationNames(String stationNames[]) {
        this.stationNames = stationNames;
    }
    private int[] stationIDs;

    public int[] getStationIDs() {
        return stationIDs;
    }

    public void setStationIDs(int stationIDs[]) {
        this.stationIDs = stationIDs;
    }
    private double[] elevation;

    public double[] getElevation() {
        return elevation;
    }

    public void setElevation(double[] elevation) {
        this.elevation = elevation;
    }
    private double[] x;

    public double[] getX() {
        return x;
    }

    public void setX(double[] x) {
        this.x = x;
    }
    private double[] y;

    public double[] getY() {
        return y;
    }

    public void setY(double[] y) {
        this.y = y;
    }
    private int[] column;

    public int[] getColumn() {
        return this.column;
    }

    public void setColumn(int[] column) {
        this.column = column;
    }
    private TimePeriod timeStepUnit;

    public TimePeriod getTimeStepUnit() {
        return timeStepUnit;
    }

    public void setTimeStepUnit(TimePeriod timeStepUnit) {
        this.timeStepUnit = timeStepUnit;
    }
    private int timestep;

    public int getTimeStep() {
        return timestep;
    }

    private void setTimeStep(int timestep) {
        this.timestep = timestep;
    }
    private String spatialReferenceID;

    public String getSpatialReferenceID() {
        return this.spatialReferenceID;
    }

    public void setSpatialReferenceID(String spatialReferenceID) {
        this.spatialReferenceID = spatialReferenceID;
    }
    private String spatialDomainID;

    public String getSpatialDomainID() {
        if (spatialDomainID != null) {
            return spatialDomainID;
        } else {
            return "spatial id not available!";
        }
    }

    public void setSpatialDomainID(String spatialDomainID) {
        this.spatialDomainID = spatialDomainID;
    }
    private String spatialDomainDescription;

    public String getSpatialDomainDescription() {
        return spatialDomainDescription;
    }

    public void setSpatialDomainDescription(String spatialDomainDescription) {
        this.spatialDomainDescription = spatialDomainDescription;
    }
    private String quantityDescription;

    public String getQuantityDescription() {
        return quantityDescription;
    }

    public void setQuantityDescription(String quantityDescription) {
        this.quantityDescription = quantityDescription;
    }
    private String unitDescription;

    public String getUnitDescription() {
        if (unitDescription != null) {
            return unitDescription;
        } else {
            return "unit description not available!";
        }
    }

    public void setUnitDescription(String unitDescription) {
        this.unitDescription = unitDescription;
    }

    private Date addTimePeriod(Date date, TimePeriod period, int count) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        switch (period) {
            case DAY:
                calendar.add(Calendar.DAY_OF_YEAR, count);
                return calendar.getTime();
            case HOUR:
                calendar.add(Calendar.HOUR, count);
                return calendar.getTime();
            case MINUTE:
                calendar.add(Calendar.MINUTE, count);
                return calendar.getTime();
            case MONTH:
                calendar.add(Calendar.MONTH, count);
                return calendar.getTime();
            case WEEK:
                calendar.add(Calendar.WEEK_OF_YEAR, count);
                return calendar.getTime();
            case YEAR:
                calendar.add(Calendar.YEAR, count);
                return calendar.getTime();
        }
        return date;
    }

    public class J2KHeaderException extends Exception {

        String exception;

        public J2KHeaderException(String exception) {
            this.exception = exception;
        }

        @Override
        public String toString() {
            return this.exception;
        }
    }

    public String writeHeader() {
        //template file .. equals J2K File format
                    /*
         * @dataConnection
         * mode = INPUT
         * @dataValueAttribs
         * rain 0 9999 mm
         * @dataSetAttribs
         * missingDataVal -9999
         * dataStart	01.01.1979 07:30
         * dataEnd	31.12.2000 07:30
         * tres	d
         * @statAttribVal
         * name	stat1	stat2	stat3	stat4	stat5	stat6	stat7	stat8	stat9	stat10	stat11	stat12	stat13	stat14
         * ID	1574	1513	1445	1440	1502	1560	1431	1559	1517	1680	1613	1388	1339	1309
         * elevation	525	498	325	469	360	286	360	262	155	316	937	475	528	321
         * x	4402310	4422269	4423636	4414207	4408462	4416709	4434256	4440297	4470713	4427446	4412785	4402455	4409474	4406282
         * y	5620906	5616856	5629818	5628111	5635626	5637345	5633382	5648145	5644173	5650160	5613300	5628317	5626336	5644937
         * dataColumn	1	2	3	4	5	6	7	8	9	10	11	12	13	14
         */
        
        j2kSdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        String header = "";
        header += "@dataConnection" + "\n";
        if (this.isInputFile)
            header += "mode\tINPUT" + "\n";
        else
            header += "mode\tOUTPUT" + "\n";
        header += "@dataValueAttribs" + "\n";
        //this information is not accessible :(
        header += this.getProperty() + "\t" + this.getLowerBound() + "\t" + this.getUpperBound() + "\t" + "??" + "\n";
        header += "@dataSetAttribs" + "\n";
        header += "missingDataVal" + "\t" + "-9999" + "\n";
        header += "dataStart\t" + j2kSdf.format(dateStart) + "\n";
        header += "dataEnd\t" + j2kSdf.format(dateEnd) + "\n";

        switch (this.timeStepUnit) {
            case DAY:
                this.setTimeStep(86400);
                break;
            case HOUR:
                this.setTimeStep(3600);
                break;
            default:
                this.setTimeStep(86400);
        }

        if (this.getTimeStep() == 3600) {
            header += "tres\th" + "\n";
        } else if (this.getTimeStep() == 86400) {
            header += "tres\td" + "\n";
        }
        header += "@statAttribVal" + "\n";

        String nameLine = "name\t";
        String idLine = "ID\t";
        String xLine = "x\t";
        String yLine = "y\t";
        String elevationLine = "elevation\t";
        String dataColumnLine = "dataColumn\t";

        for (int i = 0; i < this.stationIDs.length; i++) {
            nameLine += "SO_" + this.getStationNames()[i] + "\t";
            idLine += this.getStationIDs()[i] + "\t";

            xLine += this.getX()[i] + "\t";
            yLine += this.getY()[i] + "\t";
            elevationLine += this.getElevation()[i] + "\t";
            dataColumnLine += (i + 1) + "\t";

        }

        header += nameLine + "\n";
        header += idLine + "\n";
        header += xLine + "\n";
        header += yLine + "\n";
        header += elevationLine + "\n";
        header += dataColumnLine + "\n";
        header += "@dataVal\n";

        return header;
    }

    public void readHeader(String[] lines) throws J2KHeaderException {
        Boolean isDataValueAttribsSet = false;
        Boolean isDataSetAttribsSet = false;
        Boolean isStatAttribValSet = false;

        j2kSdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        int k = 0;
        while (k < lines.length) {
            String line = lines[k++];

            if (line.startsWith("#")) {
                continue;
            }
            line = line.replace("\t", "");
            if (line.toLowerCase().equals("@dataconnection")) {
                String attributesString = lines[k++];
                String[] attributes = attributesString.split("\t");
                if (attributes.length != 2) {
                    throw new J2KHeaderException("dataValueAttribs does not contain enough tokens! found: " + attributes.length + " required: " + 4);
                }
                if (attributes[0].toLowerCase().equals("mode")) {
                    if (attributes[1].toLowerCase().equals("input")) {
                        setIsInputFile(true);
                    } else {
                        setIsInputFile(false);
                    }
                }
            }
            if (line.toLowerCase().equals("@datavalueattribs")) {
                String attributesString = lines[k++];
                String[] attributes = attributesString.split("\t");
                if (attributes.length != 4) {
                    throw new J2KHeaderException("dataValueAttribs does not contain enough tokens! found: " + attributes.length + " required: " + 4);
                }
                property = attributes[0];
                this.lowerBound = Double.parseDouble(attributes[1]);
                this.upperBound = Double.parseDouble(attributes[2]);
                this.unit = attributes[3];

                isDataValueAttribsSet = true;
            }
            if (line.toLowerCase().equals("@datasetattribs")) {
                String attributeString = null;
                while( !(attributeString = lines[k++]).startsWith("@") ){
                    String[] attribute = attributeString.split("\t");
                    if (attribute.length != 2) {
                        throw new J2KHeaderException("dataSetAttribs does not contain enough tokens! found: " + attribute.length + " required: " + 2);
                    }
                    if (attribute[0].toLowerCase().equals("missingdataval")) {
                        this.missingDataValue = Double.parseDouble(attribute[1]);
                    } else if (attribute[0].toLowerCase().equals("datastart")) {
                        try {
                            this.dateStart = j2kSdf.parse(attribute[1]);
                        } catch (ParseException pe) {
                            throw new J2KHeaderException("Unable to parse startDate:" + pe.toString());
                        }

                    } else if (attribute[0].toLowerCase().equals("dataend")) {
                        try {
                            this.dateEnd = j2kSdf.parse(attribute[1]);
                        } catch (ParseException pe) {
                            throw new J2KHeaderException("Unable to parse startDate:" + pe.toString());
                        }
                    } else if (attribute[0].toLowerCase().equals("tres")) {
                        if (attribute[1].equals("h")) {
                            this.setTimeStepUnit(TimePeriod.HOUR);
                            this.setTimeStep(1);
                        }
                        if (attribute[1].equals("d")) {
                            this.setTimeStepUnit(TimePeriod.DAY);
                            this.setTimeStep(1);
                        }
                        if (attribute[1].equals("m")) {
                            this.setTimeStepUnit(TimePeriod.MONTH);
                            this.setTimeStep(1);
                        }
                    } else {
                        throw new J2KHeaderException("unknown or additional or misplaced attribute " + attribute[0]);
                    }
                    isDataSetAttribsSet = true;
                }
            }
            if (line.toLowerCase().equals("@statattribval")) {
                String attributeString = null;
                while( !(attributeString = lines[k++]).startsWith("@") ){

                    String[] attribute = attributeString.split("\t");
                    if (elementCount == 0) {
                        elementCount = attribute.length - 1;
                    } else if (elementCount != attribute.length - 1) {
                        throw new J2KHeaderException("number of stations and attributes is not consistent in attribute: " + attributeString);
                    }
                    if (attribute[0].toLowerCase().equals("name")) {
                        stationNames = new String[elementCount];
                        for (int j = 1; j < attribute.length; j++) {
                            stationNames[j - 1] = attribute[j];
                        }
                    } else if (attribute[0].toLowerCase().equals("id")) {
                        stationIDs = new int[elementCount];
                        for (int j = 1; j < attribute.length; j++) {
                            stationIDs[j - 1] = Integer.parseInt(attribute[j]);
                        }
                    } else if (attribute[0].toLowerCase().equals("elevation")) {
                        elevation = new double[elementCount];
                        for (int j = 1; j < attribute.length; j++) {
                            elevation[j - 1] = Double.parseDouble(attribute[j]);
                        }
                    } else if (attribute[0].toLowerCase().equals("x")) {
                        x = new double[elementCount];
                        for (int j = 1; j < attribute.length; j++) {
                            x[j - 1] = Double.parseDouble(attribute[j]);
                        }
                    } else if (attribute[0].toLowerCase().equals("y")) {
                        y = new double[elementCount];
                        for (int j = 1; j < attribute.length; j++) {
                            y[j - 1] = Double.parseDouble(attribute[j]);
                        }
                    } else if (attribute[0].toLowerCase().equals("datacolumn")) {
                        column = new int[elementCount];
                        for (int j = 1; j < attribute.length; j++) {
                            this.column[j - 1] = Integer.parseInt(attribute[j]);
                        }
                    } else {
                        throw new J2KHeaderException("unknown or additional or misplaced attribute " + attribute[0]);
                    }
                }
                isStatAttribValSet = true;
            }
            if (line.toLowerCase().equals("@dataConnection")) {
                String attributesString = lines[k++];
                String[] attributes = attributesString.split("\t");
                if (attributes.length != 1) {
                    throw new J2KHeaderException("dataConnection does not contain enough tokens! found: " + attributes.length + " required: " + 4);
                }
                if (attributes[0].toLowerCase().equals("mode")) {
                    if (attributes[0].toLowerCase().equals("input")) {
                        this.setIsInputFile(true);
                    } else {
                        this.setIsInputFile(false);
                    }

                }

                isDataValueAttribsSet = true;
            }
            if (line.toLowerCase().equals("@dataval")) {
                break;
            }
        }

        if (!isDataValueAttribsSet) {
            throw new J2KHeaderException("the section @dataValueAttribs is missing");
        }
        if (!isDataSetAttribsSet) {
            throw new J2KHeaderException("the section @DataSetAttribs is missing");
        }
        if (!isStatAttribValSet) {
            throw new J2KHeaderException("the section @StatAttribVal is missing");
        }
        long timeStampStart = dateStart.getTime();
        long timeStampEnd = dateEnd.getTime();

        long timeSteps = (timeStampEnd - timeStampStart);///(24.0*3600.0);

        switch (this.getTimeStepUnit()) {
            case MINUTE:
                this.timeStepCount = this.timeStepCount / (1000 * 60);
                break;
            case HOUR:
                this.timeStepCount = this.timeStepCount / (1000 * 60 * 60);
                break;
            case DAY:
                this.timeStepCount = this.timeStepCount / (1000 * 60 * 60 * 24);
                break;
            case WEEK:
                break;
            case MONTH:
                break;
            case YEAR:
                break;
        }
    }
};
