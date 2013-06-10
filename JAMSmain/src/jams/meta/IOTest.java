/*
 * IOTest.java
 * Created on 20.09.2010, 12:03:58
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import jams.JAMSException;
import jams.ExceptionHandler;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.data.JAMSEntity;
import jams.runtime.JAMSClassLoader;
import jams.runtime.JAMSLogger;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.tools.StringTools;
import jams.tools.XMLTools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class IOTest {

    public static void main(String[] args) throws IOException {

        SystemProperties properties = JAMSProperties.createProperties();
        properties.load("d:/jamsapplication/nsk.jap");
        String[] libs = StringTools.toArray(properties.getProperty("libs", ""), ";");

        JAMSRuntime runtime = new StandardRuntime(properties);

        ClassLoader classLoader = JAMSClassLoader.createClassLoader(libs, new JAMSLogger());

        ModelIO io = new ModelIO(new NodeFactory() {
            public ModelNode createNode(ComponentDescriptor cd) {
                return new ModelNode(cd);
            }
        }, Logger.getLogger("test"));

        Document doc = XMLTools.getDocument("d:/jamsapplication/JAMS-Gehlberg/j2k_gehlberg.jam");

        // get the model and access some meta data

        ModelDescriptor md = null;

        try {
            md = io.loadModelDescriptor(doc, classLoader, true);
        } catch (JAMSException jex) {
            System.out.println(jex);
        }
        System.out.println(md.getAuthor());
        System.out.println(md.getDescription());


        ArrayList<ComponentField> fields = md.getParameterFields();

        for (ComponentField field : fields) {
            System.out.println(field.getParent().getInstanceName() + "." + field.getName());
        }


        // rename a context attribute
        ComponentDescriptor cd = md.getComponentDescriptor("TmeanRegionaliser");
        ComponentField dataValueField = cd.getComponentFields().get("dataValue");
        ArrayList<ContextAttribute> tmean = dataValueField.getContextAttributes();
        ContextAttribute ca = tmean.get(0);
        ca.setName("mytmean");

        // output a node
        output(md.getRootNode(), 0);

        // get all attributes of a context (by type)
        ContextDescriptor context = (ContextDescriptor) md.getComponentDescriptor("HRULoop");
        HashMap<String, ContextAttribute> attribs = context.getAttributes(JAMSEntity.class);
        for (ContextAttribute attrib : attribs.values()) {
            System.out.println(attrib.getName() + " [" + attrib.getType() + "]");
        }

        ContextDescriptor ccopy = context.cloneNode();
        attribs = ccopy.getAttributes(JAMSEntity.class);
        for (ContextAttribute attrib : attribs.values()) {
            attrib.setName(attrib.getName() + "__");
            System.out.println(attrib.getName() + " [" + attrib.getType() + "]");
        }

        attribs = context.getAttributes(JAMSEntity.class);
        for (ContextAttribute attrib : attribs.values()) {
            System.out.println(attrib.getName() + " [" + attrib.getType() + "]");
        }

        Document doc2 = io.getModelDocument(md);

        XMLTools.writeXmlFile(doc, "d:/doc1.xml");
        XMLTools.writeXmlFile(doc2, "d:/doc2.xml");

        // output a component
//        cd = md.getComponentDescriptor("SpatialWeightedSumAggregator1");
//        output(cd, "");


    }

    static void output(ComponentDescriptor cd, String indent) {
        System.out.println(indent + cd.getInstanceName() + " [" + cd.getClazz() + "] " + cd.getNode().getType());
        HashMap<String, ComponentField> fields = cd.getComponentFields();
        for (String fieldName : fields.keySet()) {
            ComponentField field = fields.get(fieldName);
            System.out.println(indent + "    " + fieldName + " [" + field.getContext() + "->" + field.getAttribute() + "] [" + field.getValue() + "]");
        }
    }

    static void output(ModelNode node, int level) {
        String indent = "";
        for (int i = 0; i < level; i++) {
            indent += "    ";
        }
        output((ComponentDescriptor) node.getUserObject(), indent);
        for (int i = 0; i < node.getChildCount(); i++) {
            output((ModelNode) node.getChildAt(i), level + 1);
        }
    }
}
