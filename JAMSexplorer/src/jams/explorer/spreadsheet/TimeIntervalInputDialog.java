/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.spreadsheet;

import jams.data.Attribute;
import jams.gui.input.TimeintervalInput;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author christian
 */
public class TimeIntervalInputDialog extends JDialog {

    Attribute.TimeInterval tii;

    jams.gui.input.TimeintervalInput inputComponent;
    JLabel msgLabel = new JLabel();
    JButton approveButton = new JButton("Ok"), cancelButton = new JButton("Cancel");

    int result = -1;
    
    protected TimeIntervalInputDialog(String message, Attribute.TimeInterval tii) {
        this.tii = tii;
        inputComponent = new TimeintervalInput(true);
        msgLabel.setText(message);
        result = -1;
        JPanel rootPanel = new JPanel();
        GroupLayout layout = new GroupLayout(rootPanel);
        rootPanel.setLayout(layout);

        inputComponent.setValue(tii.getValue());
        
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                    .addComponent(msgLabel)
                    .addComponent(inputComponent)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(approveButton)
                        .addComponent(cancelButton)
                    )
        );        
        
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addComponent(msgLabel)
                    .addComponent(inputComponent)
                    .addGroup(layout.createParallelGroup()
                        .addComponent(approveButton)
                        .addComponent(cancelButton)
                    )
        );     
        
        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = 1;
                setVisible(false);
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = 0;
                setVisible(false);
            }
        });
        add(rootPanel);
        invalidate();
        pack();
    }
    
    public static Attribute.TimeInterval showTimeIntervalInputDialog(String message, Attribute.TimeInterval tii) {
        TimeIntervalInputDialog tiid = new TimeIntervalInputDialog(message, tii);
        tiid.setModal(true);
        tiid.setVisible(true);
        if (tiid.result==1){
            return tiid.inputComponent.getTimeInterval();
        }else{
            return null;
        }
    }
}
