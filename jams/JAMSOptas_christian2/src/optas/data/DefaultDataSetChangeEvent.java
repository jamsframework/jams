/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import optas.data.api.*;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class DefaultDataSetChangeEvent implements DataSetChangeEvent{

    DataSet source;

    public DefaultDataSetChangeEvent(DataSet source) {
       this.source = source;
    }
    
    public DataSet getSource(){
        return source;
    }
}
