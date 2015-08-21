/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeLMArable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import lm.view.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import lm.Componet.LMSaveModel;
import lm.Componet.Vector.LMArableVector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainContentChange extends JPanel implements Observer {

        private LMSaveModel saveModel;

        private DefaultListModel defaultListModel;

        private Constraints c;
        private JList lmArableIDList=new JList();
        private JButton addID=new JButton("Add");
        private JButton deleteID=new JButton("Delete");

        private JButton Cancel=new JButton("Cancel");
        private JButton Save = new JButton("Save");

        private lmArableHead lmArableHead=new lmArableHead();
        private lmArablePanel lmArablePanel=new lmArablePanel();
        private lmArable_fact_head lmArable_fact_head = new lmArable_fact_head();

        public MainContentChange(LMSaveModel saveModel){
            this.saveModel=saveModel;
        }

        public void createGUI(){
            this.setLayout(new GridBagLayout());

        c=new Constraints(0,0,2,2);
        c.fill=GridBagConstraints.BOTH;
        JScrollPane LA=new JScrollPane(getLmArableIDList());
        LA.setPreferredSize(new Dimension(200,475));
        this.add(LA,c);

        c=new Constraints(0,2,1,1);
        c.anchor= GridBagConstraints.EAST;
        addID.setPreferredSize(deleteID.getPreferredSize());
        this.add(addID,c);

        c=new Constraints(1,2,1,1);
        c.anchor= GridBagConstraints.WEST;
        this.add(deleteID,c);

        c=new Constraints(2,0,1,1);
        this.lmArableHead.createAndReBuildButton();
        this.lmArableHead.setPreferredSize(new Dimension (500,100));
        this.add(lmArableHead,c);
        
        c=new Constraints(3,0,2,1);
        c.fill=GridBagConstraints.BOTH;
        this.add(getLmArable_fact_head(),c);

        c=new Constraints(2,1,3,1);
        JScrollPane LP=new JScrollPane(lmArablePanel);
        LP.setPreferredSize(new Dimension(1000,400));
        this.add(LP,c);


        c=new Constraints(3,2,1,1,1,0);
        c.anchor=GridBagConstraints.EAST;
        Save.setPreferredSize(Cancel.getPreferredSize());
        this.add(Save,c);

        c=new Constraints(4,2,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(Cancel,c);
        }

        public void setContent(Integer i){
            lmArableHead.setColor(saveModel.getDefaultModel().getLMArable().get(i).getColor());
            lmArableHead.setLMArableID(saveModel.getDefaultModel().getLMArable().get(i));
            getLmArable_fact_head().setLMArableID(saveModel.getDefaultModel().getLMArable().get(i));
            getLmArable_fact_head().createGUI();
            setlmArablePanel(saveModel.getArrayLMArable(i),saveModel.getTIDBoxModel(),saveModel.getFIDBoxModel(),saveModel.getYearBoxModel(),saveModel.getDateBoxModel());
        }


        public void setSelectedListElement(int i){
            this.getLmArableIDList().setSelectedIndex(i);
        }

        public void setlmArablePanel(ArrayList<LMArableVector> a,ArrayList TID,ArrayList FID,ArrayList<String>year,ArrayList<String>date){
            this.getLmArablePanel().set(a,TID,FID,year,date);
            this.repaint();
        }

        public int getlmArableIDListPosition(){
            System.out.println(this.getLmArableIDList().getSelectedIndex());
            System.out.println(this.getLmArableIDList().getSelectedValue().toString());
            return Integer.parseInt(this.getLmArableIDList().getSelectedValue().toString().split("<")[0]);
        }

        public void Reselect(){
            int i =getLmArableIDList().getSelectedIndex();
            getLmArableIDList().clearSelection();
            getLmArableIDList().setSelectedIndex(i);
        }


        public void addLmArableIDListListener(ListSelectionListener l){
            getLmArableIDList().addListSelectionListener(l);
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

    public void setDefaultListModel(DefaultListModel dlm){
        this.defaultListModel=dlm;
        getLmArableIDList().setModel(defaultListModel);
    }

    public void update(Observable o, Object o1) {
        if(o1 instanceof Integer){
            
            getLmArableIDList().setSelectedIndex(1);
        }else{
            Reselect();
            setContent(getLmArableIDList().getSelectedIndex()+1);
        }
    }

    /**
     * @return the lmArableIDList
     */
    public JList getLmArableIDList() {
        return lmArableIDList;
    }

    /**
     * @return the lmArable_fact_head
     */
    public lmArable_fact_head getLmArable_fact_head() {
        return lmArable_fact_head;
    }

    /**
     * @param lmArable_fact_head the lmArable_fact_head to set
     */
    public void setLmArable_fact_head(lmArable_fact_head lmArable_fact_head) {
        this.lmArable_fact_head = lmArable_fact_head;
    }

        

}
