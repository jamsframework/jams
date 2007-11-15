package jams.tspaces;
import javax.swing.*;
import java.awt.*;
import org.unijena.jams.data.*;

public class BaumpartEntity {
    int maxsize; //maximale Größe einer Partitionierung
    int vertikaleverschiebung=50;
    int vertikal=40;
	int horizontal=80;
	
	NodeEntity wurzel; // Wurzelknoten des gesamten Baumes
	
	Liste partitioningtree; // Liste von Teilbäumen, in der die fertige Partitionierung steht
	
	JAMSEntityCollection hrus;
	JAMSEntityCollection reaches;
	
    public BaumpartEntity(JAMSEntityCollection h, JAMSEntityCollection r, int msize){
    	this.hrus=h;
    	this.reaches=r;
    	maxsize=msize;
    	
    	//bestimmt die Anzahl der Gebiete in den einzelnen Datein
    	int hruanzahl=hrus.getEntityArray().length;
        int reachanzahl=reaches.getEntityArray().length;             
        
        //Beispiel: hru mit ID 1 fließt in hru mit ID 2, hru mit ID 1 wird als erstes Element ausgwählt und gespeichert, 2hru ist Vaterknoten von 1hru
        
        //Zwischenspeicherung der Indizes der hrus/reaches, mit Vermerk ob es sich um ein hru oder ein reach handelt (Beispiel: namensArray[1]="1hru")
        String[] namensArray=new String[hruanzahl+reachanzahl];
       
        //Zwischenspeicherung der Fliesselemente (hru/reaches) entsprechend dem Namen (Beispiel: toFlowArray[1]="2hru")
        String[] toFlowArray=new String[hruanzahl+reachanzahl];
        
        //Zwischenspeicherung der Referenzen auf die ursprünglichen Entitys
        JAMSEntity[] EntityArray=new JAMSEntity[hruanzahl+reachanzahl];
        
        //füllt die Arrays (namensarray, toFlowarray, EntityArray) mit den entsprechenden Werten
        fillArray(hrus, "hru", 0, namensArray,toFlowArray, EntityArray);
        fillArray(reaches, "reach", hruanzahl, namensArray,toFlowArray, EntityArray);

        //Speicherung der Verbindung von Kindknoten zu Vaterknoten
        int[] baumarray=new int[hruanzahl+reachanzahl];
        
        //füllt das baumarray
        bindArray(namensArray,toFlowArray,EntityArray,baumarray);// bindArray(reachdatei, "reach", hruanzahl+1, namensarray,baumarray);
        
        //legt den Baum aus dem baumarray an. Der Elementeintrag entspricht dem gespeicherten Namen im namensarray, eine Referenz auf den Entity wird zusätzlich hinterlegt
        wurzel=baum(baumarray,EntityArray, namensArray);
        baumumsortierung(wurzel);     
        treecoordinatesdown(wurzel,0);//berechnet Koordinaten        
        //baumausgabe(wurzel);
        
        //legt eine Liste von Bäumen an, die dann nacheinander parallel bearbeitet werden können
        partitioningtree = partitioning(wurzel);
        //listenausgabe(partitioningtree);
        
        saveEntityInCollection(wurzel);
        //baumausgabe(wurzel);
    }
    
    public void partitioninggraphic(final NodeEntity wurzel, final Liste partitioningtree){
    	JFrame frame = new JFrame("baum");
    	//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	DrawPanel drawPanel= new DrawPanel(wurzel, partitioningtree);
    	drawPanel.setPreferredSize(new Dimension(drawPanel.maxx+50,drawPanel.maxy+50));
    	drawPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));	    
	    //frame.setSize(800, 600);
	    JScrollPane contentPane=new JScrollPane(drawPanel);
	    contentPane.setPreferredSize(new Dimension(800, 600));
    	frame.getContentPane().add(contentPane);
    	frame.pack();
	    frame.setVisible(true);    	
    }
    
    public void treecoordinatesdown(NodeEntity wurzel, int v){
    	NodeEntity linkesterSohn=wurzel;
    	while (linkesterSohn.getlinkesterSohn()!=null){
    		linkesterSohn=linkesterSohn.getlinkesterSohn();    		
    	}
    	linkesterSohn.xkoordinate=v;
    	linkesterSohn.ykoordinate=vertikaleverschiebung+vertikal*linkesterSohn.getHoehe();
    	if (linkesterSohn.getrechterBruder()!=null){
    		treecoordinatesdown(linkesterSohn.getrechterBruder(),v+horizontal);
    	}
    	else{
    		treecoordinatesup(linkesterSohn.getVater(),v);
    	}
    }
    
    public void treecoordinatesup(NodeEntity wurzel, int v){    	
    	int sumxkoordinaten=0;
    	NodeEntity sohn=wurzel.getlinkesterSohn();
    	for(int i=1; i<=wurzel.getDegree(); i++){
    		sumxkoordinaten+=sohn.xkoordinate;
    		sohn=sohn.getrechterBruder();
    	}
    	wurzel.xkoordinate=sumxkoordinaten/wurzel.getDegree();
    	wurzel.ykoordinate=vertikaleverschiebung+vertikal*wurzel.getHoehe();
    	if (wurzel.getrechterBruder()!=null){
    		treecoordinatesdown(wurzel.getrechterBruder(),v+horizontal);
    	}
    	else{
    		if (wurzel.getHoehe()!=0) {treecoordinatesup(wurzel.getVater(),v);}
    	}
    }
    
    public Liste<NodeEntity> partitioning(NodeEntity wurzel){
    	NodeEntity wurzelcopy=wurzel.copyTree(wurzel.getVater());
        return partitioning(wurzelcopy, 1);
    }
    
    public Liste<NodeEntity> partitioning(NodeEntity wurzel, int n){
        baumumsortierung(wurzel); //sortiert den Baum so um, dass immer die Knoten mit größter Size ganz links stehen
        NodeEntity kindknoten=wurzel;
        Liste<NodeEntity> teilbaum = new Liste<NodeEntity>();
        teilbaum.setNummer(n); //nummeriert die Teilbäume durch
        if (wurzel.getSize()<=maxsize){//letzter Baum
            teilbaum.setElement(wurzel);
            teilbaum.setNext(null);
            return teilbaum;
        }
        while (kindknoten.getSize()>maxsize){//Suche nach Teilbaum, der klein genug ist.
            kindknoten=kindknoten.getlinkesterSohn();
        }
        int size=kindknoten.getSize();
        
        //löscht die Verbindung des Vaterknotens zum gefundenen Teilbaum
        kindknoten.getVater().setDegree(kindknoten.getVater().getDegree()-1);
        kindknoten.getVater().setlinkesterSohn(kindknoten.getrechterBruder());
        kindknoten.setrechterBruder(null);
        
        teilbaum.setElement(kindknoten); //fügt den neuen Teilbaum in die Liste der Teilbäume ein und aktualisiert Höhe der Knoten im Teilbaum
        
        while (kindknoten.getVater()!=null){
            kindknoten=kindknoten.getVater();
            kindknoten.setSize(kindknoten.getSize()-size);
        }//Aktualisieren der "Size"-Einträge oberhalb des gefundenen Teilbaums
        
        teilbaum.setNext(partitioning(wurzel, n+1));
        return teilbaum;
    }
    
    public void baumumsortierung(NodeEntity wurzel){
        if (wurzel.getDegree()!=0){
            NodeEntity[] kindarray=new NodeEntity[wurzel.getDegree()];
            NodeEntity naechsterSohn=wurzel.getlinkesterSohn();
            for (int i=0; i<kindarray.length; i++){
                kindarray[i]=naechsterSohn;
                naechsterSohn=naechsterSohn.getrechterBruder();
            }//speichert alle Kinderknoten in einem Array
            for(int i=0; i<kindarray.length; i++){
            	NodeEntity max=kindarray[i];
                int pos=i;
                for (int j=i+1; j<kindarray.length; j++){
                    if(max.getSize()<kindarray[j].getSize()){
                        max=kindarray[j];
                        pos=j;
                    }
                }
                kindarray[pos]=kindarray[i];
                kindarray[i]=max;
            }//sortiert die Kinder nach ihrer Größe
            wurzel.setlinkesterSohn(kindarray[0]);
            for (int i=0; i<kindarray.length; i++){
                if (i<kindarray.length-1){
                    kindarray[i].setrechterBruder(kindarray[i+1]);
                } else{
                    kindarray[i].setrechterBruder(null);
                }//verändert die Baumstruktur so, dass das größte Kind ganz links steht
                baumumsortierung(kindarray[i]);//sortiert auch die Kinder der Kinder so um, dass das größte Kind ganz links steht
            }
        }
    }
    
    public void listenausgabe(Liste<NodeEntity> wurzel){
       //// if (wurzel.getNummer() == 0)
        System.out.println("baum: " + wurzel.getNummer());
        baumausgabe(wurzel.getElement()); System.out.println();
        if(wurzel.getNext()!=null){
            listenausgabe(wurzel.getNext());
        }
    }
    
    public void saveEntityInCollection(NodeEntity wurzel){
         //if (wurzel.getHoehe()==0)
         //System.out.println("hoehe: " + wurzel.getHoehe() + ": " + wurzel.getElement()+ " Vaterknoten: " + wurzel.getVater().getElement() +" Size: "+ wurzel.getSize()+ " Degree: "+ wurzel.getDegree()+ " xcoordinate: "+ wurzel.xkoordinate+ " ID: "+((JAMSEntity)(wurzel.getElement2())).getValue().get("ID"));    	 
		 JAMSEntity wurzelEntity=((JAMSEntity)(wurzel.getElement2()));
		 JAMSEntity linkesterSohnEntity=null;
		 JAMSEntity rechterBruderEntity=null;
		 if (wurzel.getlinkesterSohn()!=null) linkesterSohnEntity=((JAMSEntity)(wurzel.getlinkesterSohn().getElement2()));
		 if (wurzel.getrechterBruder()!=null) rechterBruderEntity=((JAMSEntity)(wurzel.getrechterBruder().getElement2()));
		 wurzelEntity.setObject("linkesterSohn",linkesterSohnEntity);
		 wurzelEntity.setObject("rechterBruder",rechterBruderEntity);		 
		 NodeEntity naechsterSohn=wurzel.getlinkesterSohn();
         while (naechsterSohn!=null){
        	 saveEntityInCollection(naechsterSohn);
             naechsterSohn=naechsterSohn.getrechterBruder();
         }
     }
    
    public void baumausgabe(NodeEntity wurzel){
       //if (wurzel.getHoehe()==0)
        System.out.println("hoehe: " + wurzel.getHoehe() + ": " + wurzel.getElement()+ " Vaterknoten: " + wurzel.getVater().getElement() +" Size: "+ wurzel.getSize()+ " Degree: "+ wurzel.getDegree()+ " xcoordinate: "+ wurzel.xkoordinate+ " ID: "+((JAMSEntity)(wurzel.getElement2())).getValue().get("ID"));
        if (((JAMSEntity)wurzel.getElement2()).getValue().get("linkesterSohn")!=null) System.out.print("linkesterSohn:" + ((JAMSEntity)((JAMSEntity)wurzel.getElement2()).getValue().get("linkesterSohn")).getValue().get("ID"));
        if (((JAMSEntity)wurzel.getElement2()).getValue().get("rechterBruder")!=null) System.out.print(" rechterBruder:"+ ((JAMSEntity)((JAMSEntity)wurzel.getElement2()).getValue().get("rechterBruder")).getValue().get("ID"));
        System.out.print("\n");
        NodeEntity naechsterSohn=wurzel.getlinkesterSohn();
        while (naechsterSohn!=null){
            baumausgabe(naechsterSohn);
            naechsterSohn=naechsterSohn.getrechterBruder();
        }
    }
    
    public NodeEntity baum(int[] barray, JAMSEntity[] ea, String[] narray){
        return baum(barray, narray, ea, new NodeEntity<String,JAMSEntity>(0,"0",null));
    }
    
    public NodeEntity baum(int[] barray, String[] narray, JAMSEntity[] ea, NodeEntity<String,JAMSEntity> node){//gibt jeweils den linkesten Teilbaum des Wurzelknoten node zurück
        if (node.getIndex()==0){
            node.setHoehe(-1);
        }
        NodeEntity[] soehne = findChildren(barray,narray, ea, node.getIndex()); //legt ein Array mit allen Sohnknoten an
        int size=1;
        for (int i=0; i<soehne.length; i++){
            if (i<soehne.length-1){
                soehne[i].setrechterBruder(soehne[i+1]); //lässt jeden Knoten auf seinen rechten Bruder zeigen
            }
            soehne[i].setVater(node); //speichert Vaterknoten
            soehne[i].setHoehe(node.getHoehe()+1); //errechnet die Höhe der Kinderknoten aus Höhe des Vaterknotens+1
            NodeEntity teilbaum=baum(barray, narray, ea, soehne[i]); //bestimmt für jedes Kind wiederum den linkesten Teilbaum und die Verkettung der Geschwister dieses Teilbaums
            soehne[i].setlinkesterSohn(teilbaum);
            size+=soehne[i].getSize();
        }
        node.setDegree(soehne.length);
        node.setSize(size);
        if (soehne.length>0) {
            return soehne[0]; //wenn der aktuelle Wuerzelknoten Kinder hat, wird der linkeste Teilbaum des Wurzelknotens zurückgegeben
        }
        return null; //ansonsten wird null zurückgegeben
    }
    
    public NodeEntity[] findChildren(int[] b, String n[], JAMSEntity[] ea, int index){
        int sohnanzahl=0;
        for (int i=1; i<b.length; i++){
            if (b[i]==index){
                sohnanzahl=sohnanzahl+1;//zählt alle Array-Einträge, die den gegeben Index als Vater haben
            }
        }
        NodeEntity[] kinderarray=new NodeEntity[sohnanzahl];
        sohnanzahl=0;
        for (int i=1; i<b.length; i++){
            if (b[i]==index){
                kinderarray[sohnanzahl]= new NodeEntity<String,JAMSEntity>(i,n[i],ea[i]);//legt die Kinderknoten an und speichert sie im Array
                sohnanzahl=sohnanzahl+1;
            }
        }
        return kinderarray;
    }
    
   public void bindArray(String[] namensArray,String[] toFlowArray,JAMSEntity[] EntityArray,int[] baumarray){
       String nameToSearch;
       int toIndex;
       boolean found;
       namensArray[0]="";
       String stringElement;
       for (int i=0;i<namensArray.length;i++){
           if (namensArray[i]==null){
               System.out.println(i);
           }
       }//überprüft ob alle Felder gefüllt sind
       	   
	   for ( int i = 1;i<toFlowArray.length;i++ ){
	       nameToSearch  =  toFlowArray[i];
	       if (nameToSearch != null){
		       found=false;
		       toIndex=1;
		       while (toIndex<namensArray.length && !found){
		           stringElement=(String)namensArray[toIndex];
		           if (stringElement.equals(nameToSearch))found = true;
		           else  toIndex+=1;
		       } // sucht den Index im namensArray, in dem der gesuchte Vaterknoten steht
		       if (toIndex<namensArray.length) 
		           baumarray[i]=toIndex; // speichert den Index des gesuchten Vaterknoten
		       else 
		           baumarray[i]=0;
	       }	      
	   }          
    }
     
      public void fillArray(JAMSEntityCollection col, String typ,int arrayStartPos, String[] namensArray,String[] toFlowArray, JAMSEntity[] EntityArray){    	                      
	  	  int index=arrayStartPos;
	      JAMSEntity[] ea=col.getEntityArray();
	      for (int i=0; i<ea.length; i++){
	    	  	  EntityArray[i+index]=ea[i]; //speichert den Entity im EntityArray
	        	  int id=(int)(Math.round(Double.parseDouble(ea[i].getValue().get("ID").toString())));
	        	  namensArray[i+index]=(id+typ); //speichert ID+Typ im namensArray
	        	  if(ea[i].getValue().get("to_poly") != null){
	        		  int topoly=(int)(Math.round(Double.parseDouble(((JAMSEntity)(ea[i].getValue().get("to_poly"))).getValue().get("ID").toString())));
	        		  toFlowArray[i+index]=topoly+"hru"; //speichert den Namen des Vaterknoten im toFlowArray (wenn dieser ein hru ist)
	        	  }
	        	  if(ea[i].getValue().get("to_reach") != null){
	        		  int toreach=(int)(Math.round(Double.parseDouble(((JAMSEntity)(ea[i].getValue().get("to_reach"))).getValue().get("ID").toString())));
	        		  toFlowArray[i+index]=toreach+"reach"; //speichert den Namen des Vaterknoten im toFlowArray (wenn dieser ein reach ist)
	        	  }                         
	      }
      }    
}


