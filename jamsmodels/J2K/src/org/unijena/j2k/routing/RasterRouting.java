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
  
  public class RasterRouting extends JAMSComponent {
   
    
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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage"
            )
            public Attribute.Double catchmentSimRunoff;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double simRunoff;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RD1_RR_Koeff;
   
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RD2_RR_Koeff;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RG1_RR_Koeff;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RG2_RR_Koeff;
      
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "spatial grid resolution"
            )
            public Attribute.Double gridresolution;
     
    
public double[][] RasterRouting(double anteil ,Point [][] routing,int dgm_sort[][],double[][] fliessrichtung,double[][] fuellstand, int anzahl_elemente, double flgew1[][],double aufloesung)
{
     int flr; //Fliessrichtung
     int zeile,spalte;
     int lauf;
     Point p=new Point();
     //Abarbeitung der Ratserzellen von der tiefsten zur hoechsten
    
     
     
     for (lauf=0;lauf<anzahl_elemente;lauf++)
     {
        zeile=dgm_sort[1][lauf]; //Auslesen der x-Koordinate
        spalte=dgm_sort[2][lauf]; //Auslesen der Y-Koordinate
        
        //Zeile und Spalte sind vermutlich vertauscht
        //
        //  32   16  8
        //  64   X   4
        // 128   1   2
        
        int test=0; //schaut, ob flgewpixel in rasterzelle liegt. dann wird hier nicht geroutet.
        if ((int)fuellstand[zeile][spalte]>0 && fliessrichtung[zeile][spalte]>0)
        {
        
            p=routing[zeile][spalte];
            
        
            //ist die Konstante Anteil gleich null, so wird kein Wasser weitergegeben
            //ist sie eins so wird alles Wasser in einem Zeitschritt weitergegeben
         
           if (flgew1[zeile][spalte]==-1)
           {
      
           
                fuellstand[zeile][spalte]=fuellstand[zeile][spalte]-fuellstand[zeile][spalte]*anteil;
                fuellstand[p.x][p.y]=fuellstand[p.x][p.y]+fuellstand[zeile][spalte]*anteil; 
            
           } 
        }   
  
       
     }
        
     return fuellstand;
     
    } 



public double[][] Weglaenge(double[][] Hangneigung,double kalibrierung, double timestep,double cellsize,double[][] saettigung)
{
 double[][] Weg=new double[Hangneigung.length][Hangneigung[0].length];
 double alpha;
 //Weg gibt an, wieviele Rasterzellen in Abh?ngigkeit von der Hangneigung geroutet wird;
for (int i=1;i<Weg.length;i++)
{
    for (int j=1;j<Weg[0].length;j++)
    {
        alpha=(Hangneigung[i][j]*2*Math.PI)/360; //Umrechnung Gradmass in Bogenmass
        Weg[i][j]=(9.9*Math.sin(alpha)*kalibrierung*timestep*saettigung[i][j])/(cellsize);
    }

}    
 return Weg;
}


public Point[][] Pfad(double [][] dgm, Point[][] routing,int actx,int acty,double zahl,double[][]fliessrichtung,double[][] weg,int startx,int starty,double aufloesung)
{
  Point p =new Point();
  
 
 

  if (dgm[actx][acty]!=-1 && zahl<1 && actx>0 && actx<fliessrichtung.length-1 && acty>0 && acty<fliessrichtung[0].length-1)
  {
    zahl=zahl+(1/weg[actx][acty]);//Ist ein Zeitschritt schon vorbei?
    if (fliessrichtung[actx][acty]==1)
    {    routing=Pfad(dgm,routing,actx+1,acty,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
    if (fliessrichtung[actx][acty]==2)
    {    routing=Pfad(dgm, routing,actx+1,acty+1,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
    if (fliessrichtung[actx][acty]==4)
    {    routing=Pfad(dgm, routing,actx,acty+1,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
    if (fliessrichtung[actx][acty]==8)
    {    routing=Pfad(dgm, routing,actx-1,acty+1,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
     if (fliessrichtung[actx][acty]==16)
    {    routing=Pfad(dgm, routing,actx-1,acty,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
    if (fliessrichtung[actx][acty]==32)
    {    routing=Pfad(dgm, routing,actx-1,acty-1,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
    if (fliessrichtung[actx][acty]==64)
    {    routing=Pfad(dgm, routing,actx,acty-1,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
    if (fliessrichtung[actx][acty]==128)
    {    routing=Pfad(dgm, routing,actx+1,acty-1,zahl,fliessrichtung,weg,startx,starty,aufloesung);}
    if (fliessrichtung[actx][acty]==0) //Pegel; hier wird nicht weitergeroutet!
    {
      p.x=actx;
      p.y=acty;
      routing[startx][starty]=p;  
    }
  }
  else
  {
      //Weist zu, in welche Rasterzelle geroutet wird
      p.x=actx;
      p.y=acty;
      routing[startx][starty]=p;
  
  } 
  
 
  return routing;

}


public Point[][] Pfad1(double[][] dgm,Point[][] routing,double[][]fliessrichtung,double[][] weg,double aufloesung)
{
  for (int i=1;i<weg.length-1;i++)
{
    for (int j=1;j<weg[0].length-1;j++)
    { 
        if (dgm[i][j]!=-1)
           
        
        {routing=Pfad(dgm,routing,i,j,0,fliessrichtung,weg,i,j,aufloesung);  }
    }
}
  return routing;
}


public void init()throws Attribute.Entity.NoSuchAttributeException
{
   
}
    
@SuppressWarnings("unchecked")    
public void run()throws Attribute.Entity.NoSuchAttributeException{

 double[][] RD1raster=null;
 double[][] RD2raster=null;
 double[][] RG1raster=null;
 double[][] RG2raster=null;
 
 double[][] actfliessrichtung=null;
 double[][] actdgm=null;
 
 double[][] actflgew1=null;
   
 
 
 double aufloesung;
 aufloesung=gridresolution.getValue();
       
 
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
        {actfliessrichtung=(double[][])information.getObject("fliessrichtung");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
             
        try
        {actdgm=(double[][])information.getObject("dgm");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
            
        
                 
        int actdgm_sort[][]=new int[3][actdgm.length*actdgm[0].length]; //Array, indem die H?henwerte sortiert gespeichert werden
       
        try
        {actdgm_sort=(int[][])information.getObject("dgm_sort");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
          
        ArrayList<ArrayList<Point>> acthrulist = new ArrayList<ArrayList<Point>>();
        
        try
        {acthrulist=(ArrayList<ArrayList<Point>>)information.getObject("hrulist");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
           
        //Bestimmen in welche Rasterzelle
        //die jeweilige Rasterzelle entwaessert (in Abhaengigkeit von der Hangneigung)
        
        Point [][] actroutingRD1 =new Point [actdgm.length][actdgm[0].length];
        Point [][] actroutingRD2 =new Point [actdgm.length][actdgm[0].length];
        Point [][] actroutingRG1 =new Point [actdgm.length][actdgm[0].length];
        Point [][] actroutingRG2 =new Point [actdgm.length][actdgm[0].length];
        
        try
        {actroutingRG2=(Point [][])information.getObject("routingRG2");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        try
        {actroutingRD1=(Point [][])information.getObject("routingRD1");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        try
        {actroutingRD2=(Point [][])information.getObject("routingRD2");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        try
        {actroutingRG1=(Point [][])information.getObject("routingRG1");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        try
        {actflgew1=(double[][])information.getObject("flgew1");}
        catch (Exception e) {
	    System.out.println(e.toString());
        }
 
 double RD1_Koeff=RD1_RR_Koeff.getValue();
 double RD2_Koeff=RD2_RR_Koeff.getValue();
 double RG1_Koeff=RG1_RR_Koeff.getValue();
 double RG2_Koeff=RG2_RR_Koeff.getValue();

 RD1raster=RasterRouting(RD1_Koeff,actroutingRD1,actdgm_sort, actfliessrichtung, RD1raster, actdgm_sort[0].length,actflgew1,aufloesung);
 RD2raster=RasterRouting(RD2_Koeff,actroutingRD2,actdgm_sort, actfliessrichtung, RD2raster, actdgm_sort[0].length,actflgew1,aufloesung);
 RG1raster=RasterRouting(RG1_Koeff,actroutingRG1,actdgm_sort, actfliessrichtung, RG1raster, actdgm_sort[0].length,actflgew1,aufloesung);
 RG2raster=RasterRouting(RG2_Koeff,actroutingRG2,actdgm_sort, actfliessrichtung, RG2raster, actdgm_sort[0].length,actflgew1,aufloesung);

 

 fuellstand.setObject("RD1_raster", RD1raster);
 fuellstand.setObject("RD2_raster", RD2raster);
 fuellstand.setObject("RG1_raster", RG1raster);
 fuellstand.setObject("RG2_raster", RG2raster);
 
   
  
}     

public void cleanup()
{
   
}


}
