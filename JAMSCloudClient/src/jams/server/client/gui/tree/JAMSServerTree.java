/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui.tree;

import jams.server.client.gui.tree.JAMSServerTreeNodes.JobNode;
import jams.server.client.gui.tree.JAMSServerTreeNodes.SortedMutableTreeNode;
import jams.server.client.gui.tree.JAMSServerTreeNodes.UserNode;
import jams.server.client.gui.tree.JAMSServerTreeNodes.WFANode;
import jams.server.client.gui.tree.JAMSServerTreeNodes.WorkspaceNode;
import jams.server.entities.WorkspaceFileAssociation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import static java.awt.Font.PLAIN;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author christian
 */
public abstract class JAMSServerTree extends JTree {

    ImageIcon folderIcon    = new ImageIcon(ClassLoader.getSystemResource("resources/images/folder.png"));
    ImageIcon workingIcon      = new ImageIcon(ClassLoader.getSystemResource("resources/images/wait.png"));
    ImageIcon finishedIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/Ok.png"));
    ImageIcon userIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/user.png"));
    
    ImageIcon modelIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png"));
    ImageIcon jarIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/jar.png"));
    ImageIcon inputIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/input.png"));
    ImageIcon outputIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/output.png"));
    ImageIcon executableIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/exeIcon.png"));
    ImageIcon configIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/Preferences16.png"));
    ImageIcon otherIcon  = new ImageIcon(ClassLoader.getSystemResource("resources/images/help16.png"));
    
    class WorkspaceCellRenderer implements TreeCellRenderer {

        JPanel renderer = new JPanel();
        JLabel item = new JLabel(" ");
        DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
        Font defaultFont = new Font("Arial", PLAIN, 10);
        Color backgroundSelectionColor;
        Color backgroundNonSelectionColor;

        public WorkspaceCellRenderer() {
            backgroundSelectionColor = new Color(220, 220, 220);//defaultRenderer.getBackgroundSelectionColor();
            backgroundNonSelectionColor = defaultRenderer.getBackgroundNonSelectionColor();
            renderer.setLayout(new BorderLayout());
            renderer.add(item, BorderLayout.CENTER);
            item.setFont(defaultFont);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component returnValue = null;
            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                //Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

                if (selected) {
                    renderer.setBackground(backgroundSelectionColor);
                } else {
                    renderer.setBackground(backgroundNonSelectionColor);
                }
                renderer.setMinimumSize(new Dimension(200, 15));
                renderer.setPreferredSize(new Dimension(200, 20));
                renderer.setMaximumSize(new Dimension(200, 25));
                
                renderer.setEnabled(tree.isEnabled());
                renderer.setAlignmentX(0.0f);
                item.setAlignmentX(0.0f);
                item.setHorizontalAlignment(JLabel.LEFT);
                item.setText(value.toString());
                item.setIcon(folderIcon);
                
                if (value instanceof WFANode){
                    renderer.setMinimumSize(new Dimension(200, 12));
                    renderer.setPreferredSize(new Dimension(200, 17));
                    renderer.setMaximumSize(new Dimension(200, 22));
                    
                    WFANode wfaNode = (WFANode)value;
                    switch (wfaNode.getWFA().getRole()){
                        case WorkspaceFileAssociation.ROLE_INPUT: 
                            item.setIcon(inputIcon); break;
                        case WorkspaceFileAssociation.ROLE_OUTPUT: 
                            item.setIcon(outputIcon); break;
                        case WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY:
                            item.setIcon(jarIcon); break;
                        case WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY:
                            item.setIcon(jarIcon); break;
                        case WorkspaceFileAssociation.ROLE_CONFIG:
                            item.setIcon(configIcon); break;
                        case WorkspaceFileAssociation.ROLE_EXECUTABLE:
                            item.setIcon(executableIcon); break;
                        case WorkspaceFileAssociation.ROLE_JAPFILE:
                            item.setIcon(configIcon); break;
                        case WorkspaceFileAssociation.ROLE_MODEL:
                            item.setIcon(modelIcon); break;
                        default:
                            item.setIcon(otherIcon); break;
                    }                    
                }
                
                if (value instanceof UserNode) {
                    renderer.setMinimumSize(new Dimension(200, 20));
                    renderer.setPreferredSize(new Dimension(200, 25));
                    renderer.setMaximumSize(new Dimension(200, 35));
                
                    item.setIcon(userIcon);                    
                }
                
                if (value instanceof WorkspaceNode) {
                    renderer.setMinimumSize(new Dimension(200, 12));
                    renderer.setPreferredSize(new Dimension(200, 17));
                    renderer.setMaximumSize(new Dimension(200, 22));
                    
                    WorkspaceNode wsNode = (WorkspaceNode) value;
                    item.setText(wsNode.toString());
                    item.setIcon(folderIcon);
                }

                if (value instanceof JobNode) {
                    renderer.setMinimumSize(new Dimension(200, 12));
                    renderer.setPreferredSize(new Dimension(200, 17));
                    renderer.setMaximumSize(new Dimension(200, 22));
                    
                    JobNode jobNode = (JobNode) value;
                    item.setText(jobNode.toString());
                    if (jobNode.getJob().getPID() > 0) {
                        item.setForeground(Color.GREEN.darker().darker());
                        item.setIcon(workingIcon);
                    } else {
                        item.setForeground(Color.black);
                        item.setIcon(finishedIcon);
                    }
                }
            }
            returnValue = renderer;
            if (returnValue == null) {
                returnValue = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded,
                        leaf, row, hasFocus);
            }
            return returnValue;
        }
    }

    /**
     *
     */
    public JAMSServerTree() {
        setCellRenderer(new WorkspaceCellRenderer());
    }

    /**
     *
     * @param wfa
     * @param top
     */
    protected void attachWFAtoTree(WorkspaceFileAssociation wfa, SortedMutableTreeNode top) {
        WFANode node = new WFANode(wfa);

        SortedMutableTreeNode currentNode = top;
        for (String dir : node.subdirs) {
            if (node.fileName.equals(dir)) {
                currentNode.add(JAMSServerTreeNodes.getNode(wfa));
                break;
            }
            Enumeration enumeration = currentNode.children();
            boolean successful = false;
            while (enumeration.hasMoreElements()) {
                Object o = enumeration.nextElement();
                if (o.toString().equals(dir)) {
                    currentNode = (SortedMutableTreeNode) o;
                    successful = true;
                    break;
                }
            }
            if (!successful) {
                SortedMutableTreeNode newNode = JAMSServerTreeNodes.getNode(dir);
                currentNode.add(newNode);
                currentNode = newNode;
            }
        }
    }
              
    /**
     *
     * @return
     */
    public SortedMutableTreeNode getSelectedNode(){
        TreePath path = getSelectionPath();
        if (path == null)
            return null;
        Object o = path.getLastPathComponent();
        if (SortedMutableTreeNode.class.isAssignableFrom(o.getClass())) {
            return (SortedMutableTreeNode) o;
        }
        return null;
    }
                    
    /**
     *
     * @return
     */
    public SortedMutableTreeNode getRoot(){
        return (SortedMutableTreeNode)this.getModel().getRoot();
    }

    /**
     *
     * @param e
     * @return
     */
    public Object getUserObjectAtLocation(MouseEvent e) {
        TreePath p = getClosestPathForLocation(e.getX(), e.getY());
        if (p != null) {
            getSelectionModel().setSelectionPath(p);
        } else {
            getSelectionModel().clearSelection();
        }
        if (!(e.getComponent() instanceof JTree)) {
            return null;
        }
        if (p==null){
            return null;
        }
        Object lastTreePathObject = p.getLastPathComponent();
        if (DefaultMutableTreeNode.class.isAssignableFrom(lastTreePathObject.getClass())) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastTreePathObject;
            return node.getUserObject();            
        }
        return null;
    }
}
