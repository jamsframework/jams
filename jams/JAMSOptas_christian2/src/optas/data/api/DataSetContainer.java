/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.api;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <K>
 * @param <T>
 */
public interface DataSetContainer<K, T extends DataSet> extends DataSet{
    public DataSetContainer<K, T> addDataSet(T set);
    public DataSetContainer<K, T> removeDataSet(String name);
    
    public DataView<? extends T> getDataSets();
    public T getDataSet(K key);
    public DataView<? extends DataSet> getDataSets(Class clazz);
}
