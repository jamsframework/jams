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
import java.util.ArrayList;
import java.awt.Point;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import jams.data.*;
import jams.tools.JAMSTools;
/**
 *
 * @author sa63kul
 */
public class CreateGrid extends JAMSComponent {
   
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "spatial grid resolution"
            )
            public Attribute.Double gridresolution;
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "spatial grid resolution"
            )
            public Attribute.Double modelrastersize;
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time bin of simulation"
            )
            public Attribute.Double timescale;
                 
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The current hru entity"
            )
            public Attribute.Entity information;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The current hru entity"
            )
            public Attribute.Entity fuellstand;
   
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RD1_Weg_Koeff;
   
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RD2_Weg_Koeff;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RG1_Weg_Koeff;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double RG2_Weg_Koeff;
     
     
    @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = ""
           )
    public Attribute.String hruRasterFile;

    @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = ""
           )
    public Attribute.String dgmFile;
     
    @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = ""
           )
    public Attribute.String flgewFile;

public double Rastersize(String path)
{
        double cellsize=0;
        try 
        {
	    //search for ncols, nrows, x11corner, y11corner, cellsize, nodatevalue
	    BufferedReader reader = new BufferedReader(new FileReader(path));		    	    	    
	    int datafound = 0;
	    String line;
	    StringTokenizer st;
	    
	    while (datafound < 5) 
            {		
		line = reader.readLine();	    
		st = new StringTokenizer(line);
		while (st.hasMoreTokens())
                {
		    String tok = st.nextToken();
		   
		    if (tok.contains("cellsize"))
                    {
			cellsize = Double.parseDouble(st.nextToken());
			datafound++;
		    }		    
		}	    
	    }	    
        }
        catch (Exception e) {
	    System.out.println(e.toString());
            e.printStackTrace();
	}
        return cellsize;
        
}
public double[][] Einlesen(String path)
{
    
    
     int ncols=0;
     int nrows=0;
     double x11corner;
     double y11corner;
    
     double cellsize;
    
     double nodatavalue = 0.0;
     double grid[][]=null;  
    
    double lowest_value = Double.POSITIVE_INFINITY;
        try {
	    //search for ncols, nrows, x11corner, y11corner, cellsize, nodatevalue
	    BufferedReader reader = new BufferedReader(new FileReader(path));		    	    	    
	    int datafound = 0;
	    String line;
	    StringTokenizer st;
	    
	    while (datafound < 5) {		
		line = reader.readLine();	    
		st = new StringTokenizer(line);
		while (st.hasMoreTokens()) {
		    String tok = st.nextToken();
		    if (tok.contains("ncols")) {
			ncols = new Integer(st.nextToken()).intValue();
			datafound++;
		    }
		    if (tok.contains("nrows")) {
			nrows = new Integer(st.nextToken()).intValue();
			datafound++;
		    }
		    if (tok.contains("xllcorner")) {
			x11corner = new Double(st.nextToken()).doubleValue();
			datafound++;
		    }
		    if (tok.contains("yllcorner")) {
			y11corner = new Double(st.nextToken()).doubleValue();
			datafound++;
		    }
		    if (tok.contains("cellsize")) {
			cellsize = new Double(st.nextToken()).doubleValue();
			datafound++;
		    }		    
		}	    
	    }	    
	    //now read grid
	    
            grid= new double[ncols][nrows];
         
	    int x = 0;
	    int y = 0;
	    boolean firsttoken = true;	    
	    while ( (line = reader.readLine()) != null) {		
		st = new StringTokenizer(line);	
		
		while (st.hasMoreTokens()) {
		    if (y >= nrows) {			
			System.out.println("error to many entrys");
			break;
		    }
		    String tok = st.nextToken();
		    //look for optional components
		    if (firsttoken) {
			firsttoken = false;
			//optional
			if (tok.contains("NODATA_value")) {		    
			    nodatavalue = new Double(st.nextToken()).doubleValue();
			}
			if (st.hasMoreTokens()) {
			    tok = st.nextToken();
			}
			else
			    continue;
		    }
		    grid[x][y] = new Double(tok).doubleValue();
		    
		    if (Math.abs(grid[x][y]-nodatavalue) < 0.0001) {
			grid[x][y] = -1.0;
		    }		    		    
		    if (grid[x][y] != -1.0 && grid[x][y] < lowest_value) {
			lowest_value = grid[x][y];
		    }
		    x++;
		    if (x >= ncols) {
			x = 0;
			y++;
		    }		    
		}
	    }
	}
	catch (Exception e) {
	    System.out.println(e.toString());
            e.printStackTrace();
	}
        return grid;
       
    }    
public void Ausgabe(double[][] fuellstand,String path)
{
  
    try
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
         for (int spalte=0;spalte<fuellstand[0].length;spalte++) 
        {   
           for (int zeile=0;zeile<fuellstand.length;zeile++)
         
           {
               writer.write((int)(fuellstand[zeile][spalte])+" ");
           }
           writer.write("\n");
        }   
        writer.close();
    }
    catch(Exception e) 
    {
        System.out.println(e.toString());
        e.printStackTrace();
    }
}
public double[][] Hangneigung(double[][] dgm,double[][] fliessrichtung,double[][] hangneigung, int cellsize)
{
//Funktion berechnet die Hangneigung. Der Winkel wird auf mindestens 1 Grad gesetzt.
    
int i,j;
double alpha;
double a=0;
double b=0;
double c=0;
double flr; //Fliessrichtung

/*
C
|\ 
|a\b 
|  \
B---A
  c
 */
for(i=1;i<dgm.length-1;i++)
        {
            for(j=1;j<dgm[0].length-1;j++)
            {
                if (dgm[i][j] !=-1)
                {
                flr=fliessrichtung[i][j];
                if (flr==1) 
                    {c=cellsize;
                     a=dgm[i][j]-dgm[i+1][j];
                     b=Math.sqrt(a*a+c*c); 
                    }
                if (flr==2) 
                    {c=Math.sqrt(2)*cellsize;
                     a=dgm[i][j]-dgm[i+1][j+1];
                     b=Math.sqrt(a*a+c*c); 
                    }
                if (flr==4) 
                    {c=cellsize;
                     a=dgm[i][j]-dgm[i][j+1];
                     b=Math.sqrt(a*a+c*c); 
                    }
                if (flr==8) 
                    {c=Math.sqrt(2)*cellsize;
                     a=dgm[i][j]-dgm[i-1][j+1];
                     b=Math.sqrt(a*a+c*c); 
                    }
                if (flr==16) 
                    {c=cellsize;
                     a=dgm[i][j]-dgm[i-1][j];
                     b=Math.sqrt(a*a+c*c); 
                    }
                if (flr==32) 
                    {c=Math.sqrt(2)*cellsize;
                     a=dgm[i][j]-dgm[i-1][j-1];
                     b=Math.sqrt(a*a+c*c); 
                    }
                if (flr==64) 
                    {c=cellsize;
                     a=dgm[i][j]-dgm[i][j-1];
                     b=Math.sqrt(a*a+c*c); 
                    }
                if (flr==128) 
                    {c=Math.sqrt(2)*cellsize;
                     a=dgm[i][j]-dgm[i+1][j-1];
                     b=Math.sqrt(a*a+c*c); 
                    }
                alpha=Math.asin(a/b);
                alpha=360*alpha/(2*Math.PI);//Umrechnung Gradmass in Bogenmass
                if (alpha<1) {alpha=1;} //negative Winkel entstehen, weil dass Fliessgewaesser nicht vollstaendig korrekt eingearbeitet wurden ist
                hangneigung[i][j]=alpha;
                }
            }
            
        }

return hangneigung;
}

public double[][] Fliessrichtung(double[][] dgm)
{
   double[][] fliessrichtung= new double[dgm.length][dgm[0].length];
   int count; //sucht das niedrigste Nachbarpixel
   int zahl=0;
 
   for(int i=1;i<dgm.length-1;i++)    
   {
    for(int j=1;j<dgm[0].length-1;j++)
    {
       // if (dgm[i][j]!=-1)
        {
        count=10000;
        if (dgm[i][j+1]<count)
        {count=(int)dgm[i][j+1];
        fliessrichtung[i][j]=4;}
        if (dgm[i+1][j+1]<count)
        {count=(int)dgm[i+1][j+1];
        fliessrichtung[i][j]=2;}
        if (dgm[i+1][j]<count)
        {count=(int)dgm[i+1][j];
        fliessrichtung[i][j]=1;}
        if (dgm[i+1][j-1]<count)
        {count=(int)dgm[i+1][j-1];
        fliessrichtung[i][j]=128;}
        if (dgm[i][j-1]<count)
        {count=(int)dgm[i][j-1];
        fliessrichtung[i][j]=64;}
        
        if (dgm[i-1][j-1]<count)
        {count=(int)dgm[i-1][j-1];
        fliessrichtung[i][j]=32;}
        if (dgm[i-1][j]<count)
        {count=(int)dgm[i-1][j];
        fliessrichtung[i][j]=16;}
        if (dgm[i-1][j+1]<count)
        {count=(int)dgm[i-1][j+1];
        fliessrichtung[i][j]=8;}
        }
       //else
       // {fliessrichtung[i][j]=0;}    
    }}
    
    
return fliessrichtung;
}


public double[][][] fliessgewaesser(int i, int j, double ID, double[][][] fliess)
{
    //Prozedur zur besonderen Verwaltung der Flei?gew?sser
    int count=0;
    double richtung=0;
    int x=0,y=0;
    

    //Pixel sind direkt benachbart
    for (int k=-1;k<2;k++)
    {    
        for (int l=-1;l<2;l++)
        {
            if (k+l==0 || k+l==2 || k+l==-2)
            {}
            else
            {    
            if (fliess [1][i][j]==ID && fliess[1][i+k][j+l]==ID)
            {
                if (k==1 && l==0) {richtung=1;}
                if (k==0 && l==1) {richtung=4;}
                if (k==-1 && l==0) {richtung=16;}
                if (k==0 && l==-1) {richtung=64;}
                x=i+k;y=j+l;
                fliess[0][i][j]=richtung;
                fliess[1][i][j]=-1.0;
                
                fliess=fliessgewaesser(x,y,ID,fliess);
                k=5;l=5;
                
            }
            
           
            }
        }
    }
    //Pixel sind diagonal benachbart
    for (int k=-1;k<2;k++)
    {    
        for (int l=-1;l<2;l++)
        {
            if ((k==0 && l==0) || k+l==1 || k+l==-1)
            {}
            else
            {    
            if (fliess [1][i][j]==ID && fliess[1][i+k][j+l]==ID)
            {
                if (k==1 && l==1) {richtung=2;}
                if (k==-1 && l==1) {richtung=8;}
                if (k==-1 && l==-1) {richtung=32;}
                if (k==1 && l==-1) {richtung=128;}
                x=i+k;y=j+l;
                fliess[0][i][j]=richtung;
                fliess[1][i][j]=-1.0;
                fliess=fliessgewaesser(x,y,ID,fliess);
                k=5;l=5;
                
            }
            
           
            }
        }
    }
    
   //Fuer den Fall, dass noch keien Richtung gefunder worden ist
   //Verarbeitung des letzten Pixels
    for (int k=-1;k<2;k++)
    {    
        for (int l=-1;l<2;l++)
        {
            if (k==0 && l==0)
            {}
            else
            {    
            if (fliess[1][i+k][j+l]!=-1  && fliess[1][i][j]==ID)
            {
                if (k==1 && l==0) {richtung=1;}
                if (k==1 && l==1) {richtung=2;}
                if (k==0 && l==1) {richtung=4;}
                if (k==-1 && l==1) {richtung=8;}
                if (k==-1 && l==0) {richtung=16;}
                if (k==-1 && l==-1) {richtung=32;}
                if (k==0 && l==-1) {richtung=64;}
                if (k==1 && l==-1) {richtung=128;}
                x=i+k;y=j+l;
                fliess[0][i][j]=richtung;
                fliess[1][i][j]=-1.0;
                
                k=5;l=5;
                
            }
            
           
            }
        }
        
    }
       
        
            
            
      
    return fliess;
}

public double[][] flgew(double[][] dgm,double[][] fliessrichtung, double[][] flgew, double[][] flgew1, double [][] hruraster)
{
     
    //Variablendaklaration
     
    double [][][] fliess=null;
    int count=1;
    int anzahl=0;
    fliess =new double [2][fliessrichtung.length][fliessrichtung[0].length];
     //Initialisierung der Array fliess
     //erste Ebene: Fliessrichtung
     //zweite Ebene Fliessgewaesser
    
    
    
  for(int i=0;i<fliessrichtung.length;i++)
      {
            for(int j=0;j<fliessrichtung[0].length;j++)
            {
                fliess[0][i][j]=fliessrichtung[i][j];
                fliess[1][i][j]=flgew[i][j]; 
            }
      }
     
    int zahl=1; 
   
     while (zahl!=0)
     {
     zahl=0; 
    
     for(int i=1;i<fliessrichtung.length-1;i++)
      {
            for(int j=1;j<fliessrichtung[0].length-1;j++)
            {
                if (fliess[1][i][j] !=-1 && fliess[1][i][j] !=123456) //123456 steht fuer den Pegel
                {    
                count=0;    
                if (fliess[1][i+1][j]==-1)
                    {count++;}
                if (fliess[1][i-1][j]==-1)
                    {count++;}
                if (fliess[1][i][j+1]==-1)
                    {count++;}
                if (fliess[1][i][j-1]==-1)
                    {count++;}
                if (fliess[1][i+1][j+1]==-1)
                    {count++;}
                if (fliess[1][i+1][j-1]==-1)
                    {count++;}
                if (fliess[1][i-1][j+1]==-1)
                    {count++;}
                if (fliess[1][i-1][j-1]==-1)
                    {count++;}
                if (count==7 )
                    {   
                  
                    fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess);
                   
                    zahl++;
                   
                    anzahl++;
                    }
               
               if (count==6 && fliess[1][i+1][j+1]!=-1 && fliess[1][i][j+1]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                if (count==6 && fliess[1][i+1][j+1]!=-1 && fliess[1][i+1][j]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                if (count==6 && fliess[1][i+1][j]!=-1 && fliess[1][i+1][j-1]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                if (count==6 && fliess[1][i+1][j-1]!=-1 && fliess[1][i][j-1]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                if (count==6 && fliess[1][i-1][j-1]!=-1 && fliess[1][i][j-1]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                if (count==6 && fliess[1][i-1][j]!=-1 && fliess[1][i-1][j-1]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                if (count==6 && fliess[1][i-1][j+1]!=-1 && fliess[1][i-1][j]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                if (count==6 && fliess[1][i-1][j+1]!=-1 && fliess[1][i][j+1]!=-1)
                {fliess=fliessgewaesser(i,j,fliess[1][i][j] ,fliess); zahl++;anzahl++;}
                
                }             
            }
        }
     }
    
    //System.out.println(anzahl);
    
    for(int i=0;i<fliessrichtung.length;i++)
      {
            for(int j=0;j<fliessrichtung[0].length;j++)
            {
                fliessrichtung[i][j]=fliess[0][i][j];
                flgew[i][j]=fliess[1][i][j]; 
            }
      }
    
    //Flussbett Bearbeitung  
     for(int i=1;i<fliessrichtung.length-1;i++)
      {
            for(int j=1;j<fliessrichtung[0].length-1;j++)
            {
           
             if (flgew1[i][j]!=-1  )
             {
                
                if (flgew1[i+1][j]==-1)
                {fliessrichtung[i+1][j]=16;}
                if (flgew1[i-1][j]==-1)
                {fliessrichtung[i-1][j]=1;}
                if (flgew1[i][j+1]==-1)
                {fliessrichtung[i][j+1]=64;}
                if (flgew1[i][j-1]==-1)
                {fliessrichtung[i][j-1]=4;}
          
             }
            }
      }
        
   
    return fliessrichtung;
    
    
     
     



}
    

public double[][] Weglaenge(double[][] Hangneigung,double kalibrierung, double zeitintervall,double cellsize)
{
 double[][] Weg=new double[Hangneigung.length][Hangneigung[0].length];
 double alpha;
 //Weg gibt an, wieviele Rasterzellen in Abh?ngigkeit von der Hangneigung geroutet wird;
for (int i=1;i<Weg.length;i++)
{
    for (int j=1;j<Weg[0].length;j++)
    {
        alpha=(Hangneigung[i][j]*2*Math.PI)/360; //Umrechnung Gradmass in Bogenmass
        Weg[i][j]=(9.9*Math.sin(alpha)*kalibrierung*zeitintervall)/(cellsize);
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



public double[][] SucheStruktur(double raster[][], int zeile, int spalte, int count,double actID)
{
    if (raster[zeile][spalte]==actID)
    {
        
        raster[zeile][spalte]=-1;
        if (zeile>1 && spalte>1 && zeile<raster.length-1 && spalte<raster[0].length-1)
        {
        raster=SucheStruktur(raster, zeile-1, spalte, count, actID);
        raster=SucheStruktur(raster, zeile, spalte-1, count, actID);
        raster=SucheStruktur(raster, zeile, spalte+1, count, actID);
        raster=SucheStruktur(raster, zeile+1, spalte, count, actID);
        }
    }
        
    
        return raster;
    }
    
public double[][] Mapping(double raster[][],ArrayList<Point> anhang,int zeile, int spalte, int count,double actID,int hilf, int type)
{
   //Die Variable "hilf" ist daf?r zust?ndig zu bestimmen, ob es die erste Zelle der Liste ist. Dort wird die HRU_ID angehangen.
   //Type 1 HRU
   //Type 2 Reach 
    
    
    Point p = new Point();
    Point p1= new Point();
    if (hilf==1 && type ==1)
    {    p1.x=(int)actID;
         p1.y=0;
         anhang.add(p1);
         
      
    }
    if (hilf==1 && type ==2)
    {
          
            p1.x=(int)actID;
            
            p1.y=0;
            anhang.add(p1);
          //  System.out.print(p.x);System.out.print(" ");System.out.println(p.y);
    }


    if (raster[zeile][spalte]==actID || raster[zeile][spalte]==123456)
    {
        
        p.x=zeile;
        p.y=spalte;
    
        anhang.add(p);
        raster[zeile][spalte]=-1;
        // Suchrichtungen
        //       1
        //    2     3
        //       4
        //
        if (zeile>1 && spalte>1 && zeile<raster.length-1 && spalte<raster[0].length-1)
        {
        raster=Mapping(raster,anhang, zeile-1, spalte, count, actID,0,type);
        raster=Mapping(raster,anhang, zeile, spalte-1, count, actID,0,type);
        raster=Mapping(raster,anhang, zeile, spalte+1, count, actID,0,type);
        raster=Mapping(raster,anhang, zeile+1, spalte, count, actID,0,type);
        if (type ==2)
        {    
        raster=Mapping(raster,anhang, zeile-1, spalte-1, count, actID,0,type);
        raster=Mapping(raster,anhang, zeile+1, spalte-1, count, actID,0,type);
        raster=Mapping(raster,anhang, zeile-1, spalte+1, count, actID,0,type);
        raster=Mapping(raster,anhang, zeile+1, spalte+1, count, actID,0,type);
        }
        
        
        }
    }
        return raster;
    }
    
public int[][] quickSort(int liste[][], int untereGrenze, int obereGrenze)
{
  int links = untereGrenze;
  int rechts = obereGrenze;
  int pivot = liste[0][((untereGrenze + obereGrenze) / 2)];
  do {
    while (liste[0][links] < pivot) {
      links++;
    }
    while (pivot < liste[0][rechts]) {
      rechts--;
    }
    if (links <= rechts) {
      //Elemente tauschen  
      int tmp = liste[0][links];
      liste[0][links] = liste[0][rechts];
      liste[0][rechts] = tmp;
      tmp = liste[1][links];
      liste[1][links] = liste[1][rechts];
      liste[1][rechts] = tmp;
      tmp = liste[2][links];
      liste[2][links] = liste[2][rechts];
      liste[2][rechts] = tmp;
      links++;
      rechts--;
    }
  } while (links <= rechts);
  if (untereGrenze < rechts) {
     liste=quickSort(liste, untereGrenze, rechts);
  }
  if (links < obereGrenze) {
     liste=quickSort(liste, links, obereGrenze);
   }
  return liste;
}

public int[][] Sort(int[][] dgm_sort, double[][] dgm)
{
    int i,j;    
    int count=0;
        for(i=1;i<dgm.length-1;i++)
        {
            for(j=1;j<dgm[0].length-1;j++)
            {
                if (dgm[i][j]!=-1)
                {dgm_sort[0][count]=(int)(dgm[i][j]);
                 dgm_sort[1][count]=i;
                 dgm_sort[2][count]=j;
                 count++;
                }
            }
        }
     
    //H?henwerte mit dem Quicksortalgorithmus sortieren
    dgm_sort =quickSort(dgm_sort, 0, count-1);
    return dgm_sort;
}


         

     
 public void init()
{
    
     double[][] fliessrichtung;
     double[][] dgm;
     double[][] dgm_start;
     double[][] hruraster_start;
     double[][] hruraster;
   
     double[][] flgew_start=null;
     double[][] flgew=null;
     double[][] flgew1=null;
     double[][] flgew2=null;
     double[][] hangneigung=null;
     
     
     
     //Einlesen der relevanten Informationen als ASCII-Datensaetze
   
     String dirName = this.getModel().getWorkspaceDirectory().getPath();
     double rastersize1=Rastersize(JAMSTools.CreateAbsoluteFileName(dirName,dgmFile.getValue()));
     int rastersize=(int)rastersize1;
     hruraster_start=Einlesen(JAMSTools.CreateAbsoluteFileName(dirName,hruRasterFile.getValue()));
     dgm_start=Einlesen(JAMSTools.CreateAbsoluteFileName(dirName,dgmFile.getValue()));
        
  
     flgew_start=Einlesen(JAMSTools.CreateAbsoluteFileName(dirName,flgewFile.getValue()));
 

     double aufloesung=gridresolution.getValue();
     double zeitschritt=timescale.getValue();
   
     if (zeitschritt==1) {zeitschritt=60*60*24;} //in seconds
     if (zeitschritt==2) {zeitschritt=60*60;}    //in seconds
     
     
     rastersize=(int)(rastersize*aufloesung);
     modelrastersize.setValue((double)rastersize);
     
     dgm= new double[(int)(dgm_start.length/aufloesung)][(int)(dgm_start[0].length/aufloesung)];
     hruraster= new double[(int)(dgm_start.length/aufloesung)][(int)(dgm_start[0].length/aufloesung)];
     int i,j;
    
     for (i=0;i<dgm_start.length-aufloesung;i++)
     {
         for (j=0;j<dgm_start[0].length-aufloesung;j++)
         {
           
           dgm[(int)(i/aufloesung)][(int)(j/aufloesung)]=dgm[(int)(i/aufloesung)][(int)(j/aufloesung)]+dgm_start[i][j];
         
         }
     }
    
     flgew = new double[dgm.length][dgm[0].length];
     flgew1 = new double[dgm.length][dgm[0].length];
     flgew2 = new double[dgm.length][dgm[0].length];
     for (i=0;i<dgm.length;i++)
     {
         for (j=0;j<dgm[0].length;j++)
         { 
            flgew[i][j]=-1;
            
         }
     }    
     int k,l;
     for (i=0;i<dgm.length;i++)
     {
         for (j=0;j<dgm[0].length;j++)
         { 
            for (k=0;k<aufloesung;k++)
            {
                for (l=0;l<aufloesung;l++)
                { 
                    if (flgew_start[(int)(i*aufloesung+k)][(int)(j*aufloesung+l)]!=-1)
                    { 
                        //Pegel soll immer erhalten bleiben
                        if (flgew_start[(int)(i*aufloesung+k)][(int)(j*aufloesung+l)]==123456)
                        {
                            flgew[i][j]=123456;
                            k=(int)aufloesung+1;
                            l=(int)aufloesung+1;
                        }
                        else 
                        {
                            flgew[i][j]=flgew_start[(int)(i*aufloesung+k)][(int)(j*aufloesung+l)];
                        }
                    }
                }
            }
            flgew1[i][j]=flgew[i][j];
            flgew2[i][j]=flgew[i][j];
         }
     }
  
     
     for (i=0;i<dgm.length;i++)
     {
         for (j=0;j<dgm[0].length;j++)
         { 
            dgm[i][j]=dgm[i][j]/(aufloesung*aufloesung);
            if ((dgm[i][j]>-1) && (dgm[i][j]<0.1) ) {dgm[i][j]=-1;}
            
            if ((i*aufloesung<hruraster_start.length) &&  (j*aufloesung<hruraster_start[0].length))
            {hruraster[i][j]=hruraster_start[(int)(i*aufloesung)][(int)(j*aufloesung)];}
            
         }
     }
     
    // Ausgabe(flgew,"C:/Arbeit/flgew");
     fliessrichtung=Fliessrichtung(dgm); 
     
     //Bestimmen der Abflussrichtung entlang des Fliessgewaessers 
     //Aufbau einer Datenstruktur zur Verwaltung der Fliessgewaesserabschnitte
      
     ArrayList<ArrayList<Point>> reachlist = new ArrayList<ArrayList<Point>>();
     ArrayList<Point> raster_in_reach = new ArrayList<Point>();
     
     
    int count=0;
     int zahl=1;
     while(zahl!=0) //pruefe, ob es Reaches gibt, die noch nicht verarbeitet worden sind
     {
    // Ausgabe(flgew2,"C:/Arbeit/flgew2");    
     zahl=0;
    
     for(i=0;i<flgew2.length;i++)
        {
            for(j=0;j<flgew2[0].length;j++)
            {
                //schauen, ob es sich um ein reach-Start-Pixel handelt
              if (flgew2[i][j]!=-1 && flgew2[i][j]!=123456)
                {
                raster_in_reach = new ArrayList<Point>(); //Neue Liste erzeugen
                count=0;   
                //zaehlt, wieviele Nachbarpixel im flgew liegen um das Startpixel zu ermitteln
                if (flgew2[i+1][j]==-1)
                    {count++;}
                if (flgew2[i-1][j]==-1)
                    {count++;}
                if (flgew2[i][j+1]==-1)
                    {count++;}
                if (flgew2[i][j-1]==-1)
                    {count++;}
                if (flgew2[i+1][j+1]==-1)
                    {count++;}
                if (flgew2[i+1][j-1]==-1)
                    {count++;}
                if (flgew2[i-1][j+1]==-1)
                    {count++;}
                if (flgew2[i-1][j-1]==-1)
                    {count++;}
                if (count==7 )
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i+1][j+1]!=-1 && flgew2[i][j+1]!=-1)
               {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i+1][j+1]!=-1 && flgew2[i+1][j]!=-1)
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i+1][j]!=-1 && flgew2[i+1][j-1]!=-1)
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i+1][j-1]!=-1 && flgew2[i][j-1]!=-1)
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i-1][j-1]!=-1 && flgew2[i][j-1]!=-1)
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i-1][j]!=-1 && flgew2[i-1][j-1]!=-1)
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i-1][j+1]!=-1 && flgew2[i-1][j]!=-1)
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
                if (count==6 && flgew2[i-1][j+1]!=-1 && flgew2[i][j+1]!=-1)
                {
                    flgew2=Mapping(flgew2,raster_in_reach, i, j, count, flgew2[i][j],1,2);
                    zahl++;
                    reachlist.add(raster_in_reach);
                }
               
                }
            }
        }
     // Ausgabe(flgew2,"C:/Arbeit/flgew2");
      }
      
     
     fliessrichtung=flgew(dgm,fliessrichtung, flgew, flgew1, hruraster);
    // Ausgabe(fliessrichtung,"C:/Arbeit/fliessrichtung");
     for(i=0;i<fliessrichtung.length;i++)
      {
            for(j=0;j<fliessrichtung[0].length;j++)
            {
           
             if (flgew1[i][j]!=-1)
             {
            
             flgew1[i][j]=fliessrichtung[i][j];
            // hruraster[i][j]=-1;
             }
            }
      }
      
     //gehlbergspezifisch
    
     //Flie?richtungen am Pegel anpassen
     for (i=0;i<flgew.length;i++)
     {
         for (j=0;j<flgew[0].length;j++)
         { 
            if (flgew[i][j]==123456)
            {
                flgew1[i][j]=0;
                flgew1[i-1][j-1]=2;
                flgew1[i-1][j]=1;
                flgew1[i-1][j+1]=128;
                flgew1[i][j-1]=4;
                flgew1[i][j+1]=64; 
                flgew1[i+1][j-1]=8;
                flgew1[i+1][j]=16;
                flgew1[i+1][j+1]=32;
                fliessrichtung[i][j]=0;
                fliessrichtung[i-1][j-1]=2;
                fliessrichtung[i-1][j]=1;
                fliessrichtung[i-1][j+1]=128;
                fliessrichtung[i][j-1]=4;
                fliessrichtung[i][j+1]=64; 
                fliessrichtung[i+1][j-1]=8;
                fliessrichtung[i+1][j]=16;
                fliessrichtung[i+1][j+1]=32;
                i=flgew.length;
                j=flgew[0].length;
            }
             
         }
     }
     
  //  Ausgabe(flgew1,"C:/Arbeit/flgew1");
  //  Ausgabe(flgew2,"C:/Arbeit/flgew2");
  //  Ausgabe(fliessrichtung,"C:/Arbeit/fliessrichtung");
  //  Ausgabe(dgm,"C:/Arbeit/dgm");
     //Hangneigung berechnen 
     hangneigung= new double[dgm.length][dgm[0].length];
     hangneigung=Hangneigung(dgm, fliessrichtung, hangneigung, rastersize);
     
      
     //Anlegen einer Datenstruktur die das spaetere Mapping zwischen verschiedenen 
     //raumlichen Diskretisierungen ermoeglichen soll. 
     
     ArrayList<ArrayList<Point>> hrulist = new ArrayList<ArrayList<Point>>();
     ArrayList<Point> raster_in_hru = new ArrayList<Point>();
     
     //Aufbau der Datenstruktur  
        
        count=0;
        for(i=0;i<hruraster.length;i++)
        {
            for(j=0;j<hruraster[0].length;j++)
            {
             if (hruraster[i][j]!=-1)
                {
           
                raster_in_hru = new ArrayList<Point>(); //Neue Liste erzeugen
                hruraster=Mapping(hruraster,raster_in_hru, i, j, count, hruraster[i][j],1,1);
                hrulist.add(raster_in_hru);
                count++;
                //Zaehlt nur HRUs die groesser als zwei pixel sind. Eleminierung aus der Liste???
                }
            }
        }
    
    

        
        //DGM Sortieren, damit Rasterzellen von der niedrigsten zur hoechsten abgearbeitet werden koennen
        
        int dgm_sort[][]=new int[3][dgm.length*dgm[0].length]; //Array, indem die H?henwerte sortiert gespeichert werden
        dgm_sort=Sort(dgm_sort, dgm); //Sortieren der dgm-Werte nach Ihrer H?he
       
        //Bestimmen in welche Rasterzelle
        //die jeweilige Rasterzelle entwaessert (in Abhaengigkeit von der Hangneigung)
        
        Point [][] RoutingRD1 =new Point [dgm.length][dgm[0].length]; 
        Point [][] RoutingRD2 =new Point [dgm.length][dgm[0].length]; 
        Point [][] RoutingRG1 =new Point [dgm.length][dgm[0].length]; 
        Point [][] RoutingRG2 =new Point [dgm.length][dgm[0].length]; 
        double [][] Pfadlaenge=new double[dgm.length][dgm[0].length];
        double [][] Fuellstand_Reaches=new double[dgm.length][dgm[0].length]; //Gedaechtnis des Wasserstandes
        double [][] Qin_Reaches=new double[dgm.length][dgm[0].length]; //Gedaechtnis der Uebergaenge von Zeitschritt t zu t+1
        
        double RD1_Koeff=RD1_Weg_Koeff.getValue();
        double RD2_Koeff=RD2_Weg_Koeff.getValue();
        double RG1_Koeff=RG1_Weg_Koeff.getValue();
        double RG2_Koeff=RG2_Weg_Koeff.getValue();
        
      
        Pfadlaenge=Weglaenge(hangneigung, RD1_Koeff, zeitschritt, rastersize);
        RoutingRD1=Pfad1(dgm, RoutingRD1, fliessrichtung, Pfadlaenge,aufloesung); 
        Pfadlaenge=Weglaenge(hangneigung, RD2_Koeff, zeitschritt, rastersize);
        RoutingRD2=Pfad1(dgm, RoutingRD2, fliessrichtung, Pfadlaenge,aufloesung); 
        Pfadlaenge=Weglaenge(hangneigung, RG1_Koeff, zeitschritt, rastersize);
        RoutingRG1=Pfad1(dgm, RoutingRG1, fliessrichtung, Pfadlaenge,aufloesung); 
        Pfadlaenge=Weglaenge(hangneigung, RG2_Koeff, zeitschritt, rastersize);
        RoutingRG2=Pfad1(dgm, RoutingRG2, fliessrichtung, Pfadlaenge,aufloesung);
        
       
        
      
     
           Point p;
       
      
      
         
       //Anlegen der Raster, die sich die Wasserverteil?ng im Gebiet merken
           
        double[][] RD1_vraster=null;
        RD1_vraster=new double[hruraster.length][hruraster[0].length];
        double[][] RD2_vraster=null;
        RD2_vraster=new double[hruraster.length][hruraster[0].length];
        double[][] RG1_vraster=null;
        RG1_vraster=new double[hruraster.length][hruraster[0].length];
        double[][] RG2_vraster=null;
        RG2_vraster=new double[hruraster.length][hruraster[0].length];
        double[][] reachinfo=null;
        reachinfo=new double[4][reachlist.size()+100];//falls welche durch das zusammenfassen wegfallen
        
        information.setObject("hruraster",hruraster );
        information.setObject("fliessrichtung",fliessrichtung );
        information.setObject("dgm",dgm );
        information.setObject("flgew",flgew );
        information.setObject("flgew1",flgew1 );//flie?richtung innerhalb des flie?gew?ssers
        information.setObject("routingRD1",RoutingRD1 );
        information.setObject("routingRD2",RoutingRD2 );
        information.setObject("routingRG1",RoutingRG1 );
        information.setObject("routingRG2",RoutingRG2 );
        information.setObject("hangneigung",hangneigung );
        information.setObject("dgm_sort",dgm_sort );
        information.setObject("hrulist",hrulist );
        information.setObject("reachlist", reachlist);
        information.setObject("RD1_vraster",RD1_vraster);
        information.setObject("RD2_vraster",RD2_vraster);
        information.setObject("RG1_vraster",RG1_vraster);
        information.setObject("RG2_vraster",RG2_vraster);
        information.setObject("reachinfo",reachinfo);
        

         double[][] RD1_raster=null;
         double[][] RD1_hru=null;
         double[][] RD1_reach=null;
         double[][] RD2_raster=null;
         double[][] RD2_hru=null;
         double[][] RD2_reach=null;
         double[][] RG1_raster=null;
         double[][] RG1_hru=null;
         double[][] RG1_reach=null;
         double[][] RG2_raster=null;
         double[][] RG2_hru=null;
         double[][] RG2_reach=null;
         
       
         RD1_raster=new double[hruraster.length][hruraster[0].length];
         RD1_hru=new double[hrulist.size()][2];
         RD1_reach=new double[reachlist.size()][2];
         RD2_raster=new double[hruraster.length][hruraster[0].length];
         RD2_hru=new double[hrulist.size()][2];
         RD2_reach=new double[reachlist.size()][2];
         RG1_raster=new double[hruraster.length][hruraster[0].length];
         RG1_hru=new double[hrulist.size()][2];
         RG1_reach=new double[reachlist.size()][2];
         RG2_raster=new double[hruraster.length][hruraster[0].length];
         RG2_hru=new double[hrulist.size()][2];
         RG2_reach=new double[reachlist.size()][2];

         
         fuellstand.setObject("RD1_raster",RD1_raster);
         fuellstand.setObject("RD1_hru",RD1_hru);
         fuellstand.setObject("RD2_raster",RD2_raster);
         fuellstand.setObject("RD2_hru",RD2_hru);
         fuellstand.setObject("RG1_raster",RG1_raster);
         fuellstand.setObject("RG1_hru",RG1_hru);
         fuellstand.setObject("RG2_raster",RG2_raster);
         fuellstand.setObject("RG2_hru",RG2_hru);
         fuellstand.setObject("RD1_reach",RD1_reach);
         fuellstand.setObject("RD2_reach",RD2_reach);
         fuellstand.setObject("RG1_reach",RG1_reach);
         fuellstand.setObject("RG2_reach",RG2_reach);
         fuellstand.setObject("fuellstand_reaches", Fuellstand_Reaches);  
         fuellstand.setObject("Qin_reaches", Qin_Reaches);
         
      
     
}   
public void run() 
{
        init();
}
    
public void cleanup() 
{
}

}