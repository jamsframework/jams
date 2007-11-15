package jams.tspaces;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpace;

import java.awt.*;
import java.awt.event.*;

import java.net.URL;
import java.util.StringTokenizer;

public class Baumpart {
    
    String hrudatei;
    String reachdatei;
    int headerLines = 5;
    int maxsize;
    int hruToPolyPos = 7;// impliziert aktuell, dass die nächste Position to_reach enthält, etwas sperrig...
    int reachToReachPos = 3;
    int vertikaleverschiebung=50;
    int vertikal=40;
	int horizontal=80;
	Node wurzel;
	Liste partitioningtree;
    
    public Baumpart(String hdatei, String rdatei, int msize){
    	hrudatei=hdatei;
    	reachdatei=rdatei;
    	maxsize=msize;
    	//bestimmt die Anzahl der Gebiete in den einzelnen Datein
        int hruanzahl=anz(hrudatei);
        int reachanzahl=anz(reachdatei);
        //legt das Array, in dem die Verknüpfungen zwischen Kindern und Vätern stehen sollen, an
        int[] baumarray=new int[hruanzahl+reachanzahl];
        //legt das Array, in dem die ursprüngliche Bezeichnung jedes Index steht, an
        String[] namensarray=new String[hruanzahl+reachanzahl];
        //Zwischenspeicherung der Fliesselemente (hru/reaches) entsprechend dem Namen 
        String[] toFlowArray=new String[hruanzahl+reachanzahl];
        //String[] toFlowHashMap = new HashMap(hruanzahl+reachanzahl);
        
        fillArray(hrudatei, "hru", 0, namensarray,toFlowArray);
        fillArray(reachdatei, "reach", hruanzahl, namensarray,toFlowArray);
        
        
        
        bindArray(namensarray,toFlowArray,baumarray);// bindArray(reachdatei, "reach", hruanzahl+1, namensarray,baumarray);
        
        //legt den Baum aus dem baumarray an. Der Elementeintrag entspricht dem gespeicherten Namen im namensarray
        wurzel=baum(baumarray, namensarray);
        baumumsortierung(wurzel);     
        treecoordinatesdown(wurzel,0);//berechnet Koordinaten
        
        //legt eine Liste von Bäumen an, die dann nacheinander parallel bearbeitet werden können
        partitioningtree = partitioning(wurzel);       
    }
    
    public void partitioninggraphic(final Node wurzel, final Liste partitioningtree){
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
    
    public void treecoordinatesdown(Node wurzel, int v){
    	Node linkesterSohn=wurzel;
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
    
    public void treecoordinatesup(Node wurzel, int v){
    	int sumxkoordinaten=0;
    	Node sohn=wurzel.getlinkesterSohn();
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
    
    public Liste<Node> partitioning(Node wurzel){
    	Node wurzelcopy=wurzel.copyTree(wurzel.getVater());
        return partitioning(wurzelcopy, 1);
    }
    
    public Liste<Node> partitioning(Node wurzel, int n){
        baumumsortierung(wurzel); //sortiert den Baum so um, dass immer die Knoten mit größter Size ganz links stehen
        Node kindknoten=wurzel;
        Liste<Node> teilbaum = new Liste<Node>();
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
        teilbaum.setElement(kindknoten); //fügt den neuen Teilbaum in die Liste der Teilbäume ein und aktualisiert Höhe der Knoten im Tailbaum
        //Aktualisieren der "Size"-Einträge oberhalb des gefundenen Teilbaums
        while (kindknoten.getVater()!=null){
            kindknoten=kindknoten.getVater();
            kindknoten.setSize(kindknoten.getSize()-size);
        }
        teilbaum.setNext(partitioning(wurzel, n+1));
        return teilbaum;
    }
    
    public void baumumsortierung(Node wurzel){
        if (wurzel.getDegree()!=0){
            Node[] kindarray=new Node[wurzel.getDegree()];
            Node naechsterSohn=wurzel.getlinkesterSohn();
            for (int i=0; i<kindarray.length; i++){
                kindarray[i]=naechsterSohn;
                naechsterSohn=naechsterSohn.getrechterBruder();
            }
            for(int i=0; i<kindarray.length; i++){
                Node max=kindarray[i];
                int pos=i;
                for (int j=i+1; j<kindarray.length; j++){
                    if(max.getSize()<kindarray[j].getSize()){
                        max=kindarray[j];
                        pos=j;
                    }
                }
                kindarray[pos]=kindarray[i];
                kindarray[i]=max;
            }
            wurzel.setlinkesterSohn(kindarray[0]);
            for (int i=0; i<kindarray.length; i++){
                if (i<kindarray.length-1){
                    kindarray[i].setrechterBruder(kindarray[i+1]);
                } else{
                    kindarray[i].setrechterBruder(null);
                }
                baumumsortierung(kindarray[i]);
            }
        }
    }
    
    public void listenausgabe(Liste<Node> wurzel){
       //// if (wurzel.getNummer() == 0)
        System.out.println("baum: " + wurzel.getNummer());
        baumausgabe(wurzel.getElement()); System.out.println();
        if(wurzel.getNext()!=null){
            listenausgabe(wurzel.getNext());
        }
    }
    
    public void baumausgabe(Node wurzel){
       //if (wurzel.getHoehe()==0)
            System.out.println("hoehe: " + wurzel.getHoehe() + ": " + wurzel.getElement()+ " Vaterknoten: " + wurzel.getVater().getElement() +" Size: "+ wurzel.getSize()+ " Degree: "+ wurzel.getDegree()+ " xcoordinate: "+ wurzel.xkoordinate);
        Node naechsterSohn=wurzel.getlinkesterSohn();
        while (naechsterSohn!=null){
            baumausgabe(naechsterSohn);
            naechsterSohn=naechsterSohn.getrechterBruder();
        }
    }
    
    public Node baum(int[] barray, String[] narray){
        return baum(barray, narray, new Node<String>(0,"0"));
    }
    
    public Node baum(int[] barray, String[] narray, Node<String> node){//gibt jeweils den linkesten Teilbaum des Wurzelknoten node zurück
        if (node.getIndex()==0){
            node.setHoehe(-1);
        }
        Node[] soehne = findChildren(barray,narray, node.getIndex()); //legt ein Array mit allen Sohnknoten an
        int size=1;
        for (int i=0; i<soehne.length; i++){
            if (i<soehne.length-1){
                soehne[i].setrechterBruder(soehne[i+1]); //lässt jeden Knoten auf seinen rechten Bruder zeigen
            }
            soehne[i].setVater(node); //speichert Vaterknoten
            soehne[i].setHoehe(node.getHoehe()+1); //errechnet die Höhe der Kinderknoten aus Höhe des Vaterknotens+1
            Node teilbaum=baum(barray, narray, soehne[i]); //bestimmt für jedes Kind wiederum den linkesten Teilbaum und die Verkettung der Geschwister dieses Teilbaums
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
    
    public Node[] findChildren(int[] b, String n[], int index){
        int sohnanzahl=0;
        for (int i=1; i<b.length; i++){
            if (b[i]==index){
                sohnanzahl=sohnanzahl+1;//zählt alle Array-Einträge, die den gegeben Index als Vater haben
            }
        }
        Node[] kinderarray=new Node[sohnanzahl];
        sohnanzahl=0;
        for (int i=1; i<b.length; i++){
            if (b[i]==index){
                kinderarray[sohnanzahl]= new Node<String>(i,n[i]);//legt die Kinderknoten an und speichert sie im Array
                sohnanzahl=sohnanzahl+1;
            }
        }
        return kinderarray;
    }
    
    public int anz(String datei){
        int anzahl=0;
        int h=0;
        try{
            //bestimmt die hru/reach-Zahlen;
            Reader reader = new FileReader(datei);
            
            while (h!=-1){
                h=reader.read(); //erstes Zeichen der Zeile
                StringBuffer zeile=new StringBuffer();
                while(h!=10&&h!=-1){
                    zeile.append((char)h);
                    h=reader.read();
                }//liest eine Zeile ein
                anzahl+=1;
                if (zeile.indexOf("#end")>-1){
                    //Verhindern des Weiterzählens bei CR/LF nach dem #end
                    h=-1;
                    anzahl-=1;
                }
            }
//            while(h!=-1){
//                h=reader.read(); //liest alle Zeichen nacheinander ein
//                if (h==10) {
//                    anzahl+=1; //bei Zeilenumbruch wird die hru/rech-Zahl um eins erhöht
//                }
//            }
            anzahl-=5; //zieht die ersten "unwichtigen" Zeilen ab
        } catch(IOException e) {
            System.out.println( "Fehler beim Lesen der Datei" );
        }
        return anzahl;
    }
    
    public void bindArray(String[] namensarray,String[] toFlowArray,int[] baumarray){
       String nameToSearch;
       int toIndex,pos;
       boolean found;
       namensarray[0]="";
       String stringElement;
       for (int i=0;i<namensarray.length;i++){
           if (namensarray[i]==null){
               System.out.println(i);
           }
       }
     // Arrays.sort(namensarray);
       int all = 0;
       for ( int i = 1;i<toFlowArray.length;i++ ){

           nameToSearch  =  toFlowArray[i];
           if (nameToSearch != null){
           
           //toIndex wird als substring vom Schlüssel ausgelesen
           found=false;
           toIndex=1;
           while (toIndex<namensarray.length && !found){
               stringElement=(String)namensarray[toIndex];
               if (stringElement.equals(nameToSearch))found = true;
               else  toIndex+=1;
           }
          
           //toIndex = Arrays.binarySearch(namensarray,nameToSearch);
//           pos = nameToSearch.indexOf("hru");
//           if (pos<0)pos=nameToSearch.indexOf("reach");
//           toIndex=Integer.parseInt(nameToSearch.substring(0,pos));
           if (toIndex<namensarray.length) 
               baumarray[i]=toIndex;
           else 
               baumarray[i]=0;
           }
           all+=1;
       }
       System.out.println("all:"+all);
  
           
    }
    
      public void fillArray(String datei, String typ,int arrayStartPos, String[] namensarray,String[] toFlowArray){
        int zeilennummer=0;
        StringBuffer zeile=null;
          try{
          
            int erstezeile=headerLines+1;
            int tabulatorPos = 0;
            FileReader reader = new FileReader(datei);
            int h=0;
           
            int index=arrayStartPos;
            boolean noParaEnd=true;
            String flowInType=typ;
            while(h!=-1){
                h=reader.read(); //erstes Zeichen der Zeile
                zeile=new StringBuffer();
                while(h!=10&&h!=-1){
                    zeile.append((char)h);
                    h=reader.read();
                }//liest eine Zeile ein
                zeilennummer+=1;
                if (zeile.indexOf("#end")>=0) noParaEnd = false; 
                if (zeilennummer>=erstezeile&&noParaEnd){ //nur "relevante" Zeilen werden betrachtet
                    
                    StringTokenizer tokenizer = new StringTokenizer(zeile.toString());
                    //int indexalt=Integer.parseInt(tokenizer.nextToken()); //liest hru/reach-Nummer ein
                    int id=Math.round(Float.parseFloat(tokenizer.nextToken())); //liest hru/reach-Nummer ein
                    
                  //hru-Einträge und reach-Einträge werden fortlaufend durchnummeriert
                    namensarray[index]=(id+typ); // den durchnummerierten Indizes werden die alten Namen zugeordnet
                  
                    String s="";
                    //bestimmt Gebiet, in das das im Index gespeicherte Gebiet abfließt, s enthält nach
                    //dem Durchlauf den Wert (HRU oder Abfluss)
                    if (typ.equals("reach")){
                        tabulatorPos = reachToReachPos;
                    }
                     if (typ.equals("hru")){
                        tabulatorPos = hruToPolyPos;
                    }
                    for (int i=1; i<tabulatorPos; i++){
                        s=tokenizer.nextToken("	 ");
                    }
                    int tohru=Math.round(Float.parseFloat(s));
                    flowInType=typ;
                    if (typ.equals("hru")&&tohru==0){ //wenn hru ind reach abfließt
                        tohru=Math.round(Float.parseFloat(tokenizer.nextToken()));
                        flowInType="reach";
                        //tohru=Integer.parseInt(tokenizer.nextToken());
                        //tohru=tohru/100+anzahl;
                    }
                   
                    toFlowArray[index]=new String(tohru+flowInType);
                    index+=1;               }
            }
            //
        } catch( IOException e ) {
            System.out.println( "Fehler beim Lesen der Datei" );
            
        }
        catch(NumberFormatException nfe){
            System.out.println( "Fehler beim Einlesen der Datei "+datei+". Zeilennummer:"+zeilennummer+", Zeile:"+zeile );
                
            }
    }
}


