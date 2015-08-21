/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.routing;

import jams.data.*;
import jams.data.Attribute.Entity;
import jams.model.*;
import java.util.ArrayList;
import java.awt.Point;




@JAMSComponentDescription
(
        title="J2KRasterRouting",
        author="Christin Michel",
        description=""
)
/**
 *
 * @author sa63kul
 */
  
  public class ReachToRaster  extends JAMSComponent {
   
                     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.EntityCollection entities;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reservoir objects"
            )
            public Attribute.EntityCollection reservoirs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 inflow"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 inflow"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 inflow"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 inflow"
            )
            public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RD1 outflow"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RD2 outflow"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RG1 outflow"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RG2 outflow"
            )
            public Attribute.Double outRG2;
    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 storage"
            )
            public Attribute.Double actRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 storage"
            )
            public Attribute.Double actRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 storage"
            )
            public Attribute.Double actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage"
            )
            public Attribute.Double actRG2;       
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage"
            )
            public Attribute.Double reachID;    
     
   
       
    
       
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = ""
            )
            public Attribute.Entity information;
       
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = ""
            )
            public Attribute.Entity fuellstand;
       
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Downstream hru entity"
            )
            public Attribute.Entity toPoly;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Downstream reach entity"
            )
            public Attribute.Entity toReach;
    
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar groundwater excess"
            )
            public Attribute.Double inGWExcess;
       







public double[][] HRU_to_Raster(double ID,ArrayList hrulist, double wert, double[][] fuellstand)
{
    //fuellstand=null; 
    int laenge1,laenge2;
    double hru=0.0,durchschnitt=0.0;
    Point p = new Point();
    
    laenge1=hrulist.size();
    for(int j=0;j<laenge1;j++)
        {   
            p=(Point)((ArrayList)hrulist.get(j)).get(0);//sucht ID der HRU
                if (p.x==ID)
                {
                    laenge2=((ArrayList)hrulist.get(j)).size();//Anzahl der Rasterzellen in einer HRU
                    durchschnitt=wert/(laenge2-1);
           
                    for(int i=1;i<laenge2;i++)
                    {
                    //teilt den Abfluss gleichm??ig auf alle Rasterzellen einer HRU auf.
                    p=(Point)((ArrayList)hrulist.get(j)).get(i);
                    fuellstand[p.x][p.y]=durchschnitt;
                 
                    }
                }
        }
    
    return fuellstand;
}



public void init()
{
    
}
    
@SuppressWarnings("unchecked")    
public void run()throws Attribute.Entity.NoSuchAttributeException
{
     
    //Variablendeklaration
    
     double[][] actfliessrichtung=null;
     double[][] actdgm=null;
     double[][] acthruraster=null;
   
     double[][] actflgew=null;
     double[][] actflgew1=null;
     double[][] acthangneigung=null;
     int rastersize=25;
     int time=60*60;
     double ID=reachID.getValue();
     
     double[][] RD1raster=null;
     double[][] RD1reach=null;
     double[][] RD2raster=null;
     double[][] RD2reach=null;
     double[][] RG1raster=null;
     double[][] RG1reach=null;
     double[][] RG2raster=null;
     double[][] RG2reach=null;
         
       
       
     
     Point p = new Point();
     
     
     
    
     
     try
        {acthruraster=(double[][])information.getObject("hruraster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
       
        try
        {actfliessrichtung=(double[][])information.getObject("fliessrichtung");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
             
        try
        {actdgm=(double[][])information.getObject("dgm");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
            
        try
        {actflgew=(double[][])information.getObject("flgew");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
            
        try
        {actflgew1=(double[][])information.getObject("flgew1");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
            
        try
        {acthangneigung=(double[][])information.getObject("hangneigung");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
           
        int actdgm_sort[][]=new int[3][actdgm.length*actdgm[0].length]; //Array, indem die H?henwerte sortiert gespeichert werden
       
        try
        {actdgm_sort=(int[][])information.getObject("dgm_sort");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
          
        ArrayList<ArrayList<Point>> actreachlist = new ArrayList<ArrayList<Point>>();
        
        try
        {actreachlist=(ArrayList<ArrayList<Point>>)information.getObject("reachlist");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
           
        //Bestimmen in welche Rasterzelle
        //die jeweilige Rasterzelle entwaessert (in Abhaengigkeit von der Hangneigung)
        
        Point [][] actrouting =new Point [actdgm.length][actdgm[0].length];   
        
        try
        {actrouting=(Point [][])information.getObject("routing");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RD1reach=(double[][])fuellstand.getObject("RD1_reach");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RD2reach=(double[][])fuellstand.getObject("RD2_reach");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RG1reach=(double[][])fuellstand.getObject("RG1_reach");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RG2reach=(double[][])fuellstand.getObject("RG2_reach");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RD1raster=(double[][])fuellstand.getObject("RD1_raster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RD2raster=(double[][])fuellstand.getObject("RD2_raster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RG1raster=(double[][])fuellstand.getObject("RG1_raster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RG2raster=(double[][])fuellstand.getObject("RG2_raster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
   
         Entity entity = entities.getCurrent();
        
        
        Entity toReservoir = null;
        try{
            toReservoir = (Attribute.Entity)entity.getObject("to_reservoir");
        }catch(Attribute.Entity.NoSuchAttributeException e){
            toReservoir = null;
        }
        double RD1out = outRD1.getValue();
        double RD2out = outRD2.getValue();
        double RG1out = outRG1.getValue();
        double RG2out = outRG2.getValue();
        
        double RD1act=actRD1.getValue();
        double RD2act=actRD2.getValue();
        double RG1act=actRG1.getValue();
        double RG2act=actRG2.getValue();
        
        double RD1in=inRD1.getValue();
        double RD2in=inRD2.getValue();
        double RG1in=inRG1.getValue();
        double RG2in=inRG2.getValue();
        
       
        
          
            RD1act = RD1in + RD1act;
            RD2act=  RD2in + RD2act;
            RG1act = RG1in + RG1act;
            RG2act = RG2in + RG2act;
            
            RD2in += inGWExcess.getValue();
            inGWExcess.setValue(0);
         
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
       
            //hier muss noch einiges optimiert werden
        for (int i=0;i<actreachlist.size();i++)
        {
            p=(Point)(((ArrayList)actreachlist.get(i)).get(0));
            if (ID==p.x)
            {    
            RD1reach[i][0]=p.x;
            RD1reach[i][1]=RD1act;
            
            RD2reach[i][0]=p.x;
            RD2reach[i][1]=RD2act;
            
            RG1reach[i][0]=p.x;
            RG1reach[i][1]=RG1act;
            
            RG2reach[i][0]=p.x;
            RG2reach[i][1]=RG2act;
            }
        }
         
        //Verteilen des Wassers auf das Raster
        RD1raster=HRU_to_Raster(ID,actreachlist, RD1act, RD1raster);
        RD2raster=HRU_to_Raster(ID,actreachlist, RD2act, RD2raster);    
        RG1raster=HRU_to_Raster(ID,actreachlist, RG1act, RG1raster);
        RG2raster=HRU_to_Raster(ID,actreachlist, RG2act, RG2raster);
  
        fuellstand.setObject("RD1_raster", RD1raster);
        fuellstand.setObject("RD2_raster", RD2raster);
        fuellstand.setObject("RG1_raster", RG1raster);
        fuellstand.setObject("RG2_raster", RG2raster);
        
        
       
        
        
    
       
        
    
        
    
    
    
}
        
    }

