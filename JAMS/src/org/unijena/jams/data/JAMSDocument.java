/*
 * JAMSString.java
 * Created on 28. September 2005, 15:11
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.data;

import org.w3c.dom.*;
import java.io.Serializable;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author S. Kralisch
 */
public class JAMSDocument implements JAMSData, Serializable {

    private Document value;

    /** Creates a new instance of JAMSString */
    public JAMSDocument() {
    }

    public JAMSDocument(Document value) {
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
            System.out.println("can´t parse string to xml document, because" + e.toString() + value);
        }
    }
  
    public String toString() {
        return value.toString();
    }

}
