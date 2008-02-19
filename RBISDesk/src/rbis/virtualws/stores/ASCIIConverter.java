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

import java.util.GregorianCalendar;
import org.unijena.jams.data.JAMSCalendar;
import rbis.virtualws.DataSet;

/**
 *
 * @author Sven Kralisch
 */
public class ASCIIConverter {

    private DataStore store;
    private String commentTag="@comments", metadataTag="@metadata", dataTag="@data";
    
    public ASCIIConverter(DataStore store) {
        this.store = store;
        
    }

    public String toASCIIString() {

        String result = "";

        result += commentTag+"\n";
        result += "#ID: " + store.getID() + "\n";
        result += "#TYPE: " + store.getClass().getSimpleName() + "\n";
        JAMSCalendar creationDate = new JAMSCalendar();
        creationDate.setValue(new GregorianCalendar());
        result += "#DATE: " + creationDate + "\n";
        result += "#RESPPARTY: " + store.getRespParty() + "\n";
        result += "#DESCRIPTION:\n";
        String description = store.getDescription();
        if (!description.equals("")) {
            result += "# " + description.replace("\n", "\n# ") + "\n";
        }

        result += metadataTag+"\n";
        result += store.getDataSetDefinition().toASCIIString() + "\n";

        result += dataTag +"\n";
        while (store.hasNext()) {
            DataSet ds = store.getNext();
            result += ds.toString() + "\n";
        }

        return result;
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
}
