package jams.tree;
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
}
