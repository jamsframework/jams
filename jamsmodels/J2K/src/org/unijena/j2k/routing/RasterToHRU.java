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
@JAMSComponentDescription
(
        title="J2KRasterRouting",
        author="Christin Michel",
        description="Wandelt Rasterebene wieder in HRU-Aufl?sung um"
)
/**
 *
 * @author sa63kul
 */
  
public class RasterToHRU extends JAMSComponent {
   
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = ""
            )
            public Attribute.Entity information;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = ""
            )
            public Attribute.Entity fuellstand;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage"
            )
            public Attribute.Double hruID;   
     
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
            description = "Reach statevar RD1 storage"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 storage"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 storage"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage"
            )
            public Attribute.Double inRG2;  
    
     
    
 public double Raster_to_HRU(double ID,ArrayList hrulist, double[][] fuellstand_hru,double[][] fuellstand_raster,double [][] flgew1)
{
        int laenge1,laenge2;
        
        double fuellstand=0;
        Point p = new Point();
        laenge1=hrulist.size();
        for(int j=0;j<laenge1;j++)
        {   
            p=(Point)((ArrayList)hrulist.get(j)).get(0);//sucht ID der HRU
            if (p.x==ID)
            {
                    fuellstand_hru[j][1]=0; //Fuellstand auf null setzen
                    laenge2=((ArrayList)hrulist.get(j)).size();
                   
                   
                    for(int i=1;i<laenge2;i++)
                    {
                        p=(Point)((ArrayList)hrulist.get(j)).get(i);
                        if (flgew1[p.x][p.y]==-1)
                        {
                          
                            fuellstand_hru[j][1]=fuellstand_hru[j][1]+fuellstand_raster[p.x][p.y];}
                    }
                    
                    
            fuellstand=fuellstand_hru[j][1];
            j=laenge1;
            }
        }    
        return fuellstand;
        
}
 
 
 public void init() throws Attribute.Entity.NoSuchAttributeException
{
    
}
    
@SuppressWarnings("unchecked")    
public void run()throws Attribute.Entity.NoSuchAttributeException
{  
     double ID=hruID.getValue();
     
     double[][] RD1raster=null;
     double[][] RD1hru=null;
     double[][] RD2raster=null;
     double[][] RD2hru=null;
     double[][] RG1raster=null;
     double[][] RG1hru=null;
     double[][] RG2raster=null;
     double[][] RG2hru=null;
     double[][] actflgew1=null;
     double RD1act=actRD1.getValue();
     double RD2act=actRD2.getValue();
     double RG1act=actRG1.getValue();
     double RG2act=actRG2.getValue();
     
      //Verteilungsraster -  merkt sich das Verteilungsmuster
     double[][] RD1_vraster=null;
     double[][] RD2_vraster=null;
     double[][] RG1_vraster=null;
     double[][] RG2_vraster=null;
 
     
     ArrayList<ArrayList<Point>> acthrulist = new ArrayList<ArrayList<Point>>();
        
        try
        {acthrulist=(ArrayList<ArrayList<Point>>)information.getObject("hrulist");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
     
       try
        {RD1hru=(double[][])fuellstand.getObject("RD1_hru");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RD2hru=(double[][])fuellstand.getObject("RD2_hru");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RG1hru=(double[][])fuellstand.getObject("RG1_hru");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {RG2hru=(double[][])fuellstand.getObject("RG2_hru");}
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
        
        try
        {actflgew1=(double[][])information.getObject("flgew1");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
     	
         try
        {RD1_vraster=(double[][])information.getObject("RD1_vraster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        try
        {RD2_vraster=(double[][])information.getObject("RD2_vraster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
         try
        {RG1_vraster=(double[][])information.getObject("RG1_vraster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
         try
        {RG2_vraster=(double[][])information.getObject("RG2_vraster");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
      
     
        RD1act=Raster_to_HRU(ID,acthrulist, RD1hru, RD1raster,actflgew1);
        RD2act=Raster_to_HRU(ID,acthrulist, RD2hru, RD2raster,actflgew1);
        RG1act=Raster_to_HRU(ID,acthrulist, RG1hru, RG1raster,actflgew1);
        RG2act=Raster_to_HRU(ID,acthrulist, RG2hru, RG2raster,actflgew1);
        
        
         //Erstellen der Verteilungsmusters innerhalb jeder HRU
        int laenge1,laenge2;
        double summe1=0;
        double summe2=0;
        double summe3=0;
        double summe4=0;
        double fuellstand=0;
        Point p = new Point();
        laenge1=acthrulist.size();
        for(int j=0;j<laenge1;j++)
        {   
            p=(Point)((ArrayList)acthrulist.get(j)).get(0);//sucht ID der HRU
            if (p.x==ID)
            {
                    
                    laenge2=((ArrayList)acthrulist.get(j)).size();
                   
                    for(int i=1;i<laenge2;i++)
                    {
                        p=(Point)((ArrayList)acthrulist.get(j)).get(i);
                        if (actflgew1[p.x][p.y]==-1)
                        {
                            summe1=summe1+RD1raster[p.x][p.y];
                            summe2=summe2+RD2raster[p.x][p.y];
                            summe3=summe3+RG1raster[p.x][p.y];
                            summe4=summe4+RG2raster[p.x][p.y];
                        }
                    }
                    for(int i=1;i<laenge2;i++)
                    {
                        p=(Point)((ArrayList)acthrulist.get(j)).get(i);
                        if (actflgew1[p.x][p.y]==-1)
                        {
                            if (summe1 !=0)
                            {
                            RD1_vraster[p.x][p.y]=RD1raster[p.x][p.y]/summe1;
                            }
                            else
                            {
                            RD1_vraster[p.x][p.y]=1/laenge2; //gleichmaessig verteilen
                            }
                            if (summe2 !=0)
                            {
                            RD2_vraster[p.x][p.y]=RD2raster[p.x][p.y]/summe2;
                            }
                            else
                            {
                            RD2_vraster[p.x][p.y]=1/laenge2; //gleichmaessig verteilen
                            }
                            if (summe3 !=0)
                            {
                            RG1_vraster[p.x][p.y]=RG1raster[p.x][p.y]/summe3;
                            }
                            else
                            {
                            RG1_vraster[p.x][p.y]=1/laenge2; //gleichmaessig verteilen
                            }
                            if (summe4 !=0)
                            {
                            RG2_vraster[p.x][p.y]=RG2raster[p.x][p.y]/summe4;
                             
                            }
                            else
                            {
                            RG2_vraster[p.x][p.y]=1/laenge2; //gleichmaessig verteilen
                            }
                        }
                    }
             
            j=laenge1;
            }
        }    
        
        
        
        information.setObject("RD1_vraster", RD1_vraster);
        information.setObject("RD2_vraster", RD2_vraster);
        information.setObject("RG1_vraster", RG1_vraster);
        information.setObject("RG2_vraster", RG2_vraster);
 
 
     
        //Verzoegerung des Abflusses um einen Tag
     
        inRD1.setValue(RD1act);
        inRD2.setValue(RD2act);
        inRG1.setValue(RG1act);
        inRG2.setValue(RG2act);
}

public void cleanup()
{
}

}
