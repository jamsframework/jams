/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.change;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import lm.Componet.Vector.LMCropVector;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class cropPanel extends JFrame {

    private panel panel=new panel();

    public cropPanel(){
        this.setVisible(false);
        this.setContentPane(panel);
        this.setResizable(false);
        this.pack();
        this.setBackground(Color.orange);
       // this.setBorder(BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("CROP")));
    }
    public void setInput(LMCropVector LMCropVector){
        panel.setInput(LMCropVector);
    }
    public ArrayList<String> getInput(){
        return panel.getAll();
    }
     public JList getCropJList(){
        return panel.CropJList;
    }
    public JButton getSaveButton(){
        return panel.Save;
    }
    public JButton getCancelButton(){
        return panel.Cancel;
    }
    public JButton getAddButton(){
        return panel.Add;
    }
    public JButton getDeleteButton(){
        return panel.Delete;
    }



    public class panel extends JPanel{


        private JLabel idcLabel=new JLabel("idc");
        private JLabel cpnmLabel=new JLabel("cpnm");
        private JLabel cropnameLabel=new JLabel("cropname");
        private JLabel phuLabel=new JLabel("phu");
        private JLabel rueLabel=new JLabel("rue");
        private JLabel hvstiLabel=new JLabel("hvsti");
        private JLabel mlaiLabel=new JLabel("mlai");
        private JLabel frgrw1Label=new JLabel("frgrw1");
        private JLabel frgrw2Label=new JLabel("frgrw2");
        private JLabel laimx1Label=new JLabel("laimx1");
        private JLabel laimx2Label=new JLabel("laimx2");
        private JLabel dlaiLabel=new JLabel("dlai");
        private JLabel chtmxLabel=new JLabel("chtmx");
        private JLabel rdmxLabel=new JLabel("rdmx");
        private JLabel toptLabel=new JLabel("topt");
        private JLabel tbaseLabel=new JLabel("tbase");
        private JLabel cnyldLabel=new JLabel("cnyld");
        private JLabel cpyldLabel=new JLabel("cpyld");
        private JLabel bn1Label=new JLabel("bn1");
        private JLabel bn2Label=new JLabel("bn2");
        private JLabel bn3Label=new JLabel("bn3");
        private JLabel bp1Label=new JLabel("bp1");
        private JLabel bp2Label=new JLabel("bp2");
        private JLabel bp3Label=new JLabel("bp3");
        private JLabel wsyfLabel=new JLabel("wsyf");
        private JLabel gsiLabel=new JLabel("gsi");
        private JLabel vpdfrLabel=new JLabel("vpdfr");
        private JLabel frgmaxLabel=new JLabel("frgmax");
        private JLabel wavpLabel=new JLabel("wavp");
        private JLabel co2hiLabel=new JLabel("co2hi");
        private JLabel bioehiLabel=new JLabel("bioehi");
        private JLabel rsdco_plLabel=new JLabel("rsdco_pl");
        private JLabel lidLabel=new JLabel("lid");
        private JLabel hu_ec1Label=new JLabel("hu_ec1");
        private JLabel hu_ec2Label=new JLabel("hu_ec2");
        private JLabel hu_ec3Label=new JLabel("hu_ec3");
        private JLabel hu_ec4Label=new JLabel("hu_ec4");
        private JLabel hu_ec5Label=new JLabel("hu_ec5");
        private JLabel hu_ec6Label=new JLabel("hu_ec6");
        private JLabel hu_ec7Label=new JLabel("hu_ec7");
        private JLabel hu_ec8Label=new JLabel("hu_ec8");
        private JLabel hu_ec9Label=new JLabel("hu_ec9");
        private JLabel lai_minLabel=new JLabel("lai_min");
        private JLabel endbionLabel=new JLabel("endbion");
        private JLabel maxfertLabel=new JLabel("maxfert");
        private JLabel bion02Label=new JLabel("bion02");
        private JLabel bion04Label=new JLabel("bion04");
        private JLabel bion06Label=new JLabel("bion06");
        private JLabel bion08Label=new JLabel("bion08");

        private JTextField idcTextField=new JTextField();
        private JTextField cpnmTextField=new JTextField();
        private JTextField cropnameTextField=new JTextField();
        private JTextField phuTextField=new JTextField();
        private JTextField rueTextField=new JTextField();
        private JTextField hvstiTextField=new JTextField();
        private JTextField mlaiTextField=new JTextField();
        private JTextField frgrw1TextField=new JTextField();
        private JTextField frgrw2TextField=new JTextField();
        private JTextField laimx1TextField=new JTextField();
        private JTextField laimx2TextField=new JTextField();
        private JTextField dlaiTextField=new JTextField();
        private JTextField chtmxTextField=new JTextField();
        private JTextField rdmxTextField=new JTextField();
        private JTextField toptTextField=new JTextField();
        private JTextField tbaseTextField=new JTextField();
        private JTextField cnyldTextField=new JTextField();
        private JTextField cpyldTextField=new JTextField();
        private JTextField bn1TextField=new JTextField();
        private JTextField bn2TextField=new JTextField();
        private JTextField bn3TextField=new JTextField();
        private JTextField bp1TextField=new JTextField();
        private JTextField bp2TextField=new JTextField();
        private JTextField bp3TextField=new JTextField();
        private JTextField wsyfTextField=new JTextField();
        private JTextField gsiTextField=new JTextField();
        private JTextField vpdfrTextField=new JTextField();
        private JTextField frgmaxTextField=new JTextField();
        private JTextField wavpTextField=new JTextField();
        private JTextField co2hiTextField=new JTextField();
        private JTextField bioehiTextField=new JTextField();
        private JTextField rsdco_plTextField=new JTextField();
        private JTextField lidTextField=new JTextField();
        private JTextField hu_ec1TextField=new JTextField();
        private JTextField hu_ec2TextField=new JTextField();
        private JTextField hu_ec3TextField=new JTextField();
        private JTextField hu_ec4TextField=new JTextField();
        private JTextField hu_ec5TextField=new JTextField();
        private JTextField hu_ec6TextField=new JTextField();
        private JTextField hu_ec7TextField=new JTextField();
        private JTextField hu_ec8TextField=new JTextField();
        private JTextField hu_ec9TextField=new JTextField();
        private JTextField lai_minTextField=new JTextField();
        private JTextField endbionTextField=new JTextField();
        private JTextField maxfertTextField=new JTextField();
        private JTextField bion02TextField=new JTextField();
        private JTextField bion04TextField=new JTextField();
        private JTextField bion06TextField=new JTextField();
        private JTextField bion08TextField=new JTextField();

        private JList CropJList=new JList();
        private JButton Save = new JButton("Save");
        private JButton Cancel = new JButton("Cancel");
        private JButton Add = new JButton ("Add");
        private JButton Delete = new JButton("Delete");

        private Dimension text=new Dimension(100,20);

        private Constraints c;


        public panel(){
            createGUI();

        }

        public void setInput(LMCropVector LMCropVector){
            if(LMCropVector!=null){
            idcTextField.setText(String.valueOf(LMCropVector.getIdc()));
              cpnmTextField.setText(String.valueOf(LMCropVector.getCpnm()));
              cropnameTextField.setText(String.valueOf(LMCropVector.getCropname()));
              phuTextField.setText(String.valueOf(LMCropVector.getPhu()));
              rueTextField.setText(String.valueOf(LMCropVector.getRue()));
              hvstiTextField.setText(String.valueOf(LMCropVector.getHvsti()));
              mlaiTextField.setText(String.valueOf(LMCropVector.getMlai()));
              frgrw1TextField.setText(String.valueOf(LMCropVector.getFrgrw1()));
              frgrw2TextField.setText(String.valueOf(LMCropVector.getFrgrw2()));
              laimx1TextField.setText(String.valueOf(LMCropVector.getLaimx1()));
              laimx2TextField.setText(String.valueOf(LMCropVector.getLaimx2()));
              dlaiTextField.setText(String.valueOf(LMCropVector.getDlai()));
              chtmxTextField.setText(String.valueOf(LMCropVector.getChtmx()));
              rdmxTextField.setText(String.valueOf(LMCropVector.getRdmx()));
              toptTextField.setText(String.valueOf(LMCropVector.getTopt()));
              tbaseTextField.setText(String.valueOf(LMCropVector.getTbase()));
              cnyldTextField.setText(String.valueOf(LMCropVector.getCnyld()));
              cpyldTextField.setText(String.valueOf(LMCropVector.getCpyld()));
              bn1TextField.setText(String.valueOf(LMCropVector.getBn1()));
              bn2TextField.setText(String.valueOf(LMCropVector.getBn2()));
              bn3TextField.setText(String.valueOf(LMCropVector.getBn3()));
              bp1TextField.setText(String.valueOf(LMCropVector.getBp1()));
              bp2TextField.setText(String.valueOf(LMCropVector.getBp2()));
              bp3TextField.setText(String.valueOf(LMCropVector.getBp3()));
              wsyfTextField.setText(String.valueOf(LMCropVector.getWsyf()));
              gsiTextField.setText(String.valueOf(LMCropVector.getGsi()));
              vpdfrTextField.setText(String.valueOf(LMCropVector.getVpdfr()));
              frgmaxTextField.setText(String.valueOf(LMCropVector.getFrgmax()));
              wavpTextField.setText(String.valueOf(LMCropVector.getWavp()));
              co2hiTextField.setText(String.valueOf(LMCropVector.getCo2hi()));
              bioehiTextField.setText(String.valueOf(LMCropVector.getBioehi()));
              rsdco_plTextField.setText(String.valueOf(LMCropVector.getRsdco_pl()));
              lidTextField.setText(String.valueOf(LMCropVector.getLid()));
              hu_ec1TextField.setText(String.valueOf(LMCropVector.getHu_ec1()));
              hu_ec2TextField.setText(String.valueOf(LMCropVector.getHu_ec2()));
              hu_ec3TextField.setText(String.valueOf(LMCropVector.getHu_ec3()));
              hu_ec4TextField.setText(String.valueOf(LMCropVector.getHu_ec4()));
              hu_ec5TextField.setText(String.valueOf(LMCropVector.getHu_ec5()));
              hu_ec6TextField.setText(String.valueOf(LMCropVector.getHu_ec6()));
              hu_ec7TextField.setText(String.valueOf(LMCropVector.getHu_ec7()));
              hu_ec8TextField.setText(String.valueOf(LMCropVector.getHu_ec8()));
              hu_ec9TextField.setText(String.valueOf(LMCropVector.getHu_ec9()));
              lai_minTextField.setText(String.valueOf(LMCropVector.getLai_min()));
              endbionTextField.setText(String.valueOf(LMCropVector.getEndbion()));
              maxfertTextField.setText(String.valueOf(LMCropVector.getMaxfert()));
              bion02TextField.setText(String.valueOf(LMCropVector.getBion02()));
              bion04TextField.setText(String.valueOf(LMCropVector.getBion04()));
              bion06TextField.setText(String.valueOf(LMCropVector.getBion06()));
              bion08TextField.setText(String.valueOf(LMCropVector.getBion08()));
            }else{
                this.clearAll();
            }
            this.repaint();
        }
        public ArrayList<String> getAll(){
            ArrayList<String> a =new ArrayList();
            String s=(String)CropJList.getSelectedValue();
            a.add(s.split("\t")[0]);
            a.add(idcTextField.getText());
            a.add(cpnmTextField.getText());
            a.add(cropnameTextField.getText());
            a.add(phuTextField.getText());
            a.add(rueTextField.getText());
            a.add(hvstiTextField.getText());
            a.add(mlaiTextField.getText());
            a.add(frgrw1TextField.getText());
            a.add(frgrw2TextField.getText());
            a.add(laimx1TextField.getText());
            a.add(laimx2TextField.getText());
            a.add(dlaiTextField.getText());
            a.add(chtmxTextField.getText());
            a.add(rdmxTextField.getText());
            a.add(toptTextField.getText());
            a.add(tbaseTextField.getText());
            a.add(cnyldTextField.getText());
            a.add(cpyldTextField.getText());
            a.add(bn1TextField.getText());
            a.add(bn2TextField.getText());
            a.add(bn3TextField.getText());
            a.add(bp1TextField.getText());
            a.add(bp2TextField.getText());
            a.add(bp3TextField.getText());
            a.add(wsyfTextField.getText());
            a.add(gsiTextField.getText());
            a.add(vpdfrTextField.getText());
            a.add(frgmaxTextField.getText());
            a.add(wavpTextField.getText());
            a.add(co2hiTextField.getText());
            a.add(bioehiTextField.getText());
            a.add(rsdco_plTextField.getText());
            a.add(lidTextField.getText());
            a.add(hu_ec1TextField.getText());
            a.add(hu_ec2TextField.getText());
            a.add(hu_ec3TextField.getText());
            a.add(hu_ec4TextField.getText());
            a.add(hu_ec5TextField.getText());
            a.add(hu_ec6TextField.getText());
            a.add(hu_ec7TextField.getText());
            a.add(hu_ec8TextField.getText());
            a.add(hu_ec9TextField.getText());
            a.add(lai_minTextField.getText());
            a.add(endbionTextField.getText());
            a.add(maxfertTextField.getText());
            a.add(bion02TextField.getText());
            a.add(bion04TextField.getText());
            a.add(bion06TextField.getText());
            a.add(bion08TextField.getText());

            return a;
        }

        public void clearAll(){
          idcTextField.setText("");
          cpnmTextField.setText("");
          cropnameTextField.setText("");
          phuTextField.setText("");
          rueTextField.setText("");
          hvstiTextField.setText("");
          mlaiTextField.setText("");
          frgrw1TextField.setText("");
          frgrw2TextField.setText("");
          laimx1TextField.setText("");
          laimx2TextField.setText("");
          dlaiTextField.setText("");
          chtmxTextField.setText("");
          rdmxTextField.setText("");
          toptTextField.setText("");
          tbaseTextField.setText("");
          cnyldTextField.setText("");
          cpyldTextField.setText("");
          bn1TextField.setText("");
          bn2TextField.setText("");
          bn3TextField.setText("");
          bp1TextField.setText("");
          bp2TextField.setText("");
          bp3TextField.setText("");
          wsyfTextField.setText("");
          gsiTextField.setText("");
          vpdfrTextField.setText("");
          frgmaxTextField.setText("");
          wavpTextField.setText("");
          co2hiTextField.setText("");
          bioehiTextField.setText("");
          rsdco_plTextField.setText("");
          lidTextField.setText("");
          hu_ec1TextField.setText("");
          hu_ec2TextField.setText("");
          hu_ec3TextField.setText("");
          hu_ec4TextField.setText("");
          hu_ec5TextField.setText("");
          hu_ec6TextField.setText("");
          hu_ec7TextField.setText("");
          hu_ec8TextField.setText("");
          hu_ec9TextField.setText("");
          lai_minTextField.setText("");
          endbionTextField.setText("");
          maxfertTextField.setText("");
          bion02TextField.setText("");
          bion04TextField.setText("");
          bion06TextField.setText("");
          bion08TextField.setText("");
        }

        public void createGUI(){
        this.setLayout(new GridBagLayout());

        c=new Constraints(0,0,2,17);
        JScrollPane SP=new JScrollPane(CropJList);
        c.fill=GridBagConstraints.BOTH;
        SP.setPreferredSize(new Dimension(175,300));
        this.add(SP,c);

        c=new Constraints(0,18,1,1);
        Add.setPreferredSize(Delete.getPreferredSize());
        c.anchor=GridBagConstraints.EAST;
        this.add(Add,c);

        c=new Constraints(1,18,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(Delete,c);

        c=new Constraints(2,0,1,1);
        //ToolTipp
        this.add(cpnmLabel,c);

        c=new Constraints(3,0,2,1);
        cpnmTextField.setPreferredSize(new Dimension (150,20));
        this.add(cpnmTextField,c);

        c=new Constraints(5,0,1,1);
        //ToolTipp
        this.add(cropnameLabel,c);

        c=new Constraints(6,0,3,1);
        cropnameTextField.setPreferredSize(new Dimension(200,20));
        this.add(cropnameTextField,c);

        c=new Constraints(2,1,1,1);
        //ToolTipp
        this.add(idcLabel,c);

        c=new Constraints(3,1,1,1);
        idcTextField.setPreferredSize(text);
        this.add(idcTextField,c);

        c=new Constraints(2,2,1,1);
        //ToolTipp
        this.add(phuLabel,c);

        c=new Constraints(3,2,1,1);
        phuTextField.setPreferredSize(text);
        this.add(phuTextField,c);

        c=new Constraints(2,3,1,1);
        //ToolTipp
        this.add(rueLabel,c);

        c=new Constraints(3,3,1,1);
        rueTextField.setPreferredSize(text);
        this.add(rueTextField,c);

        c=new Constraints(2,4,1,1);
        //ToolTipp
        this.add(hvstiLabel,c);

        c=new Constraints(3,4,1,1);
        hvstiTextField.setPreferredSize(text);
        this.add(hvstiTextField,c);

        c=new Constraints(2,5,1,1);
        //ToolTipp
        this.add(mlaiLabel,c);

        c=new Constraints(3,5,1,1);
        mlaiTextField.setPreferredSize(text);
        this.add(mlaiTextField,c);

        c=new Constraints(2,6,1,1);
        //ToolTipp
        this.add(frgrw1Label,c);

        c=new Constraints(3,6,1,1);
        frgrw1TextField.setPreferredSize(text);
        this.add(frgrw1TextField,c);

        c=new Constraints(2,7,1,1);
        //ToolTipp
        this.add(frgrw2Label,c);

        c=new Constraints(3,7,1,1);
        frgrw2TextField.setPreferredSize(text);
        this.add(frgrw2TextField,c);

        c=new Constraints(2,8,1,1);
        //ToolTipp
        this.add(laimx1Label,c);

        c=new Constraints(3,8,1,1);
        laimx1TextField.setPreferredSize(text);
        this.add(laimx1TextField,c);

        c=new Constraints(2,9,1,1);
        //ToolTipp
        this.add(laimx2Label,c);

        c=new Constraints(3,9,1,1);
        laimx2TextField.setPreferredSize(text);
        this.add(laimx2TextField,c);

        c=new Constraints(2,10,1,1);
        //ToolTipp
        this.add(dlaiLabel,c);

        c=new Constraints(3,10,1,1);
        dlaiTextField.setPreferredSize(text);
        this.add(dlaiTextField,c);

        c=new Constraints(2,11,1,1);
        //ToolTipp
        this.add(chtmxLabel,c);

        c=new Constraints(3,11,1,1);
        chtmxTextField.setPreferredSize(text);
        this.add(chtmxTextField,c);

        c=new Constraints(2,12,1,1);
        //ToolTipp
        this.add(rdmxLabel,c);

        c=new Constraints(3,12,1,1);
        rdmxTextField.setPreferredSize(text);
        this.add(rdmxTextField,c);

        c=new Constraints(2,13,1,1);
        //ToolTipp
        this.add(toptLabel,c);

        c=new Constraints(3,13,1,1);
        toptTextField.setPreferredSize(text);
        this.add(toptTextField,c);

        c=new Constraints(2,14,1,1);
        //ToolTipp
        this.add(tbaseLabel,c);

        c=new Constraints(3,14,1,1);
        tbaseTextField.setPreferredSize(text);
        this.add(tbaseTextField,c);

        c=new Constraints(2,15,1,1);
        //ToolTipp
        this.add(cnyldLabel,c);

        c=new Constraints(3,15,1,1);
        cnyldTextField.setPreferredSize(text);
        this.add(cnyldTextField,c);

        c=new Constraints(2,16,1,1);
        //ToolTipp
        this.add(cpyldLabel,c);

        c=new Constraints(3,16,1,1);
        cpyldTextField.setPreferredSize(text);
        this.add(cpyldTextField,c);

        c=new Constraints(4,1,1,1);
        //ToolTipp
        this.add(bn1Label,c);

        c=new Constraints(5,1,1,1);
        bn1TextField.setPreferredSize(text);
        this.add(bn1TextField,c);

        c=new Constraints(4,2,1,1);
        //ToolTipp
        this.add(bn2Label,c);

        c=new Constraints(5,2,1,1);
        bn2TextField.setPreferredSize(text);
        this.add(bn2TextField,c);

        c=new Constraints(4,3,1,1);
        //ToolTipp
        this.add(bn3Label,c);

        c=new Constraints(5,3,1,1);
        bn3TextField.setPreferredSize(text);
        this.add(bn3TextField,c);

        c=new Constraints(4,4,1,1);
        //ToolTipp
        this.add(bp1Label,c);

        c=new Constraints(5,4,1,1);
        bp1TextField.setPreferredSize(text);
        this.add(bp1TextField,c);

        c=new Constraints(4,5,1,1);
        //ToolTipp
        this.add(bp2Label,c);

        c=new Constraints(5,5,1,1);
        bp2TextField.setPreferredSize(text);
        this.add(bp2TextField,c);

        c=new Constraints(4,6,1,1);
        //ToolTipp
        this.add(bp3Label,c);

        c=new Constraints(5,6,1,1);
        bp3TextField.setPreferredSize(text);
        this.add(bp3TextField,c);

        c=new Constraints(4,7,1,1);
        //ToolTipp
        this.add(wsyfLabel,c);

        c=new Constraints(5,7,1,1);
        wsyfTextField.setPreferredSize(text);
        this.add(wsyfTextField,c);

        c=new Constraints(4,8,1,1);
        //ToolTipp
        this.add(gsiLabel,c);

        c=new Constraints(5,8,1,1);
        gsiTextField.setPreferredSize(text);
        this.add(gsiTextField,c);

        c=new Constraints(4,9,1,1);
        //ToolTipp
        this.add(vpdfrLabel,c);

        c=new Constraints(5,9,1,1);
        vpdfrTextField.setPreferredSize(text);
        this.add(vpdfrTextField,c);

        c=new Constraints(4,10,1,1);
        //ToolTipp
        this.add(frgmaxLabel,c);

        c=new Constraints(5,10,1,1);
        frgmaxTextField.setPreferredSize(text);
        this.add(frgmaxTextField,c);

        c=new Constraints(4,11,1,1);
        //ToolTipp
        this.add(wavpLabel,c);

        c=new Constraints(5,11,1,1);
        wavpTextField.setPreferredSize(text);
        this.add(wavpTextField,c);

        c=new Constraints(4,12,1,1);
        //ToolTipp
        this.add(co2hiLabel,c);

        c=new Constraints(5,12,1,1);
        co2hiTextField.setPreferredSize(text);
        this.add(co2hiTextField,c);

        c=new Constraints(4,13,1,1);
        //ToolTipp
        this.add(bioehiLabel,c);

        c=new Constraints(5,13,1,1);
        bioehiTextField.setPreferredSize(text);
        this.add(bioehiTextField,c);

        c=new Constraints(4,14,1,1);
        //ToolTipp
        this.add(rsdco_plLabel,c);

        c=new Constraints(5,14,1,1);
        rsdco_plTextField.setPreferredSize(text);
        this.add(rsdco_plTextField,c);

        c=new Constraints(4,15,1,1);
        //ToolTipp
        this.add(lidLabel,c);

        c=new Constraints(5,15,1,1);
        lidTextField.setPreferredSize(text);
        this.add(lidTextField,c);

        c=new Constraints(4,16,1,1);
        //ToolTipp
        this.add(lai_minLabel,c);

        c=new Constraints(5,16,1,1);
        lai_minTextField.setPreferredSize(text);
        this.add(lai_minTextField,c);

        c=new Constraints(6,1,1,1);
        //ToolTipp
        this.add(hu_ec1Label,c);

        c=new Constraints(7,1,2,1);
        hu_ec1TextField.setPreferredSize(text);
        this.add(hu_ec1TextField,c);

        c=new Constraints(6,2,1,1);
        //ToolTipp
        this.add(hu_ec2Label,c);

        c=new Constraints(7,2,2,1);
        hu_ec2TextField.setPreferredSize(text);
        this.add(hu_ec2TextField,c);

        c=new Constraints(6,3,1,1);
        //ToolTipp
        this.add(hu_ec3Label,c);

        c=new Constraints(7,3,2,1);
        hu_ec3TextField.setPreferredSize(text);
        this.add(hu_ec3TextField,c);

        c=new Constraints(6,4,1,1);
        //ToolTipp
        this.add(hu_ec4Label,c);

        c=new Constraints(7,4,2,1);
        hu_ec4TextField.setPreferredSize(text);
        this.add(hu_ec4TextField,c);

        c=new Constraints(6,5,1,1);
        //ToolTipp
        this.add(hu_ec5Label,c);

        c=new Constraints(7,5,2,1);
        hu_ec5TextField.setPreferredSize(text);
        this.add(hu_ec5TextField,c);

        c=new Constraints(6,6,1,1);
        //ToolTipp
        this.add(hu_ec6Label,c);

        c=new Constraints(7,6,2,1);
        hu_ec6TextField.setPreferredSize(text);
        this.add(hu_ec6TextField,c);

        c=new Constraints(6,7,1,1);
        //ToolTipp
        this.add(hu_ec7Label,c);

        c=new Constraints(7,7,2,1);
        hu_ec7TextField.setPreferredSize(text);
        this.add(hu_ec7TextField,c);

        c=new Constraints(6,8,1,1);
        //ToolTipp
        this.add(hu_ec8Label,c);

        c=new Constraints(7,8,2,1);
        hu_ec8TextField.setPreferredSize(text);
        this.add(hu_ec8TextField,c);

        c=new Constraints(6,9,1,1);
        //ToolTipp
        this.add(hu_ec9Label,c);

        c=new Constraints(7,9,2,1);
        hu_ec9TextField.setPreferredSize(text);
        this.add(hu_ec9TextField,c);

        c=new Constraints(6,10,1,1);
        //ToolTipp
        this.add(endbionLabel,c);

        c=new Constraints(7,10,2,1);
        endbionTextField.setPreferredSize(text);
        this.add(endbionTextField,c);

        c=new Constraints(6,11,1,1);
        //ToolTipp
        this.add(maxfertLabel,c);

        c=new Constraints(7,11,2,1);
        maxfertTextField.setPreferredSize(text);
        this.add(maxfertTextField,c);

        c=new Constraints(6,12,1,1);
        //ToolTipp
        this.add(bion02Label,c);

        c=new Constraints(7,12,2,1);
        bion02TextField.setPreferredSize(text);
        this.add(bion02TextField,c);

        c=new Constraints(6,13,1,1);
        //ToolTipp
        this.add(bion04Label,c);

        c=new Constraints(7,13,2,1);
        bion04TextField.setPreferredSize(text);
        this.add(bion04TextField,c);

        c=new Constraints(6,14,1,1);
        //ToolTipp
        this.add(bion06Label,c);

        c=new Constraints(7,14,2,1);
        bion06TextField.setPreferredSize(text);
        this.add(bion06TextField,c);

        c=new Constraints(6,15,1,1);
        //ToolTipp
        this.add(bion08Label,c);

        c=new Constraints(7,15,2,1);
        bion08TextField.setPreferredSize(text);
        this.add(bion08TextField,c);

        c=new Constraints(6,17,1,1);
        Save.setPreferredSize(Cancel.getPreferredSize());
        c.anchor=GridBagConstraints.EAST;
        this.add(Save,c);

        c=new Constraints(7,17,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(Cancel,c);
        }
    }
}
