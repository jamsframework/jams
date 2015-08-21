

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
        title="J2KKinematicWaveReachRouting",
        author="Christin Michel",
        description=""
)
/**
 *
 * @author sa63kul
 */
  
  public class KinematicWaveReachRouting extends JAMSComponent {
   
   
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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage"
            )
            public Attribute.Double catchmentSimRunoff;
     
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time bin of simulation"
            )
            public Attribute.Double timescale;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "spatial grid resolution"
            )
            public Attribute.Double modelrastersize;
               @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD1 storage"
            )
            public Attribute.Double catchmentRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD2 storage"
            )
            public Attribute.Double catchmentRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG1 storage"
            )
            public Attribute.Double catchmentRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage"
            )
            public Attribute.Double catchmentRG2;
   





    
/**
     * Calculates flow velocity in specific reach
     * @param q the runoff in the reach
     * @param width the width of reach
     * @param slope the slope of reach
     * @param rough the roughness of reach
     * @param secondsOfTimeStep the current time step in seconds
     * @return flow_velocity in m/s
     */
    public double calcFlowVelocity(double A, double width, double slope, double rough, double secondsOfTimeStep){
        double afv = 1;
        double veloc = 0;
        
        /**
         *transfering liter/d to m?/s
         **/
        
        double rh = A / (width + 2*(A / width));
        boolean cont = true;
        while(cont){
            veloc = (rough) * Math.pow(rh, (2.0/3.0)) * Math.sqrt(slope);
            if((Math.abs(veloc - afv)) > 0.001){
                afv = veloc;
                rh = calcHydraulicRadius(afv, A, width);
            } else{
                cont = false;
                afv = veloc;
            }
        }
        return afv;
    }
    
    /**
     * Calculates the hydraulic radius of a rectangular
     * stream bed depending on daily runoff and flow_velocity
     * @param v the flow velocity
     * @param q the daily runoff
     * @param width the width of reach
     * @return hydraulic radius in m
     */
    public double calcHydraulicRadius(double v, double q, double width){
        double A = (q / v);
        
        double rh = A / (width + 2*(A / width));
        
        return rh;
    }

public Point[][] Pfad(double [][] dgm, Point[][] routing,int actx,int acty,double zahl,double timestep,double[][]fliessrichtung,double[][] flow_velocity,int startx,int starty,double rastersize)
{
  Point p =new Point();
  
 
 

  if (dgm[actx][acty]!=-1 && zahl<timestep && actx>0 && actx<fliessrichtung.length-1 && acty>0 && acty<fliessrichtung[0].length-1)
  {
  
    zahl=zahl+(rastersize/flow_velocity[actx][acty]);//Ist ein Zeitschritt schon vorbei?
    if (fliessrichtung[actx][acty]==1)
    {    routing=Pfad(dgm,routing,actx+1,acty,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
    if (fliessrichtung[actx][acty]==2)
    {    routing=Pfad(dgm, routing,actx+1,acty+1,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
    if (fliessrichtung[actx][acty]==4)
    {    routing=Pfad(dgm, routing,actx,acty+1,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
    if (fliessrichtung[actx][acty]==8)
    {    routing=Pfad(dgm, routing,actx-1,acty+1,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
     if (fliessrichtung[actx][acty]==16)
    {    routing=Pfad(dgm, routing,actx-1,acty,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
    if (fliessrichtung[actx][acty]==32)
    {    routing=Pfad(dgm, routing,actx-1,acty-1,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
    if (fliessrichtung[actx][acty]==64)
    {    routing=Pfad(dgm, routing,actx,acty-1,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
    if (fliessrichtung[actx][acty]==128)
    {    routing=Pfad(dgm, routing,actx+1,acty-1,zahl,timestep,fliessrichtung,flow_velocity,startx,starty,rastersize);}
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


public Point[][] Pfad1(double[][] flgew,Point[][] routing,double[][]fliessrichtung,double[][]flow_velocity,double rastersize,double timestep)
{
  for (int i=1;i<flgew.length-1;i++)
{
    for (int j=1;j<flgew[0].length-1;j++)
    { 
        if (flgew[i][j]!=-1)
           
        
        {routing=Pfad(flgew,routing,i,j,0,timestep,fliessrichtung,flow_velocity,i,j,rastersize);  }
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
 double[][] fuellstand_reaches=null;
 double[][] Q_In=null; 
 
 double[][] actfliessrichtung=null;
 double[][] actdgm=null;
 double[][] actflgew1=null;
 double[][] flow_velocity=null;
 double[][] actreachinfo=null; 
   
 int i,j;
 
 double[][] acthangneigung=null;
 double timestep=timescale.getValue();
     
 if (timestep==1) {timestep=60*60*24;} //in seconds
 if (timestep==2) {timestep=60*60;}    //in seconds
 
 int rastersize=100;//(int)modelrastersize.getValue();


        
        try
        {Q_In=(double[][])fuellstand.getObject("Qin_reaches");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
 
        try
        {fuellstand_reaches=(double[][])fuellstand.getObject("fuellstand_reaches");}
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
        {acthangneigung=(double[][])information.getObject("hangneigung");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
           
       
          
        ArrayList<ArrayList<Point>> actreachlist = new ArrayList<ArrayList<Point>>();
        
        try
            {
            actreachlist=(ArrayList<ArrayList<Point>>)information.getObject("reachlist");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}

        try
        {actflgew1=(double[][])information.getObject("flgew1");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
        
        try
        {actreachinfo=(double[][])information.getObject("reachinfo");}
        catch (Exception e) {
	    System.out.println(e.toString());
	}
 
     Point p = new Point();
    
   
    
     double act_speicher=0; //erfasst den aktuellen Speicherzustand der Reach um die Flie?geschwindigkeit zu berechnen
     //im test in m*m*m
     
   
     //double zeit_pro_rasterzelle=rastersize/veloc;     //Wie lange das Wasser zum passieren einer Rasterzelle benoetigt in sec
             
     //erstellen einer reachlist in abh?ngigkeit von der pfadlaenge, also in abhaengigkeit von der geschwindigkeit die von der breite des flusses abhaengt
     
     flow_velocity = new double[actdgm.length][actdgm[0].length];
     int laenge1,laenge2,laenge3=0;
     Point next,p1=new Point();  
   
     double S_start=0; // Steicherzeustand zu beginn der Zeiteinheit einer Speichereinheit in m^3
     double Q_out=0;   // Abfluss der Speichereinheit in m^3/sec
     double Q_in=0;    // Q_in Zufluss der Speichereinheit in m^3/sec
     double A_start=0; // Querschnittsflaeche zu beginn des timesteps in m^2 
     double A_end=0;   // Querschnittsflaeche zum Ende des timesteps in m^2
     double S_end=0;   // Speicherzustand am Ende einer Zeiteinheit
     double q=0;//0.05;  // seitlicher Zufluss
     double Pegel_out=0; //Ausfluss am Pegel in einem Zeitschritt;
     
     //Bestimmen der Flie?geschwindigkeit in Abh?ngigkeit von den physikalischen Gegebenheiten, 
     laenge1=actreachlist.size();
     for(j=0;j<laenge1;j++) 
        {   
            laenge2=((ArrayList)actreachlist.get(j)).size();
            //Berechnung des actuellen Speichzustandes der Reach
            act_speicher=0;
            for (int l=1;l<laenge2;l++)
            {
                p=(Point)((ArrayList)actreachlist.get(j)).get(l);
                act_speicher=act_speicher+fuellstand_reaches[p.x][p.y];
            }
            p=(Point)((ArrayList)actreachlist.get(j)).get(0); //Reach ID
            double A=act_speicher/(rastersize*laenge2);  //Berechnung der Wassermenge in einem Meter Fluss
            double veloc = this.calcFlowVelocity(A, actreachinfo[2][p.x], actreachinfo[1][p.x], actreachinfo[3][p.x], timestep);  //nach Manning Strickler
            for (int l=1;l<laenge2;l++)
            {
                p=(Point)((ArrayList)actreachlist.get(j)).get(l);
                flow_velocity[p.x][p.y]=veloc;
            }
          
      } 
     
 
   
     
     
     //Routing speicher, wohin jede zellen innerhalb eines Zeitschrittes kommt
     Point [][] Routing =new Point [actdgm.length][actdgm[0].length]; 
     Routing=Pfad1(actflgew1, Routing, actfliessrichtung, flow_velocity,rastersize,timestep); 
     
    
     
     
     //Jetzt wird geroutet

     for(j=0;j<laenge1;j++) 
     {   
            p1=(Point)((ArrayList)actreachlist.get(j)).get(0); //Reach ID
            laenge2=((ArrayList)actreachlist.get(j)).size();
            for (int l=1;l<laenge2;l++)
            {
                 p=(Point)((ArrayList)actreachlist.get(j)).get(l);       
                 next=Routing[p.x][p.y];
                 //fuellstand_reaches + seitlicher zufluss
                 Q_in=fuellstand_reaches[p.x][p.y];                                 //Q_in (t,x) = Q_out (t-1,x-1)
                 //Q_In[p.x][p.y]=fuellstand_reaches[p.x][p.y];
                 //System.out.println(fuellstand_reaches[p.x][p.y]);
                 S_start=Q_In[p.x][p.y];
                 //System.out.print("Q_in: ");System.out.println(Q_In[p.x][p.y]);
                 Q_In[p.x][p.y]=0;                                                  // Start Speicherzustand 
                 //System.out.print("actreachinfo[2][p1.x]: ");System.out.println(actreachinfo[2][p1.x]);
                 // System.out.print("rastersize: ");System.out.println(rastersize);
                 A_start=S_start/(actreachinfo[2][p1.x]*rastersize*1000);              // A(x,t), Annahme querschnitt ist ein Recheck
                 //System.out.print("A_Start: ");System.out.println(A_start);
                 //A_start=Q_in/veloc;
                 S_end=S_start+Q_in+q;                                           //End Speicherzustand -->  abs(A_start-A_end)=Aenderung des Speichervolumens
                 A_end=S_end/(actreachinfo[2][p1.x]*rastersize*1000);                            //A(x,t+1)
                 Q_out=q+Q_in+(-A_start+A_end)/timestep;                         //Kinematic Wave Approximation des Abflusses
                 fuellstand_reaches[p.x][p.y]=S_end-Q_out;
                 Q_In[next.x][next.y]= Q_In[next.x][next.y]+Q_out;
                 // System.out.println(fuellstand_reaches[p.x][p.y]);
                 
            }
     }
      // System.out.println("Tag vorbei");
 
//Pegel finden     
Point pegel= new Point();
for (i=0;i<actflgew1.length;i++) 
     {
          for(j=0;j<actflgew1[0].length;j++)
          { 
             if (actflgew1[i][j]==0)
             {
              pegel.x=i;
              pegel.y=j;
             }
          }
     
     }   
     
Pegel_out=Q_In[pegel.x][pegel.y];  
Q_In[pegel.x][pegel.y]=0; 
catchmentSimRunoff.setValue(Pegel_out);   
//System.out.println(Pegel_out);
fuellstand.setObject("fuellstand_reaches", fuellstand_reaches);    
fuellstand.setObject("Qin_reaches", Q_In);      
 //hier m?ssen noch die passenden Werte gesetzt werden   
catchmentRD1.setValue(7);
catchmentRD2.setValue(7);
catchmentRG1.setValue(7);
catchmentRG2.setValue(7);   

         
     
}

public void cleanup()
{
   
}

}
