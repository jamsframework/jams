/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.workspace.plugins.waterml2;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author chris
 */
public class Result {

    Timeserie timeserie;

    public Result(Node resultNode) throws WaterML2Exception{
        NodeList list = resultNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().matches(".*Timeseries")) {
                timeserie = new Timeserie(node);
            }
        }
    }   
}
