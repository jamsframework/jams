/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;

/**
 *
 * @author christian
 */
abstract public class AbstractDataTab extends JPanel{

    boolean isChanged = false;
    String name;
    
    public AbstractDataTab(String name) {
        this.name = name;
        setChanged();
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }
        });
    }
    
    public String getName(){
        return name;
    }

    public void setChanged(){
        isChanged = true;
        refresh();
    }
    public boolean isChanged(){
        return isChanged;
    }
    
    
    abstract public void refresh();
}
