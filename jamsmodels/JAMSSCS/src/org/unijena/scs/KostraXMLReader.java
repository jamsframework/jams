/*
 * KostraXMLReader.java
 *
 * Created on 25. Januar 2007, 17:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.scs;

import jams.data.Attribute;
import java.util.HashMap;
import java.util.Map;
import jams.data.Attribute.Double;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import jams.tools.XMLTools;
import org.w3c.dom.*;
/**
 * component for reading a specific XML file provided by the KOSTRA software of the
 * German Weather Agency (DWD). The content of the file is transferred to two 
 * dimensional table for further processing.
 * @author Christian Fischer
 */
public class KostraXMLReader extends JAMSComponent {
    /**
     * the model's workspace directory<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "workspace directory name"
            )
            public Attribute.String workspaceDir;
    
    /**
     * the KOSTRA XML input file<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU parameter file name"
            )
            public Attribute.String KostraXMLFile;
    
    /**
     * the two dimensional table created from the KOSTRA XML input file<br>
     * access: WRITE<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "the kostra table object as result"
            )
            public Attribute.Entity table;
    
    int numDEntries = 0, numAEntries = 0;
    HashMap<Integer,Map<Integer,Attribute.Double>>	tmpTable;
    HashMap<Integer,Attribute.Double>			tmpHeaderD;
    HashMap<Integer,Attribute.Double>			tmpHeaderA;
    
    Attribute.Double Table[][];
    Attribute.Double HeaderD[];
    Attribute.Double HeaderA[];
    
    /**
     * the component's init() method
     * @throws jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        if(KostraXMLFile.getValue().compareTo("") != 0){
            Document dwdData = null;
            
            tmpTable = new HashMap<Integer,Map<Integer,Attribute.Double>>();
            tmpHeaderD = new HashMap<Integer,Attribute.Double>();
            tmpHeaderA = new HashMap<Integer,Attribute.Double>();
            //read document
            try {
                dwdData = XMLTools.getDocument(workspaceDir.getValue()+"/"+KostraXMLFile.getValue());
            } catch (Exception e) {
                this.getModel().getRuntime().println("Can't read XML because:" + e.toString());
            }
            //iterate over all children an extract information
            Node node = dwdData.getFirstChild();
            
            if (node == null) {
                this.getModel().getRuntime().println("XML - Document contains nothing");
            }
            while (node != null) {
                ProcessNode(node);
                node = node.getNextSibling();
            }
            //convert hashmaps to arrays
            Table = new Attribute.Double[numDEntries][numAEntries];
            HeaderD = new Attribute.Double[numDEntries];
            HeaderA = new Attribute.Double[numAEntries];
            
            for (int i=0;i<numDEntries;i++) {
                HeaderD[i] = this.tmpHeaderD.get(new Integer(i));
                for (int j=0;j<numAEntries;j++) {
                    Table[i][j] = this.tmpTable.get(new Integer(i)).get(new Integer(j));
                }
            }
            for (int i=0;i<numAEntries;i++) {
                HeaderA[i] = this.tmpHeaderA.get(new Integer(i));
            }
            //save arrays in output entity
            table.setObject("table",Table);
            table.setObject("HeaderA",HeaderA);
            table.setObject("HeaderD",HeaderD);
            System.out.println("XML read sucessfull!");
        }
    }
    
    private void ProcessNode(Node node) {
        //D nodes contain interesting data
        if (node.getNodeName().compareTo("D") == 0 && node.getParentNode().getNodeName().compareTo("Daten") == 0) {
            ProcessDNode(node);
            return;
        }
        //skip all other nodes
        Node childnode = node.getFirstChild();
        
        while (childnode != null) {
            ProcessNode(childnode);
            childnode = childnode.getNextSibling();
        }
    }
    
    private void ProcessDNode(Node node) {
        Element element = (Element)node;
        //this is a new row in our table --> add to header
        Attribute.Double dauer = getModel().getRuntime().getDataFactory().createDouble();
        dauer.setValue(java.lang.Double.parseDouble(element.getAttribute("dauer")));
        
        tmpHeaderD.put(new Integer(numDEntries),dauer);
        tmpTable.put(new Integer(numDEntries),new HashMap<Integer,Attribute.Double>());
        
        Node childnode = node.getFirstChild();
        
        int curAEntry = 0;
        //iterate over children
        while (childnode != null) {
            //T nodes contain interesting data
            if (childnode.getNodeName().compareTo("T") == 0) {
                if (numDEntries == 0) {
                    element = (Element)childnode;
                    Attribute.Double a = getModel().getRuntime().getDataFactory().createDouble();
                    a.setValue(java.lang.Double.parseDouble(element.getAttribute("a")));
                    tmpHeaderA.put(new Integer(curAEntry),a);
                }
                ProcessTNode(childnode,numDEntries,curAEntry);
                curAEntry++;
            }
            childnode = childnode.getNextSibling();
        }
        //update number of cols/rows
        if (numAEntries == 0)
            this.numAEntries = curAEntry;
        else if (numAEntries != curAEntry) {
            this.getModel().getRuntime().println("Keine Tabelle in XML File!!");
        }
        this.numDEntries++;
    }
    
    private void ProcessTNode(Node node,int DIndex,int AIndex) {
        Node childnode = node.getFirstChild();
        
        while (childnode != null) {
            if (childnode.getNodeName().compareTo("hN") == 0) {
                Attribute.Double d = getModel().getRuntime().getDataFactory().createDouble();
                d.setValue(java.lang.Double.parseDouble(childnode.getTextContent()));
                this.tmpTable.get(DIndex).put(new Integer(AIndex), d);
            }
            childnode = childnode.getNextSibling();
        }
    }
}
