package lm.view.change;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import lm.Componet.Vector.*;
import javax.swing.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class lmArableViewVector {

    private JLabel ID;

    private JTextField DateField;

    private JComboBox TID;
    private String TIDSet;
    private JButton  TIDEdit=new JButton("Edit");

    private JComboBox FID;
    private String FIDSet;
    private JButton FIDEdit=new JButton("Edit");

    private JTextField FAMountField;

    private JCheckBox PLANTBOX;

    private JCheckBox HARVESTBOX;

    private JTextField FRACHARVField;
    private JButton delete=new JButton(new ImageIcon("src/lm/resources/images/Minus.gif"));

    private DefaultComboBoxModel TIDModel;

    private DefaultComboBoxModel FIDModel;

    public lmArableViewVector (LMArableVector LMArableVector, ArrayList<String> TID,ArrayList<String> FID){

        TIDModel=new DefaultComboBoxModel();
        for(String s : TID){
            TIDModel.addElement(s);
        }
        FIDModel=new DefaultComboBoxModel();
        for(String s : FID){
            FIDModel.addElement(s);
        }


        ID=new JLabel(String.valueOf(LMArableVector.getAID().getStufe()));
        ID.setPreferredSize(new Dimension(20,20));

        DateField=new JTextField(LMArableVector.getDate());
        DateField.setPreferredSize(new Dimension(50,20));



        this.TID=new JComboBox(TIDModel);
        if(LMArableVector.getTID().getTID()!=0){
            this.TID.setSelectedItem(LMArableVector.getTID().getTID()+"");
        }else{
            this.TID.setSelectedIndex(0);
        }

        
        this.TID.setPreferredSize(new Dimension(50,20));

        this.FID=new JComboBox(FIDModel);
        if(LMArableVector.getFID().getID()!=0){
            this.FID.setSelectedItem(LMArableVector.getFID().getID()+"");
        }else{
            this.FID.setSelectedIndex(0);
        }

        this.FID.setPreferredSize(new Dimension(50,20));

        if(!String.valueOf(LMArableVector.getFAmount()).equals("null")){
            FAMountField=new JTextField(String.valueOf(LMArableVector.getFAmount()));
        }else{
            FAMountField=new JTextField("");
        }

        FAMountField.setPreferredSize(new Dimension (70,20));

        PLANTBOX=new JCheckBox("",LMArableVector.getPLANT());

        HARVESTBOX= new JCheckBox("",LMArableVector.getHARVEST());

        if(!String.valueOf(LMArableVector.getFRACHARV()).equals("null")){
            FRACHARVField=new JTextField(String.valueOf(LMArableVector.getFRACHARV()));
        }else{
            FRACHARVField=new JTextField("");
        }

        FRACHARVField.setPreferredSize(new Dimension(40,20));

       delete.setPreferredSize(new Dimension(20,20));
    }

    public void addTIDEditListener(ActionListener l){
        getTIDEdit().addActionListener(l);
    }
    public void addFIDEditListener(ActionListener l){
        getFIDEdit().addActionListener(l);
    }

    public void addDeleteListener(ActionListener l){
        getDelete().addActionListener(l);
    }

    /**
     * @return the ID
     */
    public JLabel getID() {
        return ID;
    }

    /**
     * @return the DateField
     */
    public JTextField getDateField() {
        return DateField;
    }

    /**
     * @return the TID
     */
    public JComboBox getTID() {
        return TID;
    }

    /**
     * @return the TIDEdit
     */
    public JButton getTIDEdit() {
        return TIDEdit;
    }

    /**
     * @return the FID
     */
    public JComboBox getFID() {
        return FID;
    }

    /**
     * @return the FIDEdit
     */
    public JButton getFIDEdit() {
        return FIDEdit;
    }

    /**
     * @return the FAMountField
     */
    public JTextField getFAMountField() {
        return FAMountField;
    }

    /**
     * @return the PLANTBOX
     */
    public JCheckBox getPLANTBOX() {
        return PLANTBOX;
    }

    /**
     * @return the HARVESTBOX
     */
    public JCheckBox getHARVESTBOX() {
        return HARVESTBOX;
    }

    /**
     * @return the FRACHARVField
     */
    public JTextField getFRACHARVField() {
        return FRACHARVField;
    }

    /**
     * @return the TIDSet
     */
    public String getTIDSet() {
        return TIDSet;
    }

    /**
     * @return the FIDSet
     */
    public String getFIDSet() {
        return FIDSet;
    }

    /**
     * @return the TIDModel
     */
    public DefaultComboBoxModel getTIDModel() {
        return TIDModel;
    }

    /**
     * @return the FIDModel
     */
    public DefaultComboBoxModel getFIDModel() {
        return FIDModel;
    }

    /**
     * @return the delete
     */
    public JButton getDelete() {
        return delete;
    }


}
