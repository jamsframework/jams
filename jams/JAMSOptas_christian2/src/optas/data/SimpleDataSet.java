/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

/**
 *
 * @author chris
 */
public class SimpleDataSet extends DefaultDataSet {
    final private double value;

    public SimpleDataSet(){
        super("SimpleDataSet");       
        this.value = 0.0;
    }
    public SimpleDataSet(SimpleDataSet set){
        super(set);
        this.value = set.value;
    }
    public SimpleDataSet(String name, double value){
        super(name);
        this.value = value;
    }

    public double getValue(){
        return value;
    }
}