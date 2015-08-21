/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.change;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import lm.view.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import lm.Componet.Vector.LMArableVector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainContentChange extends JPanel {

        private Constraints c;
        private JList lmArableIDList=new JList();
        private JButton addID=new JButton("Add");
        private JButton deleteID=new JButton("Delete");

        private JButton Cancel=new JButton("Cancel");
        private JButton Save = new JButton("Save");

        private lmArableHead lmArableHead=new lmArableHead();
        private lmArablePanel lmArablePanel=new lmArablePanel();

        public MainContentChange(){
            createGUI();
        }

        public void createGUI(){
            this.setLayout(new GridBagLayout());

        c=new Constraints(0,0,2,2);
        c.fill=GridBagConstraints.BOTH;
        JScrollPane LA=new JScrollPane(lmArableIDList);
        LA.setPreferredSize(new Dimension(200,475));
        this.add(LA,c);

        c=new Constraints(0,2,1,1);
        c.anchor= GridBagConstraints.EAST;
        addID.setPreferredSize(deleteID.getPreferredSize());
        this.add(addID,c);

        c=new Constraints(1,2,1,1);
        c.anchor= GridBagConstraints.WEST;
        this.add(deleteID,c);

        c=new Constraints(2,0,3,1);
        this.lmArableHead.setPreferredSize(new Dimension (500,100));
        this.add(lmArableHead,c);

        c=new Constraints(2,1,3,1);
        JScrollPane LP=new JScrollPane(lmArablePanel);
        LP.setPreferredSize(new Dimension(700,400));
        this.add(LP,c);


        c=new Constraints(3,2,1,1,1,0);
        c.anchor=GridBagConstraints.EAST;
        Save.setPreferredSize(Cancel.getPreferredSize());
        this.add(Save,c);

        c=new Constraints(4,2,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(Cancel,c);
        }

        public void setlmArableHead (){
            this.lmArableHead.set((String)this.lmArableIDList.getSelectedValue());
            this.lmArableHead.repaint();

        }

        public void setSelectedListElement(int i){
            this.lmArableIDList.setSelectedIndex(i);
        }

        public void setlmArablePanel(ArrayList<LMArableVector> a,ArrayList TID,ArrayList FID){
            this.getLmArablePanel().set(a,TID,FID);
            this.repaint();
        }

        public void setlmArableIDList(DefaultListModel defaultlistmodel){
        lmArableIDList.setModel(defaultlistmodel);
        lmArableIDList.revalidate();
        lmArableIDList.repaint();
        lmArableIDList.setSelectedIndex(0);
    }
        public int getlmArableIDListPosition(){
            return this.lmArableIDList.getSelectedIndex();
        }



        public void addLmArableIDListListener(ListSelectionListener l){
            lmArableIDList.addListSelectionListener(l);
        }

        public void addAddIDListener(ActionListener l){
            addID.addActionListener(l);
        }
        public void addDeleteIDListener(ActionListener l){
            deleteID.addActionListener(l);
        }
        public void addSaveListener(ActionListener l){
            Save.addActionListener(l);
        }
        public void addCancelListener(ActionListener l){
            Cancel.addActionListener(l);
        }

    /**
     * @return the lmArablePanel
     */
    public lmArablePanel getLmArablePanel() {
        return lmArablePanel;
    }

    public lmArableHead getLmArableHead(){
        return lmArableHead;
    }

        

}
