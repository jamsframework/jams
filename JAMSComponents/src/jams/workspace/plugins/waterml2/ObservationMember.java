/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.workspace.plugins.waterml2;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author chris
 */
public class ObservationMember {

    TimeseriesObservation tsObservation = null;

    public ObservationMember(Node node_observationMember) throws WaterML2Exception {
        NodeList list = node_observationMember.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().matches(".*TimeseriesObservation")) {
                tsObservation = (new TimeseriesObservation(node));
            }
        }
    }   
}
