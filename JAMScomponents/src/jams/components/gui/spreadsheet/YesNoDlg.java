package jams.components.gui.spreadsheet;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class YesNoDlg extends JDialog implements ActionListener{
    
    String result;
    
    public YesNoDlg(Frame parent, String text){
        
        super(parent, true);
        
        setLayout(new BorderLayout());
        setResizable(false);
        Point parloc = parent.getLocation();
        setLocation(parloc.x + 280, parloc.y + 150);
        
        add(new Label(text), BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton yesbutton = new JButton("Yes");
        JButton nobutton = new JButton("No");
        JButton cancelbutton = new JButton("Cancel");
        yesbutton.addActionListener(this);
        nobutton.addActionListener(this);
        cancelbutton.addActionListener(this);
        panel.add(yesbutton);
        panel.add(nobutton);
        panel.add(cancelbutton);
        add(panel, BorderLayout.SOUTH);
        pack();      
    }
    
    public void actionPerformed(ActionEvent e){
        
        result = e.getActionCommand();
        setVisible(false);
        dispose();
    }
    
    public String getResult(){
        return result;
    }
    
    
    
    
    
}