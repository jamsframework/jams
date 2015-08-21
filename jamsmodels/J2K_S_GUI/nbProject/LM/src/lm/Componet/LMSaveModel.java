/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet;

import java.util.ArrayList;
import javax.swing.DefaultListModel;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMArableVector;
import lm.model.MultiModel;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMSaveModel {

    public ArrayList getCIDBoxModel();
    public ArrayList getTIDBoxModel();
    public ArrayList getFIDBoxModel();
    public ArrayList getYearBoxModel();
    public ArrayList getDateBoxModel();
    public ArrayList<LMArableVector> getArrayLMArable(int searchRow);
    public LMDefaultModel getDefaultModel();
    public DefaultListModel getTIDListModel();
    public DefaultListModel getFIDListModel();
    public LMArableID getLMArableID(int i);
}
