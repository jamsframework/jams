/*
 * BlankIcon.java
 *
 * Created on 21. November 2005, 12:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package jamschartfactory.tableView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 *
 * @author c0krpe
 */
public class BlankIcon implements Icon {
    
    private Color fillColor;
    private int size;
    
    public BlankIcon() {
        this(null, 11);
    }
    
    public BlankIcon(Color color, int size) {
        //UIManager.getColor("control")
        //UIManager.getColor("controlShadow")
        fillColor = color;
        
        this.size = size;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (fillColor != null) {
            g.setColor(fillColor);
            g.drawRect(x, y, size-1, size-1);
        }
    }
    
    public int getIconWidth() {
        return size;
    }
    
    public int getIconHeight() {
        return size;
    }
    
}
