/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.juice.optimizer.wizard;

import javax.swing.JPanel;

/**
 *
 * @author Christian Fischer
 */
public abstract class stepPane {
    final protected JPanel panel = new JPanel();
    abstract public JPanel build();
    public JPanel getPanel(){
        return panel;
    }
    public String init(){
        
        return null;
    }
    
    public String finish(){
        return null;
    }
}
