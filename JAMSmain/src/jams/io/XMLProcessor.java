/*
 * XMLProcessor.java
 * Created on 20. März 2006, 09:02
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

import jams.JAMS;
import jams.tools.FileTools;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author S. Kralisch
 */
public class XMLProcessor {

    static String[] regex = {
        "jamsvar class=\".*\" name",
        "compoundcomponent",
        "jamsvar",
        "providervar",
        "spatattrib",
        "provider=",
        "org\\.unijena\\.jams\\.gui",
        "org\\.unijena\\.jamscomponents",
        "org\\.unijena\\.jams\\.",
        "jams\\.model\\.cc\\.",
        "jams\\.model\\.BooleanConditionalContext",
        "jams\\.model\\.DoubleConditionalContext"
    };
    
    static String[] replace = {
        "jamsvar name",
        "contextcomponent",
        "var",
        "cvar",
        "attribute",
        "context=",
        "jams.components.gui",
        "jams.components", 
        "jams.",
        "jams.model.",
        "jams\\.components\\.conditional\\.BooleanConditionalContext",
        "jams\\.components\\.conditional\\.DoubleConditionalContext"
    };

    public static String modelDocConverter(String inFileName) {

        String outFileName = inFileName;

        try {

            String oldDoc = FileTools.fileToString(inFileName);
            String newDoc = oldDoc;

            for (int i = 0; i < regex.length; i++) {
                newDoc = newDoc.replaceAll(regex[i], replace[i]);
            }

            if (!newDoc.contentEquals(oldDoc)) {

                File f = new File(inFileName);
                String fName = "$_" + f.getCanonicalFile().getName();
                String pName = f.getParent();

                outFileName = pName + File.separator + fName;

                FileTools.stringToFile(outFileName, newDoc);
            }


        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return outFileName;
    }

    /**
     * get node of model
     * @param root
     * @return node with attributeName "model"
     */
    public static Node getModelNode(Node root) {
        if (root.getNodeName().equals("model")) {
            return root;
        }
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node model = getModelNode(childs.item(i));
            if (model != null) {
                return model;
            }
        }
        return null;
    }

    /**
     * get workspace path from model
     * @param model
     * @return value of node with attributeName "workspaceDirectory"
     */
    public static String getWorkspacePath(Document model) {
        Element root = model.getDocumentElement();
        Element modelElem = (Element) XMLProcessor.getModelNode(root);

        NodeList childs = modelElem.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeName().equals("var")) {
                if (((Element) node).getAttribute("name").equals("workspaceDirectory")) {
                    return ((Element) node).getAttribute("value");
                }
            }
        }
        return null;
    }

    /**
     * set workspace path to model
     * @param model
     * @param theWorkSpacePath
     * @return true, if node with attributeName "workspaceDirectory" could be found, otherwise false
     */
    public static boolean setWorkspacePath(Document model, String theWorkSpacePath) {
        Element root = model.getDocumentElement();
        Element modelElem = (Element) XMLProcessor.getModelNode(root);

        NodeList childs = modelElem.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeName().equals("var")) {
                if (((Element) node).getAttribute("name").equals("workspaceDirectory")) {
                    ((Element) node).setAttribute("value", theWorkSpacePath);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get the first component of root node
     * @param root - the root node
     * @return first node with attributeName "component" or "contextcomponent"
     */
    static public Node getFirstComponent(Node root) {
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            if (childs.item(i).getNodeName().equals("component") || childs.item(i).getNodeName().equals("contextcomponent")) {
                return childs.item(i);
            }
            Node result = getFirstComponent(childs.item(i));
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * find the appropriate node within context
     * @param context
     * @param attributeName
     * @return Node if attribute "attributeName"  = attributeName or null
     */
    static public Node findComponentNode(Node context, String attributeName) {
        NodeList childs = context.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            String node_name = node.getNodeName();
            if (node_name.equals("component") || node_name.equals("contextcomponent") || node_name.equals("model")) {
                if (((Element) node).getAttribute("name").equals(attributeName)) {
                    return node;
                }
            }
            Node result = findComponentNode(node, attributeName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * get the value of a certain node
     * @param context
     * @param attributeName
     * @return value if attribute "attributeName"  = attributeName or null
     */
    public static String getComponentNodeValue(Node context, String attributeName) {
        Node componentNode = findComponentNode(context, attributeName);
        if (componentNode != null) {
            return ((Element)componentNode).getAttribute("value");
        }
        return null;
    }
}
