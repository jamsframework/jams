/*
 * ParameterOutput.java
 * Created on 17.01.2014, 15:11:33
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.gui;

import jams.JAMS;
import jams.data.Attribute;
import jams.io.ParameterProcessor;
import jams.model.*;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "ParameterOutput",
        author = "Sven Kralisch",
        description = "Print list of all parameter values",
        date = "2014-01-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),})
public class ParameterOutput extends JAMSGUIComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            defaultValue = "50",
            description = "Indentation of parameter values"
    )
    public Attribute.Integer indent;

    private JScrollPane scrollPanel;
    private JTextArea logArea;
    private int c = 0;

    private JPanel panel;

    public JPanel getPanel() {
        if (this.panel == null) {
            createPanel();
        }
        return panel;
    }

    @Override
    public void init() {
        if (panel != null) {

            String header = "#" + JAMS.i18n("JAMS_model_parameter_file");
            header += "\n#" + new SimpleDateFormat().format(new Date()) + "\n";
            logArea.append(header);

            HashMap<String, HashMap<String, Element>> componentHash = ParameterProcessor.getAttributeHash(getModel().getRuntime().getModelDocument());
            Properties params = new Properties();

            for (String componentName : componentHash.keySet()) {
                HashMap<String, Element> attributeHash = componentHash.get(componentName);

                for (String attributeName : attributeHash.keySet()) {
                    Element attribute = attributeHash.get(attributeName);
                    String aName = attribute.getAttribute("name");
                    if (!componentName.equals(aName)) {
                        String s = String.format("%" + -indent.getValue() + "s= ", componentName+"."+aName) + attribute.getAttribute("value") + "\n";
                        logArea.append(s);
                    }
                }
            }
        }
    }

    private void createPanel() {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        scrollPanel = new JScrollPane();
        logArea = new JTextArea();
        logArea.setColumns(20);
        logArea.setRows(5);
        logArea.setLineWrap(false);
        logArea.setEditable(false);
        logArea.setFont(JAMS.STANDARD_FONT);
        scrollPanel.setViewportView(logArea);

        panel.add(scrollPanel, BorderLayout.CENTER);
    }

}
