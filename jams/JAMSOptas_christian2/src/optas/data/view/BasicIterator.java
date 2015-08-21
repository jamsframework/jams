/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import java.util.Iterator;
import optas.data.api.DataView;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class BasicIterator<T> implements Iterator<T> {

        int pos = -1;

        DataView<T> supplier = null;
        
        BasicIterator(DataView<T> supplier){
            this.supplier = supplier;
        }
        
        @Override
        public boolean hasNext() {
            return pos < supplier.getSize()-1;
        }

        @Override
        public T next() {
            return supplier.getValue(++pos);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
