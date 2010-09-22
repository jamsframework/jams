/*
 * Context.java
 * Created on 20.09.2010, 12:03:58
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import jams.JAMSProperties;
import jams.SystemProperties;
import jams.data.Attribute;
import jams.data.JAMSEntity;
import jams.runtime.JAMSClassLoader;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.tools.StringTools;
import jams.tools.XMLTools;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class IOTest {

    public static void main(String[] args) throws Exception {

        JAMSRuntime runtime = new StandardRuntime();
        SystemProperties properties = JAMSProperties.createProperties();
        properties.load("D:/jamsapplication/nsk.jap");
        String[] libs = StringTools.toArray(properties.getProperty("libs", ""), ";");
        ClassLoader classLoader = JAMSClassLoader.createClassLoader(libs, runtime);

        ModelIO io = new ModelIO(classLoader);

        Document doc = XMLTools.getDocument("D:/jamsapplication/JAMS-Gehlberg/j2k_gehlberg.jam");

        ModelDescriptor md = io.loadModel(doc);


        System.out.println(md.getAuthor());
        System.out.println(md.getDescription());


        ComponentDescriptor cd = md.getComponentDescriptor("TmeanRegionaliser");
        ArrayList<ContextAttribute> tmean = cd.getComponentFields().get("dataValue").getContextAttributes();
        ContextAttribute ca = tmean.get(0);
        ca.setName("mytmean");
        output(md.getRootNode(), 0);

        ContextDescriptor context = (ContextDescriptor) md.getComponentDescriptor("HRULoop");
        HashMap<String, ContextAttribute> attribs = context.getDynamicAttributes(JAMSEntity.class);
        for (ContextAttribute attrib : attribs.values()) {
            System.out.println(attrib.getName() + " [" + attrib.getType() + "]");
        }

//        ComponentDescriptor cd = md.getComponentDescriptor("SpatialWeightedSumAggregator1");
//        output(cd, "");


    }

    static void output(ComponentDescriptor cd, String indent) {
        System.out.println(indent + cd.getName() + " [" + cd.getClazz() + "]");
        HashMap<String, ComponentDescriptor.ComponentField> fields = cd.getComponentFields();
        for (String fieldName : fields.keySet()) {
            ComponentDescriptor.ComponentField field = fields.get(fieldName);
            System.out.println(indent + "    " + fieldName + " [" + field.getContext() + "->" + field.getAttribute() + "] [" + field.getValue() + "]");
        }
    }

    static void output(JAMSNode node, int level) {
        String indent = "";
        for (int i = 0; i < level; i++) {
            indent += "    ";
        }
        output((ComponentDescriptor) node.getUserObject(), indent);
        for (int i = 0; i < node.getChildCount(); i++) {
            output(node.getChildAt(i), level + 1);
        }
    }
}
