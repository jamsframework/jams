package jams.tspaces;

public class Liste<T> {
    private T element;
    private int nummer;
    private Liste next;
    
    public Liste(){
    	this(null, null);
    }
    
    public Liste(T newelement, Liste nextList){
    	element=newelement;
    	next=nextList;    	
    }
    
    public Liste getNext(){
    	return next;
    }
    
    public void setNext(Liste newnext){
    	next=newnext;
    }
    
    public int getNummer(){
    	return nummer;
    }
    
    public void setNummer(int n){
    	nummer=n;
    }
    
    public T getElement(){
    	return element;
    }
    
    public void setElement(T newelement){
    	element=newelement;
    	updateElement((Node)element,0);
    }  
    
    public void updateElement(Node element, int hoehe){
		element.setHoehe(hoehe);
		Node kind=element.getlinkesterSohn();
		while (kind!=null){
			updateElement(kind, hoehe+1);
			kind=kind.getrechterBruder();
		}
    }
}
