/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet;

import java.util.ArrayList;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMDefaultModel {

    public void createDefaultModel();

    public ArrayList getFert();
    public ArrayList getTill();
    public ArrayList getCrop();
    public ArrayList getlmArable();

    public ArrayList getRow();

    public void tellObservers(int i);

    public void SystemPrint();

    public void setPaths(String lmArable,String crop,String till,String fert);

}
