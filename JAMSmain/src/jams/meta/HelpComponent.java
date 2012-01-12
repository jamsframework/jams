/*
 * HelpComponent.java
 * Created on 15. June 2008, 07:50
 *
 * This file is part of JAMS
 * Copyright (C) 2008 FSU Jena
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
package jams.meta;

import jams.tools.StringTools;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Heiko Busch
 */
public class HelpComponent {

    private String helpURL = "";
    private String helpText = "";
    //public static ImageIcon HELP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/help.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));

    public HelpComponent() {
    }

    /**
     * alternative Constructor, data coming from DOM-element
     * @param domElement - the DOM-Element with help-node
     */
    public HelpComponent(Element domElement) {
        NodeList childNodes = domElement.getChildNodes();
        for (int pindex = 0; pindex < childNodes.getLength(); pindex++) {
            Node node = childNodes.item(pindex);
            if (node.getNodeName().equalsIgnoreCase("help")) {
                Element helpElement = (Element) node;
                NodeList helpTextNodes = helpElement.getElementsByTagName("text");
                for (int l = 0; l < helpTextNodes.getLength(); l++) {
                    Node helpTextNode = helpTextNodes.item(l);
                    helpText = helpTextNode.getTextContent();
                }
                NodeList helpURLNodes = helpElement.getElementsByTagName("url");
                for (int l = 0; l < helpURLNodes.getLength(); l++) {
                    Node helpURLNode = helpURLNodes.item(l);
                    helpURL = helpURLNode.getTextContent();
                }
            }
        }

    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getHelpURL() {
        return helpURL;
    }

    public void setHelpURL(String helpURL) {
        this.helpURL = helpURL;
    }

    public boolean isEmpty() {
        if (hasHelpText() || hasHelpURL()) {
            return false;
        }
        return true;
    }

    public boolean hasHelpText() {
        if (StringTools.isEmptyString(helpText)) {
            return false;
        }
        return true;
    }

    public boolean hasHelpURL() {
        if (StringTools.isEmptyString(helpURL)) {
            return false;
        }
        return true;
    }
    
    /**
     * creates DOM-element from content
     * @param document the document
     * @return the DOM-element
     * @throws org.w3c.dom.DOMException
     */
    public Element createDOMElement(Document document) throws DOMException {
        Element helpElement = (Element) document.createElement("help");
        if (this.hasHelpText()) {
            Element helpTextElement = (Element) document.createElement("text");
            helpTextElement.setTextContent(this.getHelpText());
            helpElement.appendChild(helpTextElement);
        }
        if (this.hasHelpURL()) {
            Element helpURLElement = (Element) document.createElement("url");
            helpURLElement.setTextContent(this.getHelpURL());
            helpElement.appendChild(helpURLElement);
        }
        return helpElement;
    }

}
