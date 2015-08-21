/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Constraints extends GridBagConstraints {

    public Constraints(){
        this.insets=new Insets(2,2,2,2);
    }


    public Constraints(int gridx,int gridy){
        this.gridx=gridx;
        this.gridy=gridy;
        this.insets=new Insets(2,2,2,2);

    }

    public Constraints(int gridx,int gridy,int gridwidht,int gridheight){
        this.gridx=gridx;
        this.gridy=gridy;
        this.gridwidth=gridwidht;
        this.gridheight=gridheight;
        this.insets= new Insets(2,2,2,2);
    }
    public Constraints(int gridx,int gridy,int gridwidht,int gridheight,double weightx,double weighty){
        this.gridx=gridx;
        this.gridy=gridy;
        this.gridwidth=gridwidht;
        this.gridheight=gridheight;
        this.weightx=weightx;
        this.weighty=weighty;
        this.insets= new Insets(2,2,2,2);
    }

    public void deleteInsets(){
        this.insets=new Insets(0,0,0,0);
    }

}
