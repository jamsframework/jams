/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import optas.metamodel.AttributeWrapper;

/**
 *
 * @author chris
 */
public class WizardOutputPanel extends JPanel{
    JList availableAttributes = new JList();
    ArrayList<AttributeWrapper> selectedAttributes = null;

    public WizardOutputPanel(SortedSet<AttributeWrapper> availableAttributes, ArrayList<AttributeWrapper> selectedAttributes ){
        this.selectedAttributes = selectedAttributes;
        DefaultListModel model = new DefaultListModel();
        for (AttributeWrapper a : availableAttributes)
            model.addElement(a);

        this.availableAttributes.setModel(model);

        int indices[] = new int[selectedAttributes.size()];
        int c=0;
        for (AttributeWrapper a : selectedAttributes){
            if (model.indexOf(a)!=-1 )
                indices[c++] = model.indexOf(a);
        }
        indices = Arrays.copyOf(indices, c);
        this.availableAttributes.setSelectedIndices(indices);

        this.availableAttributes.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                JList list = (JList)e.getSource();
                WizardOutputPanel.this.selectedAttributes.clear();
                for (Object o : list.getSelectedValues()){
                    WizardOutputPanel.this.selectedAttributes.add((AttributeWrapper)o);
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(this.availableAttributes);
        listScroller.setPreferredSize(new Dimension(450,420));
        this.add(listScroller);
    }

    public void showDialog(JFrame owner){
        final JDialog dialog = new JDialog(owner);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(this, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        mainPanel.add(okButton, BorderLayout.SOUTH);
        dialog.setSize(500,500);
        dialog.getContentPane().add(mainPanel);
        dialog.setVisible(true);
    }
}
