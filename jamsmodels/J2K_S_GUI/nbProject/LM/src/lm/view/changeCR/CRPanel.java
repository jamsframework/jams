/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.TreeMap;
import javax.swing.*;
import lm.Componet.Vector.LMCropRotationElement;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRPanel extends JPanel {

    private TreeMap<Integer,CRRowPanel> treeMap;
    private Constraints c;

    public CRPanel(){
        //this.setPreferredSize(new Dimension(1000,500));
        treeMap=new TreeMap();
    }

    public void addRow(Integer id){
        treeMap.put(id, new CRRowPanel(id));
    }

    public void addElementsOfARow(Integer key ,LMCropRotationElement cropRotationElement, Color color,int zoom){
        treeMap.get(key).addButton(cropRotationElement,color,zoom);
    }
    public void setLast(Integer key){
        treeMap.get(key).addButton(key);
    }
    public void clearRowList(){
        treeMap.clear();
    }


    public void repaintAll(){
        this.revalidate();
        this.repaint();
    }

    public void createAndShowGUI(){
        this.setLayout(new GridBagLayout());
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            int i=0;
            while(key!=null){
                treeMap.get(key).buttonsToView();
                c=new Constraints(0,i);
                c.anchor=GridBagConstraints.WEST;
                this.add(treeMap.get(key),c);
                key=treeMap.higherKey(key);
                i++;
            }
        }
        this.repaintAll();
    }

    public void addDetailListener(ActionListener l){
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                treeMap.get(key).addDetailListener(l);
                key=treeMap.higherKey(key);
            }
        }
    }
    public void addCropIDListener(ActionListener l){
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                treeMap.get(key).addCropIDListener(l);
                key=treeMap.higherKey(key);
            }
        }
    }
    
}
