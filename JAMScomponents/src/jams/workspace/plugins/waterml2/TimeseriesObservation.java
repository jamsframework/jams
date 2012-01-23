/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.workspace.plugins.waterml2;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author chris
 */
public class TimeseriesObservation {

    private OM2Metadata metadata;
    private Result result;

    private String id;
    private String name;

    private GMLLocation location;

    public TimeseriesObservation(Node node_TimeseriesObservation) throws WaterML2Exception{
        Element e = (Element)node_TimeseriesObservation;
        id = e.getAttribute("gml:id");

        NodeList list = node_TimeseriesObservation.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().matches(".*type")) {
            } else if (node.getNodeName().matches(".*metadata")) {
                setMetadata(new OM2Metadata(node));
            } else if (node.getNodeName().matches(".*relatedObservation")) {
            } else if (node.getNodeName().matches(".*phenomenonTime")) {
            } else if (node.getNodeName().matches(".*resultTime")) {
            } else if (node.getNodeName().matches(".*validTime")) {
            } else if (node.getNodeName().matches(".*procedure")) {
            } else if (node.getNodeName().matches(".*parameter")) {
            } else if (node.getNodeName().matches(".*observedProperty")) {
            } else if (node.getNodeName().matches(".*featureOfInterest")) {
            } else if (node.getNodeName().matches(".*resultQuality")) {
            } else if (node.getNodeName().matches(".*result")) {
                setResult(new Result(node));
            } else if (node.getNodeName().matches(".*location")) {
                location = new GMLLocation(node);
            } else if (node.getNodeName().matches(".*name")) {
                if (node.getFirstChild()!=null)
                    name = node.getFirstChild().getNodeValue();
            }
        }
    }

    public GMLLocation getLocation(){
        return location;
    }
    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }
    public Result getResult(){
        return result;
    }
    private void setResult(Result result) {
        this.result = result;
    }

    public OM2Metadata getMetadata(){
        return this.metadata;
    }
    private void setMetadata(OM2Metadata metadata) {
        this.metadata = metadata;
    }
}
