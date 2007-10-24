package jams.tree;
import java.io.*;
import java.util.*;

public class baumpart {
	
	static String hrudatei="hrus_new.par";
	static String reachdatei="reach.par";
	static int maxsize=5;

	public static void main(String[] args) {
		
		//bestimmt die Anzahl der Gebiete in den einzelnen Datein
		int hruanzahl=anz(hrudatei);
		int reachanzahl=anz(reachdatei);
		
		//legt das Array, in dem die Verknüpfungen zwischen Kindern und Vätern stehen sollen, an
		int[] baumarray=new int[hruanzahl+reachanzahl+1];
		//legt das Array, in dem die ursprüngliche Bezeichnung jedes Index steht, an
		String[] namensarray=new String[hruanzahl+reachanzahl+1];
		
		//liest die Kind/Vater-Beziehungen der hru- bzw. der reach-Datei ein. Diese stehen dann im baumarray.
		barray(hrudatei, "hru", hruanzahl, baumarray, namensarray);
		barray(reachdatei, "reach", reachanzahl, baumarray, namensarray);
		
		//legt den Baum aus dem baumarray an. Der Elementeintrag entspricht dem gespeicherten Namen im namensarray
		Node wurzel=baum(baumarray, namensarray);
		//gibt den gespeicherten Baum aus
		baumausgabe(wurzel);System.out.println();
		
		//legt eine Liste von Bäumen an, die dann nacheinander parallel bearbeitet werden können
		Liste partitioningtree = partitioning(wurzel);
		listenausgabe(partitioningtree);
	}
	
	public static Liste<Node> partitioning(Node wurzel){
		return partitioning(wurzel, 1);
	}
	
	public static Liste<Node> partitioning(Node wurzel, int n){
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
	
	public static void baumumsortierung(Node wurzel){
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
				}
				else{
					kindarray[i].setrechterBruder(null);
				}
				baumumsortierung(kindarray[i]);
			}
		}
	}
	
	public static void listenausgabe(Liste<Node> wurzel){
		System.out.println("baum: " + wurzel.getNummer());
		baumausgabe(wurzel.getElement()); System.out.println();
		if(wurzel.getNext()!=null){
			listenausgabe(wurzel.getNext());
		}	
	}	
	
	public static void baumausgabe(Node wurzel){
		System.out.println("hoehe: " + wurzel.getHoehe() + ": " + wurzel.getElement()+ " Vaterknoten: " + wurzel.getVater().getElement() +" Size: "+ wurzel.getSize()+ " Degree: "+ wurzel.getDegree());
		Node naechsterSohn=wurzel.getlinkesterSohn();
		while (naechsterSohn!=null){
			baumausgabe(naechsterSohn);
			naechsterSohn=naechsterSohn.getrechterBruder();
		}
	}
	
	public static Node baum(int[] barray, String[] narray){
		return baum(barray, narray, new Node<String>(0,"0"));
	}
	
	public static Node baum(int[] barray, String[] narray, Node<String> node){//gibt jeweils den linkesten Teilbaum des Wurzelknoten node zurück
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
	
	public static Node[] findChildren(int[] b, String n[], int index){
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
	
	public static int anz(String datei){
		int anzahl=0;
		int h=0;
		try{
			//bestimmt die hru/reach-Zahlen;
			Reader reader = new FileReader(datei);
			while(h!=-1){
				h=reader.read(); //liest alle Zeichen nacheinander ein
				if (h==10) {
					anzahl+=1; //bei Zeilenumbruch wird die hru/rech-Zahl um eins erhöht
				}
			}
			anzahl-=5; //zieht die ersten "unwichtigen" Zeilen ab 
		}
		catch(IOException e) {
			System.out.println( "Fehler beim Lesen der Datei" );
		}
		return anzahl;
	}
	
	public static void barray(String datei, String typ, int anzahl, int[] baumarray, String[] namensarray){
		try{
			int pos=0;
			int erstezeile=6;
			if (typ.equals("hru")){
				pos=7; //in der hru-Datei wird die 7. Spalte abgelesen
			}
			else if (typ.equals("reach")){
				pos=3; //in der reach-Datei wird die 3. Spalte abgelesen
			}
			FileReader reader = new FileReader(datei);
			int h=0;
			int zeilennummer=0;
			while(h!=-1){
		    	h=reader.read(); //erstes Zeichen der Zeile
		    	StringBuffer zeile=new StringBuffer();
		    	while(h!=10&&h!=-1){
		    		zeile.append((char)h);
		        	h=reader.read();
		    	}//liest eine Zeile ein 
		    	zeilennummer+=1;
		    	if (zeilennummer>erstezeile-1&&zeilennummer<anzahl+erstezeile){ //nur "relevante" Zeilen werden betrachtet
			    	StringTokenizer tokenizer = new StringTokenizer(zeile.toString());
			    	int indexalt=Integer.parseInt(tokenizer.nextToken()); //liest hru/reach-Nummer ein
			    	int index=indexalt;
			    	if (typ.equals("reach")){
			    		index=indexalt/100+ baumarray.length-anzahl-1; //hru-Einträge und reach-Einträge werden fortlaufend durchnummeriert 
			    	}
			    	namensarray[index]=(indexalt+typ); // den durchnummerierten Indizes werden die alten Namen zugeordnet
			    		
			    	String s="";
			    	for (int i=1; i<pos-1; i++){
			    		s=tokenizer.nextToken("	 "); //bestimmt Gebiet, in das das im Index gespeicherte Gebiet abfließt		
			    	}
			    	int tohru=Integer.parseInt(tokenizer.nextToken());
			    	if (typ.equals("hru")&&tohru==0){ //wenn hru ind reach abfließt		    		
			    			tohru=Integer.parseInt(tokenizer.nextToken());
			    			tohru=tohru/100+anzahl;
			    	}
			    	if (typ.equals("reach")&&tohru!=0){
			    		tohru=tohru/100+baumarray.length-anzahl-1;
			    	}
			    	baumarray[index]=tohru; //speichert die Beziehung zwischen einem Gebiet (index) und dem Gebiet, in welches es abfließt (baumarray[index])
		    	}
			}
		}
		catch( IOException e ) {
			System.out.println( "Fehler beim Lesen der Datei" );		
		}
	}
}
