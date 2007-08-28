/*
 * DataColumnTreeNode.java
 *
 * Created on 20. April 2006, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jamschartfactory;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author c0krpe
 */
public class DataColumnTreeNode extends DefaultMutableTreeNode implements java.awt.event.ActionListener{
    public JPopupMenu popupMenu = null;
    public String selectedAction = null;
    /** Creates a new instance of DataColumnTreeNode */
    public DataColumnTreeNode(String nodeName) {
        super(nodeName);
        //create the popup Menu
        popupMenu = new javax.swing.JPopupMenu();
        popupMenu.setFont(new java.awt.Font("Arial", 0, 11));
        this.createPopupMenu(popupMenu);
    }
    
    /**
     * creates a popup menu for the tree node
     * @param popup the popup menu for the node
     */
    public void createPopupMenu(JPopupMenu popup){
        JMenuItem menuItem = new JMenuItem();
        menuItem = new JMenuItem("New plot ...");
        menuItem.setFont(new java.awt.Font("Arial", 0, 11));
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Add to plot ...");
        menuItem.setFont(new java.awt.Font("Arial", 0, 11));
        menuItem.addActionListener(this);
        popup.add(menuItem);
        
    }
    /**
     * the mouse action from the popup menu
     * @param e the action from the menu
     */
    public void actionPerformed(java.awt.event.ActionEvent e){
        System.out.println("Popup Attributes was selected: " + e.getActionCommand());
        if(e.getActionCommand().equals("New plot ...")){
            int level = this.getRoot().getIndex(this);
            System.out.println("new plot was selected: " + this.getUserObject() + " for series no: " + level);
            MainFrame.seriesNo = level - 1;
            this.selectedAction = "newPlot";
            
            //TimeSeriesPlot tsp = new TimeSeriesPlot(MainFrame.dateMatrix, MainFrame.dataMatrix[19]);
            //this.jPanel1.add(tsp.cp);
        }
        else if(e.getActionCommand().equals("Add to plot ...")){
            System.out.println("add to plot was selected");
            this.selectedAction = "addPlot";
        }
        else{
            System.out.println(this.getUserObject() + " was selected.");
        }
    }
    
}
