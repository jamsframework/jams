/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.workspace.plugins.waterml2;

import java.util.StringTokenizer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author chris
 */
public class GMLPoint {

    double x,y,h;

    int dimension = 0;

    public GMLPoint(Node nodePoint) throws WaterML2Exception {
        Element elem = (Element)nodePoint;
        String srsName = elem.getAttribute("srsName");
        String dim = elem.getAttribute("srsDimension");

        try{
            dimension = Integer.parseInt(dim);
        }catch(NumberFormatException nfe){
            nfe.printStackTrace();
            throw new WaterML2Exception("error: srs dimension in wrong number format:" + dim);
        }

        NodeList list = nodePoint.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if (node.getNodeName().matches(".*pos")) {
                if (node.getFirstChild()!=null){
                    String pos = node.getFirstChild().getNodeValue();
                    StringTokenizer tok = new StringTokenizer(pos," ");
                    if (tok.countTokens() != dimension){
                        throw new WaterML2Exception("error: wrong number of dimensions:" + pos);
                    }
                    try{
                        x = Double.parseDouble(tok.nextToken());
                        y = Double.parseDouble(tok.nextToken());
                        h = Double.parseDouble(tok.nextToken());
                    }catch(NumberFormatException nfe){
                        nfe.printStackTrace();
                        throw new WaterML2Exception("error: location in wrong number format:" + pos);
                    }
                }
            }
        }
    }


}
