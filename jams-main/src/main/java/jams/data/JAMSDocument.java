/*
 * JAMSString.java
 * Created on 28. September 2005, 15:11
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
package jams.data;

import org.w3c.dom.*;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class JAMSDocument implements Attribute.Document {

    private Document value;

    /** Creates a new instance of JAMSString */
    JAMSDocument() {
    }

    JAMSDocument(Document value) {
        this.value = value;
    }

    public Document getValue() {
        return value;
    }

    public void setValue(Document value) {
        this.value = value;
    }
    
    public void setValue(String value) {
        try{
            this.value = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(value)));
        }catch(Exception e){
            System.out.println(JAMS.i18n("cant_parse_string_to_xml_document,_because") + e.toString() + value);
        }
    }
  
    public String toString() {
        return value.toString();
    }

}
