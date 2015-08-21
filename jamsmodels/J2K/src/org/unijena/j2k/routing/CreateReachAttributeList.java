/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.routing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.StringTokenizer;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.awt.Point;
import javax.naming.directory.NoSuchAttributeException;
/**
 *
 * @author sa63kul
 */
public class CreateReachAttributeList extends JAMSComponent {
 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
            public Attribute.EntityCollection entities;
    
 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope"
            )
            public Attribute.Double slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute width"
            )
            public Attribute.Double width;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute roughness"
            )
            public Attribute.Double roughness;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = ""
            )
            public Attribute.Entity information;
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage"
            )
            public Attribute.Double reachID; 
      
    
    public void init()throws Attribute.Entity.NoSuchAttributeException
{

}
    
@SuppressWarnings("unchecked")    
public void run()throws Attribute.Entity.NoSuchAttributeException{
 
// Attribute.Entity entity = entities.getCurrent();
 double reachID=this.reachID.getValue();
 double width = this.width.getValue();
 double slope = this.slope.getValue();
 double rough = this.roughness.getValue();
 
 ArrayList<ArrayList<Point>> actreachlist = new ArrayList<ArrayList<Point>>();
        
        try
        {actreachlist=(ArrayList<ArrayList<Point>>)information.getObject("reachlist");}
        catch (Exception e) {
	    System.out.println(e.toString());
            e.printStackTrace();
	}
  
 double [][] actreachinfo =null; 
 
        try
        {actreachinfo=(double[][])information.getObject("reachinfo");}
        catch (Exception e) {
	    System.out.println(e.toString());
            e.printStackTrace();
	}
 
actreachinfo[0][(int)reachID]=reachID;
actreachinfo[1][(int)reachID]=slope; 
actreachinfo[2][(int)reachID]=width;
actreachinfo[3][(int)reachID]=rough;


information.setObject("reachinfo", actreachinfo);

}

   
public void cleanup() 
{
}
}
