/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data.ensemble;

import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.ensemble.api.EfficiencyEnsemble;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class NegativeEfficiencyEnsemble extends DefaultEfficiencyEnsemble implements EfficiencyEnsemble{
    public NegativeEfficiencyEnsemble(NegativeEfficiencyEnsemble dataset){
        super(dataset);
        this.isPostiveBest = false;
    }
    
    public NegativeEfficiencyEnsemble(String name, DataSetContainer parent, DataView<Integer> ids, DataView<Double> values){
        super(name, parent, ids, values);
        this.isPostiveBest = false;
    }
}