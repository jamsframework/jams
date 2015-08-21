/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRHead extends JPanel {

    private JComboBox zoomBox=new JComboBox();
    private JButton addNewCropRID =new JButton("Add a Row");
    private Constraints c;

    public CRHead(){
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public void createAndShowGUI(){
        this.setLayout(new GridBagLayout());
        c=new Constraints(0,0,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(addNewCropRID,c);
        c=new Constraints(1,0,1,1);
        this.add(new JLabel("Zoom"),c);
        c=new Constraints(2,0,1,1);
        this.add(zoomBox,c);
        this.repaint();

    }
    public void addZoomBoxModel(DefaultComboBoxModel model){
        this.zoomBox.setModel(model);
        this.zoomBox.setSelectedIndex(0);
    }
    public void addZoomListener(ActionListener l){
        this.zoomBox.addActionListener(l);
    }
    public int getZoomBoxPosition(){
        return zoomBox.getSelectedIndex();
    }

    public void addActionListenerToNewRow(ActionListener l){
        addNewCropRID.addActionListener(l);
    }

}
