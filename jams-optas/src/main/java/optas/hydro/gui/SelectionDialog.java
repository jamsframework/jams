/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 *
 * @author chris
 */
public class SelectionDialog extends JDialog {

    private Object selectedAttribute = null;

    public SelectionDialog(Frame owner, Set<Object> attr) {
        super(owner, "Select Attribute", true);
        setLayout(new GridBagLayout());
        final JComboBox attrSelection = new JComboBox();
        for (Object name : attr) {
            attrSelection.addItem(name);
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 10;
        c.ipady = 10;
        add(new JLabel("Select Attribute"), c);


        selectedAttribute = attrSelection.getSelectedItem();
        c.gridx = 1;
        add(attrSelection, c);
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selectedAttribute = attrSelection.getSelectedItem();
                SelectionDialog.this.setVisible(false);
            }
        });
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 1;
        c.anchor = c.CENTER;
        add(okButton, c);

        setPreferredSize(new Dimension(200, 100));
        setMinimumSize(new Dimension(200, 100));

        setLocationRelativeTo(null);

        setVisible(true);
    }

    public Object getSelection() {
        return this.selectedAttribute;
    }
}
