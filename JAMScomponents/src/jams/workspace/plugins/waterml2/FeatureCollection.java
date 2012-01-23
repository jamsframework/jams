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
public class FeatureCollection {

    Metadata metadata = null;
    ArrayList<ObservationMember> observations = new ArrayList<ObservationMember>();

    public FeatureCollection(Node featureCollectionNode) throws WaterML2Exception {
        NodeList nodeList = featureCollectionNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeName().matches(".*metaDataProperty")) {
            } else if (node.getNodeName().matches(".*description")) {
            } else if (node.getNodeName().matches(".*descriptionReference")) {
            } else if (node.getNodeName().matches(".*identifier")) {
            } else if (node.getNodeName().matches(".*name")) {
            } else if (node.getNodeName().matches(".*boundedBy")) {
            } else if (node.getNodeName().matches(".*location")) {
            } else if (node.getNodeName().matches(".*metadata")) {
                if (hasDocumentMetadata()) {
                    throw new WaterML2Exception.DuplicateEntryException(node.getNodeName());
                }
                setMetadata(new Metadata(node));
            } else if (node.getNodeName().matches(".*temporalExtent")) {
            } else if (node.getNodeName().matches(".*parameter")) {
            } else if (node.getNodeName().matches(".*phenomenalDirectory")) {
            } else if (node.getNodeName().matches(".*qualifierDirectory")) {
            } else if (node.getNodeName().matches(".*processingDirectory")) {
            } else if (node.getNodeName().matches(".*samplingFeatureMember")) {
            } else if (node.getNodeName().matches(".*observationMember")) {
                addObservationMember(new ObservationMember(node));
            } else if (node.getNodeName().matches(".*extension")) {
            } 
        }
    }

    private void setMetadata(Metadata docMetadata) {
        this.metadata = docMetadata;
    }

    private boolean hasDocumentMetadata() {
        return metadata != null;
    }

    private void addObservationMember(ObservationMember observation) {
        observations.add(observation);
    }
}
