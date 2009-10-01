package jams.tspaces;

public class NodeEntity<T,T2> extends Node<T>{
	  private  T2 element2;	  
	  private NodeEntity vater;
	  private NodeEntity linkesterSohn;
	  private NodeEntity rechterBruder;
	  
	  // Simple constructors: 
	  public NodeEntity() {
		    super.setElement(null);
		    element2=null;
		    super.setIndex(0);
		    linkesterSohn=null;
		    rechterBruder=null;
		  }
		  
		  public NodeEntity(Integer i, T e1, T2 e2) {
			  super.setElement(e1);
			  super.setIndex(i);
			    element2 = e2;
			  }
		  
		   public NodeEntity(T e1, T2 e2, NodeEntity s) {
			   super.setElement(e1);
			    element2 = e2;
		    linkesterSohn = s;
		  }
	  
	  void setElement2(T2 newElem2) { 
	    element2 = newElem2;
	  }
	  
	  T2 getElement2() { 
		    return element2;
		  }
	  
	  Object getElement1() {
		    return super.getElement(); 
		  }
		  
		  void setElement(T newElem1, T2 newElem2) { 
			super.setElement(newElem1);
		    element2 = newElem2;
		  }
		  		  

		  NodeEntity getlinkesterSohn() { 
			    return linkesterSohn;
			  }
		  
		  void setlinkesterSohn(NodeEntity newlinkesterSohn) {
		    linkesterSohn = newlinkesterSohn; 
		  }
		  
		  NodeEntity getVater() { 
		    return vater;
		  }

		  void setVater(NodeEntity newVater) {
		    vater = newVater; 
		  }

		  NodeEntity getrechterBruder() {
			    return rechterBruder; 
		  }
		  
		  void setrechterBruder(NodeEntity newrechterBruder) {
			    rechterBruder = newrechterBruder; 
			  }
		  
		  NodeEntity copyTree(NodeEntity vater) {
			  NodeEntity aktuell=new NodeEntity<T,T2>();
			  aktuell.setElement(super.getElement());
			  aktuell.element2=this.element2;
			  aktuell.setHoehe(super.getHoehe());
			  aktuell.setSize(super.getSize());
			  aktuell.setDegree(super.getDegree());
			  aktuell.setIndex(super.getIndex());
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