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
  
public class RasterToReach extends JAMSComponent {
   
    
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
            public Attribute.Double reachID;   
     
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
     
    
 public double Raster_to_HRU(double ID,ArrayList hrulist, double[][] fuellstand_hru,double[][] fuellstand_raster,int aufloesung)
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
                       // fuellstand_hru[j][1]=fuellstand_hru[j][1]+fuellstand_raster[(int)(p.x/aufloesung)][(int)(p.y/aufloesung)]/(aufloesung*aufloesung);
                        {fuellstand_hru[j][1]=fuellstand_hru[j][1]+fuellstand_raster[p.x][p.y];}
                    }
            fuellstand=fuellstand_hru[j][1];
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
     double ID=reachID.getValue();
     
     double[][] RD1raster=null;
     double[][] RD1reach=null;
     double[][] RD2raster=null;
     double[][] RD2reach=null;
     double[][] RG1raster=null;
     double[][] RG1reach=null;
     double[][] RG2raster=null;
     double[][] RG2reach=null;
     
     double RD1act=actRD1.getValue();
     double RD2act=actRD2.getValue();
     double RG1act=actRG1.getValue();
     double RG2act=actRG2.getValue();
     
     //verzoegerung um einen tag sol erreicht werden
     double RD1_speicher;
     double RD2_speicher;
     double RG1_speicher;
     double RG2_speicher;
 
     
     ArrayList<ArrayList<Point>> actreachlist = new ArrayList<ArrayList<Point>>();
        
        try
        {actreachlist=(ArrayList<ArrayList<Point>>)information.getObject("reachlist");}
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
        
        RD1act=Raster_to_HRU(ID,actreachlist, RD1reach, RD1raster,1);
        RD2act=Raster_to_HRU(ID,actreachlist, RD2reach, RD2raster,1);
        RG1act=Raster_to_HRU(ID,actreachlist, RG1reach, RG1raster,1);
        RG2act=Raster_to_HRU(ID,actreachlist, RG2reach, RG2raster,1);
    
        //Loeschen der Werte die in die Reaches umgeschrieben wurden
        int laenge1=actreachlist.size();
        Point p;
        for(int j=0;j<laenge1;j++)
        {   
            p=(Point)((ArrayList)actreachlist.get(j)).get(0);//sucht ID der HRU
            if (p.x==ID)
            {
                    int laenge2=((ArrayList)actreachlist.get(j)).size();
                    for(int i=1;i<laenge2;i++)
                    {
                        p=(Point)((ArrayList)actreachlist.get(j)).get(i);
                        RD1raster[p.x][p.y]=0;
                        RD2raster[p.x][p.y]=0;
                        RG1raster[p.x][p.y]=0;
                        RG2raster[p.x][p.y]=0;
                    }
            
            }
        }    
        
        
        inRD1.setValue(RD1act);
        inRD2.setValue(RD2act);
        inRG1.setValue(RG1act);
        inRG2.setValue(RG2act);
     
}



public void cleanup()
{
}

}
