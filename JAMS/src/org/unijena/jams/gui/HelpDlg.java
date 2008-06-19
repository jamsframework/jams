/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.jams.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.unijena.jams.data.HelpComponent;

/**
 *
 * @author Heiko Busch
 */
    public class HelpDlg extends JDialog {
    
        public final static int OK_RESULT = 0;
        public final static int CANCEL_RESULT = -1;
        private JTextField helpURLField;
        private HelpComponent helpComponent;
        
        public HelpDlg(Frame owner) {
            super(owner);
            setLocationRelativeTo(owner);
            init();
        }

    public HelpComponent getHelpComponent() {
        return helpComponent;
    }

    public void setHelpComponent(HelpComponent helpComponent) {
        this.helpComponent = helpComponent;
        helpURLField.setText(this.helpComponent.getHelpURL());
    }

        
        private void init() {

            setModal(true);
            this.setTitle("Help");

            this.setLayout(new BorderLayout());
            GridBagLayout gbl = new GridBagLayout();

            
            
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(gbl);

            LHelper.addGBComponent(contentPanel, gbl, new JPanel(), 0, 0, 1, 1, 0, 0);
            LHelper.addGBComponent(contentPanel, gbl, new JLabel("Help URL:"), 0, 1, 1, 1, 0, 0);

            helpURLField = new JTextField();

            helpURLField.setPreferredSize(new Dimension(200, 20));
            LHelper.addGBComponent(contentPanel, gbl, helpURLField, 1, 3, 2, 1, 0, 0);


            JButton okButton = new JButton("OK");
            ActionListener okListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            };
            okButton.addActionListener(okListener);
            getRootPane().setDefaultButton(okButton);


            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);

            JScrollPane scrollPane = new JScrollPane(contentPanel);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            pack();
        }
    }
