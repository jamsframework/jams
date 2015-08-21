/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import optas.data.api.DataCollection;
import optas.data.api.DataView;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public interface DataCollectionImporter<T> {
   
    enum SimpleValueType{Parameter, StateVariable, NegEfficiency, PosEfficiency, Unknown, Ignore};
    enum TimeSerieType{Measurement, Simulation, Unknown, Ignore};
    enum SpatialType{Measurement, Simulation, Unknown, Ignore};
    enum SpatioTemporalType{Measurement, Simulation, Unknown, Ignore};
    
    public DataView<String> getDataSetNames();

    public T getDataSetType(String name);
    
    public void setDataSetType(String name, T type);
    
    DataCollection importData();
}