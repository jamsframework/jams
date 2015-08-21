/*
 * InputInfoPanelSimple.java
 * Created on 11. June 2009, 10:55
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
 *
 */
package jams.explorer.gui;

import jams.gui.tools.GUIHelper;
import jams.workspace.stores.StandardInputDataStore;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * a simple info panel for input sources
 * consists of some text fields and a textarea at the end
 * known implementations:
 * @see InputInfoPanelTS
 * @see InputInfoPanelShape
 *
 * @author hbusch
 */
public class InputInfoPanelSimple extends JPanel {

    /**
     * the main layout
     **/
    protected GridBagLayout mainLayout;

    /**
     * the number of text fields, has to be overwritten
     */
    protected int FIELD_COUNT = 0;

    /**
     * the text fields
     */
    protected JTextField[] fields;

    /**
     * textarea (for description)
     */
    protected JTextArea textArea;

    /**
     * Constructor
     * set all fields and the textarea into layout
     * @param fieldCount
     */
    public InputInfoPanelSimple(int fieldCount) {
        super();
        this.FIELD_COUNT = fieldCount;
        mainLayout = new GridBagLayout();
        this.setLayout(mainLayout);
        fields = new JTextField[FIELD_COUNT];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new JTextField();
            fields[i].setColumns(20);
            fields[i].setEditable(false);
            GUIHelper.addGBComponent(this, mainLayout, fields[i], 2, i, 1, 1, 0, 0);
        }
        textArea = new JTextArea();
        textArea.setRows(5);
        textArea.setColumns(20);
        textArea.setEditable(false);
        Font textFont = (Font) UIManager.getDefaults().get("Label.font");
        textFont = new Font(textFont.getName(), Font.PLAIN, textFont.getSize() - 1);
        textArea.setFont(textFont);
        JScrollPane textScrollPane = new JScrollPane(textArea);
        GUIHelper.addGBComponent(this, mainLayout, textScrollPane, 2, FIELD_COUNT, 1, 2, 0, 0);

    }

    /**
     * update the info panel
     *
     * @param datastore
     */
    public void updateInfoPanel(StandardInputDataStore datastore) {

    }
    
}
