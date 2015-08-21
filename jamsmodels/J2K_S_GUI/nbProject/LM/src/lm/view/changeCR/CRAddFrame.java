/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRAddFrame extends JFrame{
    
    private Panel panel;
    
    public CRAddFrame(){
        this.setResizable(false);
        this.setTitle("CR - Add Frame");
        panel=new Panel();
        this.setContentPane(panel);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        panel.createPanel();
        this.pack();
    }

    public void createAndShowGUI(){
        panel.createPanel();
        this.setVisible(true);
    }

    public void setArableIDList(DefaultListModel defaultlistmodel){
        panel.arableIDList.setModel(defaultlistmodel);
        panel.arableIDList.revalidate();
        panel.arableIDList.repaint();
        panel.arableIDList.setSelectedIndex(0);
    }
    
    public void setCropRotationIDList(DefaultListModel defaultlistmodel){
        panel.cropIDList.setModel(defaultlistmodel);
        panel.cropIDList.revalidate();
        panel.cropIDList.repaint();
        panel.cropIDList.setSelectedIndex(0);
    }

    public void addButtonListener(ActionListener l,String s){
        ActionListener[]a=panel.ok.getActionListeners();
        if(a.length!=0){
            panel.ok.removeActionListener(a[0]);
        }
        panel.ok.addActionListener(l);
        panel.ok.setActionCommand(s);
    }

    public void setArableIDListElement(int i){
        panel.arableIDList.setSelectedIndex(i);
    }
    public void setCropIDListElement(int i){
        panel.cropIDList.setSelectedIndex(i);
    }
    public int getArableIDListElement(){
        String s=(String)panel.arableIDList.getSelectedValue();
        if(s.equals("-")){
            return 0;
        }
        return Integer.parseInt(s.split("\t")[0]);
    }
    public int getCropIDListElement(){
        if(panel.cropIDList.getSelectedValue().equals("-")){
            return 0;
        }
        return (Integer)panel.cropIDList.getSelectedValue();
    }
    public String getMultiplikator(){
        return panel.Multiplikator.getText();
    }


    private class Panel extends JPanel{

        private JList arableIDList=new JList();
        private JList cropIDList=new JList();
        private JTextField Multiplikator=new JTextField();
        private JButton ok=new JButton("ok");

        private Constraints c;

        public Panel(){

        }

        public void createPanel(){
            this.removeAll();
            this.setLayout(new GridBagLayout());
            c=new Constraints(0,0);
            c.anchor=GridBagConstraints.WEST;
            c.fill=GridBagConstraints.BOTH;
            JScrollPane LA=new JScrollPane(arableIDList);
            LA.setPreferredSize(new Dimension(200,250));
            this.add(LA,c);
            c=new Constraints(1,0);
            c.anchor=GridBagConstraints.WEST;
            c.fill=GridBagConstraints.BOTH;
            JScrollPane CI=new JScrollPane(cropIDList);
            CI.setPreferredSize(new Dimension(200,250));
            this.add(CI,c);
            c=new Constraints(0,1);
            c.fill=GridBagConstraints.BOTH;
            this.add(Multiplikator,c);
            c=new Constraints(1,1);
            c.fill=GridBagConstraints.BOTH;
            this.add(ok,c);
        }
    }

}
