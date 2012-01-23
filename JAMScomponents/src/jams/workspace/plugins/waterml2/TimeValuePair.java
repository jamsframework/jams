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
public class TimeValuePair {

    GregorianCalendar time;
    Double value;
    String unit;

    public TimeValuePair(){
        unit = "";
    }
    public TimeValuePair(Node timeValuePairNode, TimeValuePair defaultTimeValuePair ) throws WaterML2Exception {
        NodeList list = timeValuePairNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if (node.getNodeName().matches(".*time")) {
                try{
                    if (node.getFirstChild()==null){
                        throw new WaterML2Exception("invalid timeValuePair date:null");
                    }
                    time=(Common.parseTime(node.getFirstChild().getNodeValue()));
                }catch(ParseException pe){
                    throw new WaterML2Exception("invalid timeValuePair: " + node.getNodeValue());
                }
            } else if (node.getNodeName().matches(".*value")) {
                try{
                    if (node.getFirstChild()==null){
                        throw new WaterML2Exception("invalid timeValuePair value:null");
                    }
                    if (node.getFirstChild().getNodeValue().toLowerCase().compareTo("nan")==0)
                        value = Double.NaN;
                    else if(node.getFirstChild().getNodeValue().toLowerCase().compareTo("inf")==0)
                        value = Double.POSITIVE_INFINITY;
                    else if(node.getFirstChild().getNodeValue().toLowerCase().compareTo("-inf")==0)
                        value = Double.NEGATIVE_INFINITY;
                    else
                        value = Double.parseDouble(node.getFirstChild().getNodeValue());
                }catch(NumberFormatException pe){
                    throw new WaterML2Exception("invalid double value: " + node.getFirstChild().getNodeValue());
                }
            } else if (node.getNodeName().matches(".*unitOfMeasure")) {
                unit = node.getNodeValue();
            } else if (node.getNodeName().matches(".*quality")) {
            } else if (node.getNodeName().matches(".*dataType")) {
            } else if (node.getNodeName().matches(".*processing")) {
            } else if (node.getNodeName().matches(".*qualifier")) {
            }
        }
        if (defaultTimeValuePair != null) {
            if (time == null) {
                time = (GregorianCalendar) defaultTimeValuePair.time.clone();
            }
            if (value == null) {
                value = defaultTimeValuePair.value.doubleValue();
            }
            if (unit == null) {
                unit = defaultTimeValuePair.unit.substring(0);
            }
        }
    }
    public void setTime(GregorianCalendar time){
        this.time = time;
    }
    public GregorianCalendar getTime(){
        return time;
    }

    public void setValue(double value){
        this.value = value;
    }
    public double getValue(){
        return value;
    }

    public void setUnit(String unit){
        this.unit = unit;
    }
    public String getUnit(){
        return unit;
    }
}
