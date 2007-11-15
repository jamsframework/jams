package jams.tspaces;

import java.awt.Graphics;

import javax.swing.*;
import java.awt.Color;
import java.awt.FontMetrics;
import java.lang.Math;

public class DrawPanel extends JPanel{
	Node wurzel;
	Liste p;
	Node[] elements;
	int[][] linien;
	int[][] rectangle;
	int i=0;
	int maxx=0;
	int maxy=0;
	boolean firsttime=true;
	
	public DrawPanel(Node w, Liste partitioiningtree){
		super();
		wurzel=w;
		p=partitioiningtree;	
		int[] max=findmax(w);
		maxx=max[0];
		maxy=max[1];		
	}
	
	public int[] findmax(Node wurzel){
		int[] rec=new int[2];
		rec[0]=wurzel.xkoordinate;
		rec[1]=wurzel.ykoordinate;		
		int[] min=new int[2];
		min=rec;
		if (wurzel.getlinkesterSohn()!=null){
			min=findmax(wurzel.getlinkesterSohn());
			rec[0]=Math.max(rec[0],min[0]);
			rec[1]=Math.max(rec[1],min[1]);
		}
		if (wurzel.getrechterBruder()!=null){
			min=findmax(wurzel.getrechterBruder());
			rec[0]=Math.max(rec[0],min[0]);
			rec[1]=Math.max(rec[1],min[1]);
		}
		return rec;
	}
	
	public void makeElementArray(Node wurzel,Graphics g){
		elements[i]=wurzel;   	
		Node sohn=wurzel.getlinkesterSohn();
		for(int m=1; m<=wurzel.getDegree(); m++){
			linien[i][0]=wurzel.xkoordinate+g.getFontMetrics().stringWidth((String)wurzel.getElement())/2;
			linien[i][1]=wurzel.ykoordinate+5;
			linien[i][2]=sohn.xkoordinate+g.getFontMetrics().stringWidth((String)sohn.getElement())/2;
			linien[i][3]=sohn.ykoordinate-g.getFontMetrics().getHeight();
			i++;
			makeElementArray(sohn,g);
			sohn=sohn.getrechterBruder();	
			Graphics gr=this.getGraphics();
			
		}
		
	}	
	
	public int[] findgroup(Node wurzel,Graphics g){
		int[] rec=new int[4];
		rec[0]=wurzel.xkoordinate;
		rec[1]=wurzel.ykoordinate-g.getFontMetrics().getHeight();
		rec[2]=wurzel.xkoordinate+g.getFontMetrics().stringWidth((String)wurzel.getElement());
		rec[3]=wurzel.ykoordinate;
		int[] min=new int[4];
		min=rec;
		if (wurzel.getlinkesterSohn()!=null){
			min=findgroup(wurzel.getlinkesterSohn(),g);
			rec[0]=Math.min(rec[0],min[0]);
			rec[1]=Math.min(rec[1],min[1]);
			rec[2]=Math.max(rec[2],min[2]);
			rec[3]=Math.max(rec[3],min[3]);
		}
		if (wurzel.getrechterBruder()!=null){
			min=findgroup(wurzel.getrechterBruder(),g);
			rec[0]=Math.min(rec[0],min[0]);
			rec[1]=Math.min(rec[1],min[1]);
			rec[2]=Math.max(rec[2],min[2]);
			rec[3]=Math.max(rec[3],min[3]);
		}
		return rec;
	}
	
  @Override 
  protected void paintComponent( Graphics g ) 
  { 
    super.paintComponent( g );  
    if (firsttime){
	    elements=new Node[wurzel.getSize()];
		linien=new int[wurzel.getSize()][4];
		makeElementArray(wurzel,g);   
	    for (int i=0; i<elements.length; i++){
	    	g.drawString(elements[i].getElement().toString(), elements[i].xkoordinate, elements[i].ykoordinate);    	
	    	g.drawLine(linien[i][0], linien[i][1],linien[i][2],linien[i][3]);
	    }
	    Liste z=p;
	    int j=0;
	    while(p!=null){
	    	j++;
	    	p=p.getNext();
	    }
	    p=z;
		rectangle=new int[j][4];
		j=0;
	    while (p!=null){    
	    	int[] rec=findgroup((Node)p.getElement(),g);
	    	g.drawRect(rec[0], rec[1], rec[2]-rec[0], rec[3]-rec[1]);
	    	rectangle[j][0]=rec[0];
	    	rectangle[j][1]=rec[1];
	    	rectangle[j][2]=rec[2]-rec[0];
	    	rectangle[j++][3]=rec[3]-rec[1];
	    	//System.out.println(rec[0]+" "+rec[1]+" "+rec[2]+ " "+rec[3]);
	    	p=p.getNext();
	    }
	    firsttime=false;
    }
    else{
	 for (int m=0; m<elements.length; m++){
    	g.drawString(elements[m].getElement().toString(), elements[m].xkoordinate, elements[m].ykoordinate);    	
    	g.drawLine(linien[m][0], linien[m][1],linien[m][2],linien[m][3]);
	 }	
	 for (int m=0; m<rectangle.length; m++){
	    	g.drawRect(rectangle[m][0],rectangle[m][1],rectangle[m][2],rectangle[m][3] );    	
		 }	
		 
    }
    
  } 
}