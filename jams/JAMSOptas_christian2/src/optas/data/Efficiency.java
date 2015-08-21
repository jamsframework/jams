/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

/**
 *
 * @author chris
 */
public class Efficiency extends StateVariable {
    boolean postiveIsBest;

    public Efficiency(SimpleDataSet set){
        super(set);

        postiveIsBest = true;
    }

    public Efficiency(SimpleDataSet set,boolean pos){
        super(set);

        postiveIsBest = pos;
    }

    public boolean isPostiveBest(){
        return postiveIsBest;
    }
}