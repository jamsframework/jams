package jams.tspaces;

public class Node<T>{
	
  // Instance variables:
  private T element;
  private Integer hoehe;
  private Integer size;
  private Integer degree;
  private Integer index;
  private Node vater;
  private Node linkesterSohn;
  private Node rechterBruder;
  public int xkoordinate;
  public int ykoordinate;
  
  // Simple constructors: 
  public Node() {
    element=null;
    index=0;
    linkesterSohn=null;
    rechterBruder=null;
  }
  
  public Node(Integer i, T e) {
	    element = e;
	    index=i;
	  }
  
   public Node(T e, Node s) {
    element = e;
    linkesterSohn = s;
  }
   
  Object getElement() {
    return element; 
  }
  
  void setElement(T newElem) { 
    element = newElem; 
  }
  
  Integer getIndex() {
    return index; 
  }
	  
  void setIndex(Integer newIndex) { 
    index = newIndex; 
  }

  Node getlinkesterSohn() { 
	    return linkesterSohn;
	  }
  
  void setlinkesterSohn(Node newlinkesterSohn) {
    linkesterSohn = newlinkesterSohn; 
  }
  
  Node getVater() { 
    return vater;
  }

  void setVater(Node newVater) {
    vater = newVater; 
  }

  Node getrechterBruder() {
	    return rechterBruder; 
  }
  
  void setrechterBruder(Node newrechterBruder) {
	    rechterBruder = newrechterBruder; 
	  }
  
  Integer getSize() {
		    return size; 
	}

  void setSize(Integer newSize) {
		    size= newSize; 
	}
  
  Integer getDegree() {
	    return degree; 
  }

  void setDegree(Integer newDegree) {
	    degree= newDegree; 
  }
  
  Integer getHoehe() {
	    return hoehe; 
  }

  void setHoehe(Integer newHoehe) {
	    hoehe= newHoehe;	    	   
}
  
  Node copyTree(Node vater) {
	  Node aktuell=new Node<T>();
	  aktuell.element=this.element;
	  aktuell.hoehe=this.hoehe;
	  aktuell.size=this.size;
	  aktuell.degree=this.degree;
	  aktuell.index=this.index;
	  aktuell.vater=vater;	  
	  aktuell.xkoordinate=this.xkoordinate;
	  aktuell.ykoordinate=this.ykoordinate;
	  if (this.linkesterSohn!=null) {aktuell.linkesterSohn=this.linkesterSohn.copyTree(aktuell);}
	  else {aktuell.linkesterSohn=null;}
	  if (this.rechterBruder!=null){aktuell.rechterBruder=this.rechterBruder.copyTree(vater);}
	  else {aktuell.rechterBruder=null;}
	  return aktuell;
  }
}