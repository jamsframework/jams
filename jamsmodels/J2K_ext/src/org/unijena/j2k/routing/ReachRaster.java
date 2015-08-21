
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
        title="J2KReachRaster",
        author="Christin Michel",
        description="berechnet Reachraster aus den viel Bodenschichten"
)
/**
 *
 * @author sa63kul
 */
  
public class ReachRaster extends JAMSComponent {
   
    
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
    
     
     
     
    

 public void init() throws Attribute.Entity.NoSuchAttributeException
{
    
}
    
@SuppressWarnings("unchecked")
public void run()throws Attribute.Entity.NoSuchAttributeException
{  

     double[][] RD1raster=null;
   
     double[][] RD2raster=null;
   
     double[][] RG1raster=null;
   
     double[][] RG2raster=null;
     double[][] Fuellstand_Reaches=null;
     
    
  
 
     
     ArrayList<ArrayList<Point>> actreachlist = new ArrayList<ArrayList<Point>>();
        
        try
        {actreachlist=(ArrayList<ArrayList<Point>>)information.getObject("reachlist");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
     
        try
        {Fuellstand_Reaches=(double[][])fuellstand.getObject("fuellstand_reaches");}
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
        
        
        //Loeschen der Werte die in die Reaches umgeschrieben wurden
        int laenge1=actreachlist.size();
        Point p;
        for(int j=0;j<laenge1;j++)
        {   
            
                    int laenge2=((ArrayList)actreachlist.get(j)).size();
                    for(int i=1;i<laenge2;i++)
                    {
                        p=(Point)((ArrayList)actreachlist.get(j)).get(i);
                        Fuellstand_Reaches[p.x][p.y]=Fuellstand_Reaches[p.x][p.y]+RD1raster[p.x][p.y]+RD2raster[p.x][p.y]+RG1raster[p.x][p.y]+RG2raster[p.x][p.y];
                        
                        RD1raster[p.x][p.y]=0;
                        RD2raster[p.x][p.y]=0;
                        RG1raster[p.x][p.y]=0;
                        RG2raster[p.x][p.y]=0;
                    }
            
           
        }    
        fuellstand.setObject("fuellstand_reaches", Fuellstand_Reaches);
     
}



public void cleanup()
{
}

}
