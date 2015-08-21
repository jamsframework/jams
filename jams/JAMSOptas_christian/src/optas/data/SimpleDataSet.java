/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

/**
 *
 * @author chris
 */
public class SimpleDataSet extends DataSet {
    public double set;

    public SimpleDataSet(){
        this.parent = null;
        this.set = 0.0;
    }
    public SimpleDataSet(SimpleDataSet set){
        this.set = set.set;
        this.name = set.name;
        this.parent = set.parent;
    }
    public SimpleDataSet(double value, String name, DataSet parent){
        this.set = value;
        this.name = name;
        this.parent = parent;
    }

    public double getValue(){
        return set;
    }
}