/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.workspace.plugins.waterml2;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author chris
 */
public class Timeserie {
    private ArrayList<TimeValuePair> result;

    private GregorianCalendar startAnchorPoint=null;
    private GregorianCalendar endAnchorPoint=null;

    private GregorianCalendar baseTime=null;
    private long spacing=0;

    private TimeValuePair defaultTimeValuePair=null;

    private GregorianCalendar currentTime=null;

    public Timeserie(Node timeserieNode) throws WaterML2Exception{
        result = new ArrayList<TimeValuePair>();

        NodeList list = timeserieNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if (node.getNodeName().matches(".*domainExtent")) {

            }else if (node.getNodeName().matches(".*startAnchorPoint")) {
                 try{
                    startAnchorPoint = new GregorianCalendar();
                    startAnchorPoint.setTime(Common.WML2_TIME_FORMAT.parse(node.getNodeValue()));
                }catch(ParseException pe){
                    throw new WaterML2Exception("invalid date: " + node.getNodeValue());
                }
            }else if (node.getNodeName().matches(".*endAnchorPoint")) {
                 try{
                    endAnchorPoint = new GregorianCalendar();
                    endAnchorPoint.setTime(Common.WML2_TIME_FORMAT.parse(node.getNodeValue()));
                }catch(ParseException pe){
                    throw new WaterML2Exception("invalid date: " + node.getNodeValue());
                }

            }else if (node.getNodeName().matches(".*baseTime")) {
                 try{
                    baseTime = new GregorianCalendar();
                    baseTime.setTime(Common.WML2_TIME_FORMAT.parse(node.getNodeValue()));
                    currentTime = (GregorianCalendar)baseTime.clone();
                }catch(ParseException pe){
                    throw new WaterML2Exception("invalid date: " + node.getNodeValue());
                }
            }else if (node.getNodeName().matches(".*spacing")) {
                try{
                    spacing = Long.parseLong(node.getNodeValue());
                }catch(NumberFormatException pe){
                    throw new WaterML2Exception("invalid spacing: " + node.getNodeValue());
                }
            }else if (node.getNodeName().matches(".*cumulative")) {

            }else if (node.getNodeName().matches(".*accumulationAnchorTime")) {

            }else if (node.getNodeName().matches(".*accumulationIntervalLength")) {

            }else if (node.getNodeName().matches(".*aggregationDatum")) {

            }else if (node.getNodeName().matches(".*commentBlock")) {

            }else if (node.getNodeName().matches(".*parameter")) {

            }else if (node.getNodeName().matches(".*defaultTimeValuePair")) {
                defaultTimeValuePair = new TimeValuePair(node,null);
                if (defaultTimeValuePair.unit==null)
                    defaultTimeValuePair.unit = "";

            }else if (node.getNodeName().matches(".*point")) {
                result.add(readPoint(node));
            }
        }
    }

    private TimeValuePair readPoint(Node pointNode) throws WaterML2Exception{
        NodeList list = pointNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeName().matches(".*TimeValuePair")) {
                TimeValuePair tvp = new TimeValuePair(node,defaultTimeValuePair);
                //could be a problem if spacing >100years
                if (currentTime != null){
                    currentTime.add(Calendar.SECOND, (int)this.spacing);
                    if (defaultTimeValuePair==null){
                        defaultTimeValuePair = new TimeValuePair();
                    }
                    defaultTimeValuePair.time = currentTime;
                }
                return tvp;
            }
        }
        return null;
    }

    public TimeValuePair[] getData(){
        TimeValuePair tvp[] = new TimeValuePair[this.result.size()];
        int counter = 0;
        for (TimeValuePair t : result){
            tvp[counter++] = t;
        }
        return tvp;
    }
}
