/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jamsui.juice.optimizer.wizard.OptimizationWizard.ComponentWrapper;
import java.awt.Component;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Christian Fischer
 */
public class Tools {
    
    static class ModelTreeRenderer extends DefaultTreeCellRenderer {
        Icon contextIcon, componentIcon, attributeIcon;

        int ICON_WIDTH = 16;
        int ICON_HEIGHT = 16;

        public ModelTreeRenderer() {
            componentIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Component_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
            contextIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Context_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
            attributeIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/attribute.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel,expanded, leaf, row,hasFocus);
            if (value instanceof DefaultMutableTreeNode){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                if (node.getUserObject() instanceof ComponentWrapper) {
                    ComponentWrapper wrapper = (ComponentWrapper)node.getUserObject();
                    if (wrapper.contextComponent)
                        setIcon(contextIcon);
                    else
                        setIcon(componentIcon);
                }else{
                    setIcon(attributeIcon);
                }
            }
            return this;
        }
    }
    
}
