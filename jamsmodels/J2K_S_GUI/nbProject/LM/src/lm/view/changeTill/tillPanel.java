package lm.view.changeLMArable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import lm.Componet.Vector.LMTillVector;
import lm.view.Constraints;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class tillPanel extends JFrame{

    private Panel panel=new Panel();

    public tillPanel(){
        this.setVisible(false);
        this.setContentPane(panel);
        this.setResizable(false);
        pack();
    }

    public void setInput(LMTillVector LMTillVector){
        this.panel.setInput(LMTillVector);
    }
    public ArrayList<String> getInput(){
        return panel.getAll();
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
    public JList getTillJList(){
        return panel.TillJList;
    }

private class Panel extends JPanel {

    private Constraints c;
    private Insets insets=new Insets(3,3,3,3);

    private JTextField effmixTextField=new JTextField();
    private JTextField deptilTextField=new JTextField();
    private JTextField tillnmTextField=new JTextField();
    private JTextField descTextField = new JTextField();
    private JLabel effmixLabel = new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("EFFMIX :"));
    private JLabel deptilLabel = new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("DEPTIL :"));
    private JLabel tillnmLabel = new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("TILLNM :"));
    private JLabel descLabel = new JLabel(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("DESC :"));

    private JList TillJList =new JList();

    private JButton Save=new JButton("Save");
    private JButton Cancel =new JButton("Cancel");

    private JButton Add =new JButton("Add");
    private JButton Delete = new JButton("Delete");


    private LMTillVector Vector;

    public Panel(){
        this.setBackground(Color.yellow);
        this.setBorder(BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("TILLAGE")));
        this.createGUI();
    }

    public void setInput(LMTillVector LMTillVector){
        if(LMTillVector != null){
            effmixTextField.setText(String.valueOf(LMTillVector.geteffmix()));
            deptilTextField.setText(String.valueOf(LMTillVector.getdeptil()));
            tillnmTextField.setText(String.valueOf(LMTillVector.gettillnm()));
            descTextField.setText(String.valueOf(LMTillVector.getdesc()));
        }else{
            clearAll();
        }

        this.repaint();
    }

    public void createGUI(){
        this.setLayout(new GridBagLayout());

        c=new Constraints(0,0,2,4);
        JScrollPane SP =new JScrollPane(TillJList);
        SP.setPreferredSize(new Dimension(175,300));
        this.add(SP,c);

        c=new Constraints(0,4,1,1);
        c.anchor=GridBagConstraints.EAST;
        Add.setPreferredSize(Delete.getPreferredSize());
        this.add(Add,c);

        c=new Constraints(1,4,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(Delete,c);

        c=new Constraints(2,0,1,1);
        tillnmLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("tillnm"));
        this.add(tillnmLabel,c);

        c=new Constraints(3,0,2,1);
        tillnmTextField.setPreferredSize(new Dimension(150,20));
        this.add(tillnmTextField,c);

        c=new Constraints(2,1,1,1);
        descLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("desc"));
        this.add(descLabel,c);

        c=new Constraints(3,1,2,1);
        descTextField.setPreferredSize(new Dimension(150,20));
        this.add(descTextField,c);

        c=new Constraints(2,2,1,1);
        effmixLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("effmix"));
        this.add(effmixLabel,c);

        c=new Constraints(3,2,2,1);
        effmixTextField.setPreferredSize(new Dimension(150,20));
        this.add(effmixTextField,c);

        c=new Constraints(2,3,1,1);
        c.anchor=GridBagConstraints.NORTH;
        deptilLabel.setToolTipText(java.util.ResourceBundle.getBundle("lm/properties/ToolTipps").getString("deptil"));
        this.add(deptilLabel,c);

        c=new Constraints(3,3,2,1);
        c.anchor=GridBagConstraints.NORTH;
        deptilTextField.setPreferredSize(new Dimension(150,20));
        this.add(deptilTextField,c);

        c=new Constraints(3,4,1,1);
        c.anchor=GridBagConstraints.EAST;
        Save.setPreferredSize(Cancel.getPreferredSize());
        this.add(Save,c);

        c=new Constraints(4,4,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(Cancel,c);



    }

    public void clearAll(){
            effmixTextField.setText("");
            deptilTextField.setText("");
            tillnmTextField.setText("");
            descTextField.setText("");
    }

    public ArrayList<String> getAll(){
        ArrayList<String> a= new ArrayList();
        String s =(String) TillJList.getSelectedValue();
        a.add(s.split("\n")[0]);
        a.add(tillnmTextField.getText());
        a.add(descTextField.getText());
        a.add(effmixTextField.getText());
        a.add(deptilTextField.getText());
        return a;
    }

}
}
