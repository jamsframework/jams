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
public class PositiveEfficiencyEnsemble extends DefaultEfficiencyEnsemble implements EfficiencyEnsemble{
    public PositiveEfficiencyEnsemble(PositiveEfficiencyEnsemble dataset){
        super(dataset);
        this.isPostiveBest = true;
    }
    
    public PositiveEfficiencyEnsemble(String name, DataSetContainer parent, DataView<Integer> ids, DataView<Double> values){
        super(name, parent, ids, values);
        this.isPostiveBest = true;
    }
}
