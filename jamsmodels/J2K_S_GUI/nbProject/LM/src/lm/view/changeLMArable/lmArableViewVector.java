package lm.view.changeLMArable;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import lm.Componet.Vector.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class lmArableViewVector {

    private JLabel ID;

    private JComboBox dateBox;

    private JComboBox yearBox;

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

    private DefaultComboBoxModel yearBoxModel;
    private DefaultComboBoxModel dateBoxModel;

    private DefaultComboBoxModel TIDModel;

    private DefaultComboBoxModel FIDModel;

    public lmArableViewVector (int integer,LMArableVector LMArableVector, ArrayList<String> TID,ArrayList<String> FID,ArrayList<String>year,ArrayList<String>date){

        TIDModel=new DefaultComboBoxModel();
        for(String s : TID){
            TIDModel.addElement(s);
        }
        FIDModel=new DefaultComboBoxModel();
        for(String s : FID){
            FIDModel.addElement(s);
        }
        yearBoxModel=new DefaultComboBoxModel();
        for(String s : year){
            yearBoxModel.addElement(s);
        }
        dateBoxModel=new DefaultComboBoxModel();
        for(String s : date){
            dateBoxModel.addElement(s);
        }
        this.dateBox=new JComboBox(dateBoxModel);
        this.yearBox=new JComboBox(yearBoxModel);
        this.FID=new JComboBox(FIDModel);
        this.TID=new JComboBox(TIDModel);

        ID=new JLabel(String.valueOf(integer));
        ID.setPreferredSize(new Dimension(20,20));

        dateBox.setSelectedIndex(LMArableVector.getDate()-1);

       // DateField.setPreferredSize(new Dimension(50,20));

        yearBox.setSelectedIndex(LMArableVector.getYear()-1);

        if(LMArableVector.getTID()!=0){
            for(int i=1;i<TID.size();i++){
                String s=((String)TID.get(i)).split("\n")[0];
                if(LMArableVector.getTID()==Integer.valueOf(s)){
                    this.TID.setSelectedIndex(i);
                }
                this.FID.setEnabled(false);
            }
        }else{
            this.TID.setSelectedIndex(0);
        }

        
        this.TID.setPreferredSize(new Dimension(150,20));

        
        if(LMArableVector.getFID()!=0){
            for(int i=1;i<FID.size();i++){
                String s=((String)FID.get(i)).split("\n")[0];
                if(LMArableVector.getFID()==Integer.valueOf(s)){
                    this.FID.setSelectedIndex(i);
                }
                this.TID.setEnabled(false);
            }
        }else{
            this.FID.setSelectedIndex(0);
        }

        this.FID.setPreferredSize(new Dimension(150,20));

        if(!String.valueOf(LMArableVector.getFAmount()).equals("null")){
            FAMountField=new JTextField(String.valueOf(LMArableVector.getFAmount()));
        }else{
            FAMountField=new JTextField("");
        }

        FAMountField.setPreferredSize(new Dimension (70,20));

        PLANTBOX=new JCheckBox("",LMArableVector.getPLANT());

        HARVESTBOX= new JCheckBox("",LMArableVector.getHARVEST());

        if(PLANTBOX.isSelected()){
            HARVESTBOX.setEnabled(false);
        }
        if(HARVESTBOX.isSelected()){
            PLANTBOX.setEnabled(false);
        }
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
    public void addDateFieldActionListener(ActionListener l){
        dateBox.addActionListener(l);
    }
    public void addYearActionListener(ActionListener l){
        yearBox.addActionListener(l);
    }

    public void addTIDListener(ActionListener l){
        getTID().addActionListener(l);
    }

    public void addFIDListener(ActionListener l){
        getFID().addActionListener(l);
    }

    public void addFAMountFieldKeyListener(KeyListener kl){
        FAMountField.addKeyListener(kl);
    }
    public void addFAMountFieldActionListener(ActionListener l){
        FAMountField.addActionListener(l);
    }
    public void addPLANTBOXActionListener(ActionListener l){
        PLANTBOX.addActionListener(l);
    }

    public void addHARVESTBOXActionListener (ActionListener l){
        HARVESTBOX.addActionListener(l);
    }

    public void addFRACHARVFieldKeyListener(KeyListener kl){
        FRACHARVField.addKeyListener(kl);
    }
    public void addFRACHARVFieldActionListener(ActionListener l){
        FRACHARVField.addActionListener(l);
    }

    public Boolean isPLANT(){
        return PLANTBOX.isSelected();
    }
    public Boolean isHARVEST(){
        return HARVESTBOX.isSelected();
    }

    public Integer getYear(){
        return Integer.valueOf((String)yearBox.getSelectedItem());
    }

    public Integer getTIDContent(){
        String s=((String)getTID().getSelectedItem()).split("\n")[0];
        if(s.equals("-")){
            return null;
        }
        return Integer.valueOf(s);
    }

    public Integer getFIDContent(){
        String s=((String)getFID().getSelectedItem()).split("\n")[0];
        if (s.equals("-")){
            return null;
        }
        return Integer.valueOf(s);
    }
    public void setFIDVisible(Boolean b){
        FID.setEnabled(b);
    }
    public void setTIDVisible(Boolean b){
        TID.setEnabled(b);
    }
    public void setPLANTVisible(Boolean b){
        PLANTBOX.setEnabled(b);
    }
    public void setHARVESTVisible(Boolean b){
        HARVESTBOX.setEnabled(b);
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
    public JComboBox getDateField() {
        return dateBox;
    }

    /**
     * @return the TIDEdit
     */
    public JButton getTIDEdit() {
        return TIDEdit;
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

    /**
     * @return the TID
     */
    public JComboBox getTID() {
        return TID;
    }

    /**
     * @return the FID
     */
    public JComboBox getFID() {
        return FID;
    }

    /**
     * @return the yearBox
     */
    public JComboBox getYearBox() {
        return yearBox;
    }

    public Integer getDate() {
        
        return Integer.valueOf((String)dateBox.getSelectedItem());
    }

  

}
