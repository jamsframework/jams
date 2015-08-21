package lm.view.change;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import lm.Componet.Vector.LMFertVector;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class fertPanel extends JFrame{

    private Panel panel=new Panel();

    public fertPanel(){
        this.setVisible(false);
        this.setContentPane(panel);
        this.setResizable(false);
        pack();
    }
    public void setInput(LMFertVector LMFertVector){
        this.panel.setInput(LMFertVector);
    }
//    public ArrayList<String> getInput(){
//        return panel.getAll();
//    }
    public ArrayList<String> getInput(){
        return panel.getAll();
    }

    public JList getFertJList(){
        return panel.FertJList;
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


public class Panel extends JPanel {

    private Constraints c;
    private Insets insets=new Insets(3,3,3,3);

    private JLabel FIDLabel =new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FID :"));
    private JLabel fertnmLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FERTNM :"));
    private JLabel fminnLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FMINN :"));
    private JLabel fminpLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FMINP :"));
    private JLabel forgnLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FORGN :"));
    private JLabel forgpLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FORGP :"));
    private JLabel fnh3nLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FNH3N :"));
    private JLabel bactpdbLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("BACTPDB :"));
    private JLabel bactldbLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("BACTLDB :"));
    private JLabel bactddbLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("BACTDDB :"));
    private JLabel descLabel=new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("DESC :"));

    private JTextField FIDTextField =new JTextField();
    private JTextField fertnmTextField=new JTextField();
    private JTextField fminnTextField=new JTextField();
    private JTextField fminpTextField=new JTextField();
    private JTextField forgnTextField=new JTextField();
    private JTextField forgpTextField=new JTextField();
    private JTextField fnh3nTextField=new JTextField();
    private JTextField bactpdbTextField=new JTextField();
    private JTextField bactldbTextField=new JTextField();
    private JTextField bactddbTextField=new JTextField();
    private JTextField descTextField=new JTextField();

    private JList FertJList=new JList();

    private JButton Save=new JButton("Save");
    private JButton Cancel =new JButton("Cancel");

    private JButton Add=new JButton("Add");
    private JButton Delete=new JButton("Delete");


    public Panel(){
        this.setBackground(Color.red);
        this.setBorder(BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FERTILIZATION")));
        this.createGUI();
    }

    public void setInput(LMFertVector LMFertVector){
        if(LMFertVector!=null){
         FIDTextField.setText(String.valueOf(LMFertVector.getID()));
    fertnmTextField.setText(String.valueOf(LMFertVector.getfertnm()));
    fminnTextField.setText(String.valueOf(LMFertVector.getfminn()));
    fminpTextField.setText(String.valueOf(LMFertVector.getfminp()));
    forgnTextField.setText(String.valueOf(LMFertVector.getforgn()));
    forgpTextField.setText(String.valueOf(LMFertVector.getforgp()));
    fnh3nTextField.setText(String.valueOf(LMFertVector.getfnh3n()));
    bactpdbTextField.setText(String.valueOf(LMFertVector.getbactpdb()));
    bactldbTextField.setText(String.valueOf(LMFertVector.getbactldb()));
    bactddbTextField.setText(String.valueOf(LMFertVector.getbactddb()));
    descTextField.setText(String.valueOf(LMFertVector.getdesc()));
    this.repaint();
        }else{
            panel.clearAll();
        }
    }

    public void createGUI(){

    this.setLayout(new GridBagLayout());

    c=new Constraints(0,0,2,10);
    JScrollPane SP=new JScrollPane(FertJList);
    SP.setPreferredSize(new Dimension(175,300));
    this.add(SP,c);

    c=new Constraints(0,10,1,1);
    c.anchor=GridBagConstraints.EAST;
    Add.setPreferredSize(Delete.getPreferredSize());
    this.add(Add,c);

    c=new Constraints(1,10,1,1);
    c.anchor=GridBagConstraints.WEST;
    this.add(Delete,c);


        c=new Constraints(2,0,1,1);
        fertnmLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("fertnm"));
        this.add(fertnmLabel,c);

        c=new Constraints(3,0,2,1);
        fertnmTextField.setPreferredSize(new Dimension(100,20));
        this.add(fertnmTextField,c);

        c=new Constraints(2,1,1,1);
        fminnLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("fminn"));
        this.add(fminnLabel,c);

        c=new Constraints(3,1,2,1);
        fminnTextField.setPreferredSize(new Dimension(100,20));
        this.add(fminnTextField,c);
        //2te ZEILE
        c=new Constraints(2,2,1,1);
        fminpLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("fminp"));
        this.add(fminpLabel,c);

        c=new Constraints(3,2,2,1);
        fminpTextField.setPreferredSize(new Dimension(100,20));
        this.add(fminpTextField,c);

        c=new Constraints(2,3,1,1);
        forgnLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("forgn"));
        this.add(forgnLabel,c);

        c=new Constraints(3,3,2,1);
        forgnTextField.setPreferredSize(new Dimension(100,20));
        this.add(forgnTextField,c);
        //3tes ZEILE
        c=new Constraints(2,4,1,1);
        forgpLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("forgp"));
        this.add(forgpLabel,c);

        c=new Constraints(3,4,2,1);
        forgpTextField.setPreferredSize(new Dimension(100,20));
        this.add(forgpTextField,c);

        c=new Constraints(2,5,1,1);
        fnh3nLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("fnh3n"));
        this.add(fnh3nLabel,c);

        c=new Constraints(3,5,2,1);
        fnh3nTextField.setPreferredSize(new Dimension(100,20));
        this.add(fnh3nTextField,c);
       //4tes ZEILE
        c=new Constraints(2,6,1,1);
        bactpdbLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("bactpdb"));
        this.add(bactpdbLabel,c);

        c=new Constraints(3,6,2,1);
        bactpdbTextField.setPreferredSize(new Dimension(100,20));
        this.add(bactpdbTextField,c);

        c=new Constraints(2,7,1,1);
        bactldbLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("bactldb"));
        this.add(bactldbLabel,c);

        c=new Constraints(3,7,2,1);
        bactldbTextField.setPreferredSize(new Dimension(100,20));
        this.add(bactldbTextField,c);
        //5tes ZEILE
        c=new Constraints(2,8,1,1);
        bactddbLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("bactddb"));
        this.add(bactddbLabel,c);

        c=new Constraints(3,8,2,1);
        bactddbTextField.setPreferredSize(new Dimension(100,20));
        this.add(bactddbTextField,c);

        c=new Constraints(2,9,1,1);
        descLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("desc"));
        this.add(descLabel,c);

        c=new Constraints(3,9,2,1);
        descTextField.setPreferredSize(new Dimension(100,20));
        this.add(descTextField,c);

        c=new Constraints(3,10,1,1);
        c.anchor=GridBagConstraints.EAST;
        Save.setPreferredSize(Cancel.getPreferredSize());
        this.add(Save,c);

        c=new Constraints(4,10,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(Cancel,c);

    }
    public void clearAll(){
    fertnmTextField.setText("");
    fminnTextField.setText("");
    fminpTextField.setText("");
    forgnTextField.setText("");
    forgpTextField.setText("");
    fnh3nTextField.setText("");
    bactpdbTextField.setText("");
    bactldbTextField.setText("");
    bactddbTextField.setText("");
    descTextField.setText("");
    }

    public ArrayList<String> getAll(){
        ArrayList<String> a=new ArrayList();
        String s =(String)FertJList.getSelectedValue();
        a.add(s.split("/t")[0]);
        a.add(fertnmTextField.getText());
        a.add(fminnTextField.getText());
        a.add(fminpTextField.getText());
        a.add(forgnTextField.getText());
        a.add(forgpTextField.getText());
        a.add(fnh3nTextField.getText());
        a.add(bactpdbTextField.getText());
        a.add(bactldbTextField.getText());
        a.add(bactddbTextField.getText());
        a.add(descTextField.getText());
        return a;
    }
}
}
