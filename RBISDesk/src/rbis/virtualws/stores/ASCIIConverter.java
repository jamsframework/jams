/*
 * ASCIIConverter.java
 * Created on 19. Februar 2008, 09:16
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package rbis.virtualws.stores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import org.unijena.jams.data.JAMSCalendar;
import rbis.virtualws.DataSet;

/**
 *
 * @author Sven Kralisch
 */
public class ASCIIConverter {

    private DataStore store;
    private String commentTag = "@comments",  metadataTag = "@metadata",  dataTag = "@data",  endTag = "@end";

    public ASCIIConverter(DataStore store) {
        this.store = store;

    }

    public String toASCIIString() {
        StringTarget target = new StringTarget();
        try {
            output(target);
        } catch (IOException ioe) {
        }
        return target.buffer.toString();
    }

    public void toASCIIFile(File file) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        FileTarget target = new FileTarget(writer);
        output(target);
        writer.close();
    }

    private void output(OutputTarget target) throws IOException {

        target.append(commentTag + "\n");
        target.append("#ID: " + store.getID() + "\n");
        target.append("#TYPE: " + store.getClass().getSimpleName() + "\n");
        JAMSCalendar creationDate = new JAMSCalendar();
        creationDate.setValue(new GregorianCalendar());
        target.append("#DATE: " + creationDate + "\n");
        target.append("#RESPPARTY: " + store.getRespParty() + "\n");
        target.append("#DESCRIPTION:\n");
        String description = store.getDescription();
        if (!description.equals("")) {
            target.append("# " + description.replace("\n", "\n# ") + "\n");
        }

        target.append(metadataTag + "\n");
        target.append(store.getDataSetDefinition().toASCIIString() + "\n");

        target.append(dataTag + "\n");
        while (store.hasNext()) {
            DataSet ds = store.getNext();
            target.append(ds.toString() + "\n");
        }
        target.append(endTag);
    }

    public void setCommentTag(String commentTag) {
        this.commentTag = commentTag;
    }

    public void setMetadataTag(String metadataTag) {
        this.metadataTag = metadataTag;
    }

    public void setDataTag(String dataTag) {
        this.dataTag = dataTag;
    }

    interface OutputTarget {

        public void append(String s) throws IOException;
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
