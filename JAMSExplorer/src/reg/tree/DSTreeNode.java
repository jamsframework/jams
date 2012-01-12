/*
 * DSTreeNode.java
 * Created on 7. April 2006, 21:55
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package reg.tree;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author S. Kralisch
 */
public class DSTreeNode extends DefaultMutableTreeNode {
    
    public final static int IO_ROOT = 0;
    public final static int INPUT_ROOT = 1;
    public final static int OUTPUT_ROOT = 2;
    public final static int INPUT_DS = 3;
    public final static int OUTPUT_DS = 4;
    public final static int OUTPUT_DIR = 5;
    
    static int ICON_WIDTH = 16;
    static int ICON_HEIGHT = 16;
    
    static Icon[] NODE_ICON = {
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("reg/resources/images/inout.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("reg/resources/images/folder.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("reg/resources/images/folder.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("reg/resources/images/spreadsheet.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("reg/resources/images/spreadsheet.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("reg/resources/images/folder.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
    };
    
    private int type = 0;
    
    public DSTreeNode(Object o, int type) {
        super(o);
        this.setType(type);
    }
    
    public DSTreeNode(Object o) {
        super(o);
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public DSTreeNode clone(JAMSTree target) {
        return null;
    }
}
