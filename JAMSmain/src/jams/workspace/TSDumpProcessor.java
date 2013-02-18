/*
 * TSDumpProcessor.java
 * Created on 19. Februar 2008, 09:16
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
package jams.workspace;

import jams.data.Attribute;
import jams.workspace.stores.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import jams.data.DefaultDataFactory;

/**
 *
 * @author Sven Kralisch
 */
public class TSDumpProcessor {

    public static final String COMMENT_TAG = "@comments";
    public static final String METADATA_TAG = "@metadata";
    public static final String DATA_TAG = "@data";
    public static final String END_TAG = "@end";

    public String toASCIIString(TSDataStore store) throws IOException {
        StringTarget target = new StringTarget();
        output(store, target);
        return target.buffer.toString();
    }

    public void toASCIIFile(TSDataStore store, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        FileTarget target = new FileTarget(writer);
        output(store, target);
        writer.close();
    }

    /**
     * @todo: Implement method
     * @param file
     * @return
     */
    public TSDataStore fromASCIIFile(File file) {

        return null;
    }

    private TSDataStore input(InputSource source) throws IOException {

        return null;

    }

    private void output(TSDataStore store, OutputTarget target) throws IOException {

        target.append(JAMSWorkspace.DUMP_MARKER + "\n");
        target.append(COMMENT_TAG + "\n");
        target.append("#ID: " + store.getID() + "\n");
        target.append("#TYPE: " + store.getClass().getSimpleName() + "\n");
        target.append("#START: " + store.getStartDate() + "\n");
        target.append("#END: " + store.getEndDate() + "\n");
        target.append("#STEPUNIT: " + store.getTimeUnit() + "\n");
        target.append("#STEPSIZE: " + store.getTimeUnitCount() + "\n");
        target.append("#MISSINGDATAVALUE: " + store.getMissingDataValue() + "\n");

        Attribute.Calendar creationDate = DefaultDataFactory.getDataFactory().createCalendar();
        target.append("#DATE: " + creationDate + "\n");
        target.append("#DESCRIPTION:\n");
        String description = store.getDescription();
        if (!description.equals("")) {
            target.append("# " + description.replace("\n", "\n# ") + "\n");
        }

        target.append(METADATA_TAG + "\n");

        target.append(store.getDataSetDefinition().toASCIIString());

        target.append(DATA_TAG + "\n");
        while (store.hasNext()) {
            DefaultDataSet ds = store.getNext();
            target.append(ds.toString(store.getMissingDataValue()) + "\n");
        }
        target.append(END_TAG);
    }

    interface OutputTarget {

        public void append(String s) throws IOException;
    }

    interface InputSource {

        public String readln() throws IOException;
    }

    class StringTarget implements OutputTarget {

        StringBuffer buffer = new StringBuffer();

        public void append(String s) {
            buffer.append(s);
        }
    }

    class FileTarget implements OutputTarget {

        BufferedWriter writer;

        public FileTarget(BufferedWriter writer) {
            this.writer = writer;
        }

        public void append(String s) throws IOException {
            writer.write(s);
        }
    }
}
