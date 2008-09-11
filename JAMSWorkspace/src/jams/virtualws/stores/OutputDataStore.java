/*
 * OutputDataStore.java
 * Created on 9. September 2008, 22:23
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

package jams.virtualws.stores;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import jams.virtualws.VirtualWorkspace;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDataStore {
    
    private static final String CONTEXT_STRING = "context";
    private static final String TRACE_STRING = "trace";
    private static final String ATTRIBUTE_STRING = "attribute";
    private String context;
    private String[] attributes;

    public OutputDataStore(VirtualWorkspace ws, Document doc) {
        
        Element root = doc.getDocumentElement();
        context = root.getAttribute(CONTEXT_STRING);
        NodeList traceNodes = root.getElementsByTagName(TRACE_STRING);
        
        int length = traceNodes.getLength();
        attributes = new String[length];
        
        for (int i = 0; i < length; i++) {
            Element traceElement = (Element) traceNodes.item(i);
            attributes[i] = traceElement.getAttribute(ATTRIBUTE_STRING);
        }
        
    }

    public String getContext() {
        return context;
    }

    public String[] getAttributes() {
        return attributes;
    }

}
