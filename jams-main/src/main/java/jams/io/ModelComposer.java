/*
 * ModelComposer.java
 * Created on 26. Januar 2009, 19:34
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
package jams.io;

import jams.tools.XMLTools;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ModelComposer {

    private Document template;

    private HashMap<String, Element> pluginBlocks;

    public ModelComposer(Document template) {
        this.template = template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(Document template) {
        this.template = template;
    }

    /**
     * Combines the model template represented by this ModelComposer instance 
     * with a given plugin document and returns the result
     * @param plugin A XML document representing a plugin, i.e. a number of
     * components to be inserted in some model template
     * @return A model as a result of inserting the components from the provided
     * plugin into the model template
     */
    public Document loadPlugin(Document plugin) {

        pluginBlocks = new HashMap<String, Element>();

        NodeList blocks = plugin.getElementsByTagName("block");

        for (int i = 0; i < blocks.getLength(); i++) {
            Element block = (Element) blocks.item(i);
            pluginBlocks.put(block.getAttribute("name"), block);
        }

        Document result = (Document) template.cloneNode(true);
        NodeList slots = result.getElementsByTagName("slot");

        for (int i = 0; i < slots.getLength(); i++) {
            Element slot = (Element) slots.item(i);

            // get the next block node and iterate over it's childs
            Element block = pluginBlocks.get(slot.getAttribute("name"));
            NodeList blockNodes = block.getChildNodes();

            for (int j = 0; j < blockNodes.getLength(); j++) {

                Node blockNode = blockNodes.item(j);
                if (blockNode.getNodeName().equals("contextcomponent") || blockNode.getNodeName().equals("component")) {
                    Node newNode = result.importNode(blockNode, true);
                    slot.getParentNode().insertBefore(newNode, slot);
                }
            }
        }

        // do the launcher stuff

        // put all the group nodes from the template into a hashmap for
        // easy access
        HashMap<String, Element> launcherGroups = new HashMap<String, Element>();
        Element templateLauncher = (Element) result.getElementsByTagName("launcher").item(0);
        NodeList groupNodes = templateLauncher.getElementsByTagName("group");
        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element group = (Element) groupNodes.item(i);
            launcherGroups.put(group.getAttribute("name"), group);
        }

        // get the group element(s) from the plugin and check where to insert 
        // them into the result document
        Element pluginLauncher = (Element) plugin.getElementsByTagName("launcher").item(0);
        groupNodes = pluginLauncher.getElementsByTagName("group");
        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element group = (Element) groupNodes.item(i);

            // check if there is already a group with the same name
            String groupName = group.getAttribute("name");
            Element templateGroup = launcherGroups.get(groupName);
            if (templateGroup != null) {

                // append the property nodes from the plugin's group node
                // to the existing template group node
                NodeList propertyNodes = group.getChildNodes();
                for (int j = 0; j < propertyNodes.getLength(); j++) {
                    Node newNode = result.importNode(propertyNodes.item(j), true);
                    templateGroup.appendChild(newNode);
                }

            } else {

                // append the whole plugin group node to the launcher node
                // of the template
                Node newNode = result.importNode(group, true);
                templateLauncher.appendChild(newNode);

            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {

        Document plugin = XMLTools.getDocument("D:/jamsapplication/j2k_gehlberg_snow_plugin.jam");
        Document template = XMLTools.getDocument("D:/jamsapplication/j2k_gehlberg_template.jam");
        ModelComposer comp = new ModelComposer(template);

        Document result = comp.loadPlugin(plugin);

        XMLTools.writeXmlFile(result, "D:/jamsapplication/j2k_gehlberg_result.jam");

    //System.out.println(XMLTools.getStringFromDocument(result));

    }
}
