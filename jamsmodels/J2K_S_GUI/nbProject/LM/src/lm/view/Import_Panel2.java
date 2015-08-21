/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import lm.Componet.LMConfig;

/**
 *
 * @author MultiMedia
 */
public class Import_Panel2 extends JFrame implements Observer {
    
    private Panel panel;
    private LMConfig lmConfig;
    
    public Import_Panel2(LMConfig lmconfig){
        this.lmConfig=lmconfig;
        this.setResizable(false);    
        this.setTitle("Import Panel");
        panel=new Panel();
        this.setContentPane(panel);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        panel.createPanel();
        this.pack();
    }
    
    
    public void start(){
        this.setVisible(true);       
    }
    
    public void close(){
        this.dispose();
    }
    
    public void setActionListerToChooseSpecial(ActionListener l){
        panel.chooseCrop.addActionListener(l);
        panel.chooseCropRotation.addActionListener(l);
        panel.chooseFert.addActionListener(l);
        panel.chooseTill.addActionListener(l);
        panel.chooseLmArable.addActionListener(l);
        panel.chooseLanduse.addActionListener(l);
        panel.chooseHru_rot_acker.addActionListener(l);
    }
    
    public void setActionListener_choose (ActionListener l){
    panel.chooseAll.addActionListener(l);    
    }
    
    public void setActionListener_import (ActionListener l){
    panel.import_.addActionListener(l);    
    } 
    
    public void setActionListener_cancel (ActionListener l){
    panel.cancel.addActionListener(l);    
    } 
    
    public void setActionListener_textfield (ActionListener l){
        panel.crop.addActionListener(l);
        panel.till.addActionListener(l);
        panel.fert.addActionListener(l);
        panel.cropRotation.addActionListener(l);
        panel.lmArable.addActionListener(l);
        panel.landuse.addActionListener(l);
        panel.hru_rot_acker.addActionListener(l);
    }
    
    public void setKeyListenerToTextfields(KeyListener kl){
        panel.crop.addKeyListener(kl);
        panel.till.addKeyListener(kl);
        panel.fert.addKeyListener(kl);
        panel.lmArable.addKeyListener(kl);
        panel.cropRotation.addKeyListener(kl);
        panel.landuse.addKeyListener(kl);
        panel.hru_rot_acker.addKeyListener(kl);
    }
    
    public void setActionListenerToPathField(ActionListener l){
        panel.path.addActionListener(l);
    }
    public void setKeyListenerToPathField(KeyListener kl){
        panel.path.addKeyListener(kl);
    }

    public void update(Observable o, Object arg) {
        int i =(Integer)arg;
        if(i!=1){
            panel.path.setText(lmConfig.getPath());
            panel.crop.setText(lmConfig.getCrop());
            panel.till.setText(lmConfig.getTill());
            panel.fert.setText(lmConfig.getFert());
            panel.cropRotation.setText(lmConfig.getCropRotation());
            panel.lmArable.setText(lmConfig.getLmArable());
            panel.landuse.setText(lmConfig.getLanduse());
            panel.hru_rot_acker.setText(lmConfig.getHru_rot_acker());                   
        }        
                
        //Set the Path from Crop
        if(lmConfig.getCropCheck()){           
            panel.crop_okay.setIcon(panel.vorhanden);
        }else{            
            panel.crop_okay.setIcon(panel.nicht_vorhanden);
        }
        //Set the Path from Till
        if(lmConfig.getTillCheck()){
            panel.till_okay.setIcon(panel.vorhanden);
        }else{
            panel.till_okay.setIcon(panel.nicht_vorhanden);
        }
        //Set the Path from fert
        if(lmConfig.getFertCheck()){
             panel.fert_okay.setIcon(panel.vorhanden);
        }else{
            panel.fert_okay.setIcon(panel.nicht_vorhanden);
        }
        //Set the Path from Croprotation
        if(lmConfig.getCropRotationCheck()){
            panel.cropRotation_okay.setIcon(panel.vorhanden);
        }else{
           panel.cropRotation_okay.setIcon(panel.nicht_vorhanden);
        }
        //Set the Path from lmArable
        if(lmConfig.getLmArableCheck()){
            panel.lmArable_okay.setIcon(panel.vorhanden);
        }else{
          panel.lmArable_okay.setIcon(panel.nicht_vorhanden);
        }
        //Set the Path from landuse
        if(lmConfig.getLanduseCheck()){
            panel.landuse_okay.setIcon(panel.vorhanden);
        }else{
          panel.landuse_okay.setIcon(panel.nicht_vorhanden);
        }
        //Set the Path from Hru_rot_acker
        if(lmConfig.getHru_rot_ackerCheck()){
            panel.hru_rot_acker_okay.setIcon(panel.vorhanden);
        }else{
         panel.hru_rot_acker_okay.setIcon(panel.nicht_vorhanden);
        }
            
    }
        
            
            
    
    
    public class Panel extends JPanel{
        
        private Constraints c;
        private JButton chooseAll = new JButton("ChosseAll");
        private JButton import_ = new JButton("Import");
        private JButton cancel = new JButton ("Cancel"); 
        private JButton chooseCrop =new JButton ("Choose");
        private JButton chooseFert =new JButton ("Choose");
        private JButton chooseTill =new JButton ("Choose");
        private JButton chooseLmArable =new JButton ("Choose");
        private JButton chooseCropRotation =new JButton ("Choose");
        private JButton chooseLanduse =new JButton ("Choose");
        private JButton chooseHru_rot_acker =new JButton ("Choose");
        private JTextField path = new JTextField();
        private JTextField fert = new JTextField();
        private JTextField till = new JTextField();
        private JTextField crop = new JTextField();
        private JTextField lmArable = new JTextField();
        private JTextField cropRotation = new JTextField();
        private JTextField landuse = new JTextField();
        private JTextField hru_rot_acker = new JTextField();      
        private JLabel lmArable_okay=new JLabel();
        private JLabel fert_okay=new JLabel();
        private JLabel till_okay= new JLabel();
        private JLabel crop_okay=new JLabel();
        private JLabel cropRotation_okay=new JLabel();
        private JLabel landuse_okay=new JLabel();
        private JLabel hru_rot_acker_okay = new JLabel();
        private JToolTip not_found=new JToolTip();
        private ImageIcon vorhanden =new ImageIcon(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SRC/LM/RESOURCES/IMAGES/HAKEN.GIF"));
        private ImageIcon nicht_vorhanden=new ImageIcon(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("SRC/LM/RESOURCES/IMAGES/KREUZ.GIF"));
    
        
        
        public void createPanel(){
            
            this.setLayout(new GridBagLayout());
            
            //Insert Line 0 filled with a Header
            c=new Constraints(0,0,4,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            this.add(new JLabel("IMPORT FROM EXSISTEND DATA"),c);
            
            
            //Insert Line 1 filled with all from Path and the choose Button
            c=new Constraints(0,1,1,1);
            c.anchor=GridBagConstraints.CENTER;
            this.add(new JLabel("Path"),c);
            
            c=new Constraints(1,1,1,1);
            c.anchor=GridBagConstraints.CENTER;
            path.setMinimumSize(new Dimension(150,20));
            path.setPreferredSize(new Dimension(150,20));
            this.add(path,c);
            
            c=new Constraints (3,1,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            this.add(chooseAll,c);
            
            //Insert Line 2 filled with all from Crop
            
            c=new Constraints(0,2,1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel("crop.par"),c);
            
            c=new Constraints(1,2,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;           
            crop.setMinimumSize(new Dimension(150,20));
            crop.setActionCommand("crop");
            this.add(crop,c);
            
            c=new Constraints (2,2,1,1);
            c.anchor=GridBagConstraints.CENTER;
            crop_okay.setIcon(nicht_vorhanden);
            this.add(crop_okay,c);
            
            c=new Constraints (3,2,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            chooseCrop.setActionCommand("crop");
            this.add(chooseCrop,c);
            
            //Insert Line 3 filled with all from fert
            
            c=new Constraints(0,3,1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel("fert.par"),c);
            
            c=new Constraints(1,3,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            fert.setMinimumSize(new Dimension(150,20));
            fert.setActionCommand("fert");
            this.add(fert,c);
            
            c=new Constraints(2,3,1,1);
            c.anchor=GridBagConstraints.CENTER;
            fert_okay.setIcon(nicht_vorhanden);
            this.add(fert_okay,c);          
            
            c=new Constraints (3,3,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            chooseFert.setActionCommand("fert");
            this.add(chooseFert,c);
            
            
            //Insert Line 4 filled with all from till
            
            c=new Constraints(0,4,1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel("till.par"),c);
            
            c=new Constraints(1,4,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            till.setMinimumSize(new Dimension(150,20));
            till.setActionCommand("till");
            this.add(till,c);
            
            c=new Constraints(2,4,1,1);
            c.anchor=GridBagConstraints.CENTER;
            till_okay.setIcon(nicht_vorhanden);
            this.add(till_okay,c);            
            
            c=new Constraints (3,4,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            chooseTill.setActionCommand("till");
            this.add(chooseTill,c);
            
            //Insert Line 5 filled with all from lmArable
            
            c=new Constraints(0,5,1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel("lmArable.par"),c);
            
            c=new Constraints(1,5,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            lmArable.setMinimumSize(new Dimension(150,20));
            lmArable.setActionCommand("lmarable");
            this.add(lmArable,c);
            
            c=new Constraints(2,5,1,1);
            c.anchor=GridBagConstraints.CENTER;
            lmArable_okay.setIcon(nicht_vorhanden);
            this.add(lmArable_okay,c);            
            
            c=new Constraints (3,5,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            chooseLmArable.setActionCommand("lmarable");
            this.add(chooseLmArable,c);
            
            
            //Insert Line 6 filled with all from croprotation
            
            c=new Constraints(0,6,1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel("cropRotation.par"),c);
            
            c=new Constraints(1,6,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            cropRotation.setMinimumSize(new Dimension(150,20));
            cropRotation.setActionCommand("croprotation");
            this.add(cropRotation,c);
            
            c=new Constraints(2,6,1,1);
            c.anchor=GridBagConstraints.CENTER;
            cropRotation_okay.setIcon(nicht_vorhanden);
            this.add(cropRotation_okay,c);           
            
            c=new Constraints (3,6,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            chooseCropRotation.setActionCommand("croprotation");
            this.add(chooseCropRotation,c);
            
            
            //Insert Line 7 filled with all from landuse
            
            c=new Constraints(0,7,1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel("landuse.par"),c);
            
            c=new Constraints(1,7,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            landuse.setMinimumSize(new Dimension(150,20));
            landuse.setActionCommand("landuse");
            this.add(landuse,c);
            
            c=new Constraints(2,7,1,1);
            c.anchor=GridBagConstraints.CENTER;
            landuse_okay.setIcon(nicht_vorhanden);
            this.add(landuse_okay,c);            
            
            c=new Constraints (3,7,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            chooseLanduse.setActionCommand("landuse");
            this.add(chooseLanduse,c);
            
            
            //Insert Line 8 filled with all from hru_rot_acker
            
            c=new Constraints(0,8,1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel("hru_rot_acker.par"),c);
            
            c=new Constraints(1,8,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            hru_rot_acker.setMinimumSize(new Dimension(150,20));
            hru_rot_acker.setActionCommand("hru_rot_acker");
            this.add(hru_rot_acker,c);
            
            c=new Constraints(2,8,1,1);
            c.anchor=GridBagConstraints.CENTER;
            hru_rot_acker_okay.setIcon(nicht_vorhanden);
            this.add(hru_rot_acker_okay,c);
            
            c=new Constraints (3,8,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            chooseHru_rot_acker.setActionCommand("hru_rot_acker");
            this.add(chooseHru_rot_acker,c);            
            
            //Insert Line 9 with Import and Cancel Button
            
            c=new Constraints(0,9,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            this.add(cancel,c);
            
            c=new Constraints(3,9,1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            this.add(import_,c);
            
        }
    }
    
}
