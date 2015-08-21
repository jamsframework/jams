package org.unijena.abc;

import java.util.Comparator;
import java.util.Vector;


public class ABCEvoluAlgComparator implements Comparator {

    public ABCEvoluAlgComparator() {}

    public int compare(Object d1, Object d2) {

        Vector b1 = (Vector)d1;
        Vector b2 = (Vector)d2;
        
	return Double.compare((Double)b1.lastElement(),(Double)b2.lastElement());        
    }
} 
