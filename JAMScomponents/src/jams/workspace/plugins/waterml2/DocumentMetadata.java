/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.workspace.plugins.waterml2;

import java.text.ParseException;
import java.util.GregorianCalendar;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author chris
 */
public class DocumentMetadata {

    String generationSystem;
    String version;
    GregorianCalendar generationTime;

    public DocumentMetadata(Node metaDataNode) throws WaterML2Exception {

        NodeList list = metaDataNode.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().matches(".*generationDate")) {
                
                try {
                    this.generationTime = Common.parseTime(node.getFirstChild().getNodeValue());
                } catch (ParseException pe) {
                    throw new WaterML2Exception("generationDate: " + node.getFirstChild().getNodeValue());
                }
            } else if (node.getNodeName().matches(".*generationSystem")) {
                this.generationSystem = node.getNodeValue();
            } else if (node.getNodeName().matches(".*version")) {
                this.version = node.getNodeValue();
            } 
        }

    }
}
