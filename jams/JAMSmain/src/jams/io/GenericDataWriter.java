/*
 * GenericDataWriter.java
 *
 * Created on 6. Oktober 2005, 01:41
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.io;

import java.io.*;
import java.util.*;
import jams.runtime.RuntimeException;
import jams.JAMS;
import jams.tools.JAMSTools;

/**
 *
 * @author S. Kralisch
 */
public class GenericDataWriter implements Serializable{

    private String fileName;
    public SerializableBufferedWriter writer;
    private ArrayList<String> header = new ArrayList<String>();
    private ArrayList<String> comments = new ArrayList<String>();
    private ArrayList<Object> data;
    private boolean headerClosed = false;

    public GenericDataWriter() {
    }

    public GenericDataWriter(String fileName) {
        setFileName(fileName);
    }

    private void openFile() {
        try {
            writer = new SerializableBufferedWriter(new FileWriter(fileName));
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        openFile();
    }

    public void addColumn(String name) {
        if (!headerClosed) {
            header.add(name);
        }
    }

    public void addComment(String comment) {
        if (!headerClosed) {
            comments.add("# " + comment);
        }
    }

    public void writeHeader() {

        Iterator<String> i;
        String s = "";

        i = comments.iterator();
        while (i.hasNext()) {
            s += i.next() + "\n";
        }

//        String s = "#JAMS output file\n#\n";
        //s += "#";
        i = header.iterator();
        if (i.hasNext()) {
            s += i.next();
        }
        while (i.hasNext()) {
            s += "\t" + i.next();
        }
        try {
            writer.write(s);
            writer.newLine();
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
        headerClosed = true;
        data = new ArrayList<Object>(header.size());
    }

    public void addData(Object o) {
        data.add(o);
    }

    public void addData(double val, int prec) {
        String fStr = null;
        fStr = "%." + prec + "f";

        String dStr = String.format(Locale.US, fStr, val);
        data.add(dStr);
    }

    public void writeLine(String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }

    public void write(String line) {
        try {
            writer.write(line);
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }

    public void writeData() throws RuntimeException {

        String s = "";

        if (data.size() != header.size()) {
            throw new RuntimeException(JAMS.i18n("Wrong_number_of_output_columns!"));
        } else {
            Iterator<Object> i = data.iterator();
            if (i.hasNext()) {
                s = i.next().toString();
            }
            while (i.hasNext()) {
                s += "\t" + i.next().toString();
            }
            try {
                writer.write(s);
                writer.newLine();
            } catch (IOException ioe) {
                JAMSTools.handle(ioe);
            }
            data.clear();
        }
    }

//    public void writeData(int prec) throws RuntimeException {
//
//        String s = "";
//
//        if (data.size() != header.size()) {
//            throw new RuntimeException(JAMS.i18n("Wrong_number_of_output_columns!"));
//        } else {
//            Iterator<Object> i = data.iterator();
//            //date first
//            if (i.hasNext()) {
//                s = i.next().toString();
//            }
//            while (i.hasNext()) {
//                double val = ((JAMSDouble) i.next()).getValue();
//                String dStr = String.format(Locale.US, "%." + prec + "f", val);
//                s += "\t" + dStr;
//            //s += "\t"+i.next().toString();
//            }
//            try {
//                writer.write(s);
//                writer.newLine();
//            } catch (IOException ioe) {
//                JAMSTools.handle(ioe);
//            }
//            data.clear();
//        }
//    }

    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }
}
