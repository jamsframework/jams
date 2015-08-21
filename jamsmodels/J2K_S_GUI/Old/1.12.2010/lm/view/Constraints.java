/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view;

import java.awt.GridBagConstraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Constraints extends GridBagConstraints {


    public Constraints(int gridx,int gridy){
        this.gridx=gridx;
        this.gridy=gridy;

    }

    public Constraints(int gridx,int gridy,int gridwidht,int gridheight){
        this.gridx=gridx;
        this.gridy=gridy;
        this.gridwidth=gridwidht;
        this.gridheight=gridheight;
    }
    public Constraints(int gridx,int gridy,int gridwidht,int gridheight,int weightx,int weighty){
        this.gridx=gridx;
        this.gridy=gridy;
        this.gridwidth=gridwidht;
        this.gridheight=gridheight;
        this.weightx=weightx;
        this.weighty=weighty;
    }

}
