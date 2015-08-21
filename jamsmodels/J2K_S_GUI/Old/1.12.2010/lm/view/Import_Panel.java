/*
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view;

import lm.Componet.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Import_Panel extends JFrame implements Observer {
    
    private Constraints c;
    private static JPanel Content=new JPanel();
    private JTextField Path , fert , till ,crop , ImArable;
    private JButton OKAY;
    private JButton path_dir_button = new JButton("Choose");
    private JButton Import =new JButton("Import");
    private JButton Cancel = new JButton("Cancel");
    private JFileChooser path_dir;
    private JCheckBox relative =new JCheckBox();
    private JLabel lmArable_okay=new JLabel();
    private JLabel fert_okay=new JLabel();
    private JLabel till_okay= new JLabel();
    private JLabel crop_okay=new JLabel();
    private JToolTip not_found=new JToolTip();
    private ImageIcon vorhanden =new ImageIcon("src/lm/resources/images/haken.gif");
    private ImageIcon nicht_vorhanden=new ImageIcon("src/lm/resources/images/kreuz.gif");
    private LMConfig config;

    public Import_Panel(){
        not_found.setTipText("File not Found");
        this.setLayout(new GridLayout(1,1));
        this.setResizable(false);
        this.setMinimumSize(new Dimension(250,250));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        create_ContentPane();
        Content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        this.setContentPane(Content);
        this.pack();


    }
    private void create_ContentPane(){

        //Ordner Textfeld
        Content.setLayout(new GridBagLayout());
        c=new Constraints(0,0);
        c.fill=GridBagConstraints.HORIZONTAL;
        Content.add(new JLabel("Ordner :"),c);
        c=new Constraints(1,0);
        c.fill=GridBagConstraints.HORIZONTAL;
        Path=new JTextField();
        Path.setPreferredSize(new Dimension(150,20));
        Content.add(Path,c);
        c=new Constraints(2,0);

        //Path Chooser Button
        Content.add(path_dir_button,c);


        //ImArable Textfeld
        c=new Constraints(0,1);
        Content.add(new JLabel("IMArable.par :"),c);
        c=new Constraints(1,1);
        c.fill=GridBagConstraints.HORIZONTAL;
        ImArable=new JTextField();
        ImArable.setPreferredSize(new Dimension(150,20));
        Content.add(ImArable,c);
        c=new Constraints(2,1);
        Content.add(lmArable_okay,c);

        //till.par Textfeld
        c=new Constraints(0,2);
        Content.add(new JLabel("till.par :"),c);
        c=new Constraints(1,2);
        c.fill=GridBagConstraints.HORIZONTAL;
        till=new JTextField();
        till.setPreferredSize(new Dimension(150,20));
        Content.add(till,c);
        c=new Constraints(2,2);
        Content.add(till_okay,c);

        //crop.par Textfeld
        c=new Constraints(0,3);
        Content.add(new JLabel("crop.par :"),c);
        c=new Constraints(1,3);
        c.fill=GridBagConstraints.HORIZONTAL;
        crop=new JTextField();
        crop.setPreferredSize(new Dimension(150,20));
        Content.add(crop,c);
        c=new Constraints(2,3);
        Content.add(crop_okay,c);

        //fert.par Textfeld
        c=new Constraints(0,4);
        Content.add(new JLabel("fert.par :"),c);
        c=new Constraints(1,4);
        c.fill=GridBagConstraints.HORIZONTAL;
        fert=new JTextField();
        fert.setPreferredSize(new Dimension(150,20));
        Content.add(fert,c);
        c=new Constraints(2,4);
        Content.add(fert_okay,c);

        //JCheckBox

        c=new Constraints(0,5);
        Content.add(new JLabel("Relative Angabe"),c);
        c=new Constraints(1,5);
        Content.add(relative,c);

        c= new Constraints(0,6);
        Content.add(Import,c);
        c=new Constraints(1,6);
        Content.add(Cancel,c);


    }

    public void path_dir_button_listener(ActionListener add){
        path_dir_button.addActionListener(add);
    }
    public void Cancel_button_Listener(ActionListener add){
        Cancel.addActionListener(add);
    }
    public void Import_button_Listener(ActionListener add){
        Import.addActionListener(add);
    }

    public void update(Observable test2 , Object args){
        int i =(Integer)args;

        if(i==0||i==1){
                if (!config.getLmArable().equals("")){
                    if(i==0){
                        ImArable.setText(config.getLmArable());
                    }else{
                        ImArable.setText("lmArable.par");
                    }
                    lmArable_okay.setIcon(vorhanden);
                    lmArable_okay.setToolTipText(null);
                }else{
                    if(i==0){
                        ImArable.setText(config.getPath() + "lmArable.par");
                    }else{
                        ImArable.setText("lmArable.par");
                    }
                    lmArable_okay.setIcon(nicht_vorhanden);
                    lmArable_okay.setToolTipText("Datei nicht vorhanden");
                }
                if (!config.getFert().equals("")){
                    if(i==0){
                        fert.setText(config.getFert());
                    }else{
                        fert.setText("fert.par");
                    }
                    fert_okay.setIcon(vorhanden);
                    fert_okay.setToolTipText(null);
                }else{
                    if(i==0){
                        fert.setText(config.getPath() + "fert.par");
                    }else{
                        fert.setText("fert.par");
                    }
                    fert_okay.setIcon(nicht_vorhanden);
                    fert_okay.setToolTipText("Datei nicht vorhanden");
                }
                if (!config.getCrop().equals("")){
                    if(i==0){
                        crop.setText(config.getCrop());
                    }else{
                        crop.setText("crop.par");
                    }
                    crop_okay.setIcon(vorhanden);
                    crop_okay.setToolTipText(null);
                }else{
                    if(i==0){
                        crop.setText(config.getPath() + "crop.par");
                    }else{
                        crop.setText("crop.par");
                    }
                    crop_okay.setIcon(nicht_vorhanden);
                    crop_okay.setToolTipText("Datei nicht vorhanden");
                }
                 if (!config.getTill().equals("")){
                    if(i==0){
                        till.setText(config.getTill());
                    }else{
                        till.setText("till.par");
                    }
                    till_okay.setIcon(vorhanden);
                    till_okay.setToolTipText(null);
                }else{
                    if(i==0){
                        till.setText(config.getPath() + "till.par");
                    }else{
                        till.setText("till.par");
                    }
                    till_okay.setIcon(nicht_vorhanden);
                    till_okay.setToolTipText("Datei nicht vorhanden");
                }
            }
        System.out.println("3.   notifyedOberserv");
        System.out.println(ImArable.getText()+"\n"+fert.getText()+"\n"+till.getText()+"\n"+crop.getText());

        
    }
    public void setLMConfig(LMConfig t){
        config=t;
    }
    public int getConfigInt(){
        int i;
        if(relative.isSelected()){
            i=1;
        }else{
            i=0;
        }
        return i;
    }
    public String getLmArable(){
        return ImArable.getText();
    }
    public String getFert(){
        return fert.getText();
    }
    public String getTill(){
        return till.getText();
    }
    public String getCrop(){
        return crop.getText();
    }
}
