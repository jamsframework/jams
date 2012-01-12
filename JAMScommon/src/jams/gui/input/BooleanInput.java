/*
 * BooleanInput.java
 * Created on 7. September 2006, 10:26
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
 *
 */
package jams.gui.input;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 *
 * @author S. Kralisch
 */
public class BooleanInput extends JCheckBox implements InputComponent {

    private ValueChangeListener l;

    public String getValue() {
        if (isSelected()) {
            return "1";
        } else {
            return "0";
        }
    }

    public void setValue(String value) {
        if ("1".equals(value) || "true".equals(value)) {
            this.setSelected(true);
        } else {
            this.setSelected(false);
        }
    }

    public JComponent getComponent() {
        return this;
    }

    public void setRange(double lower, double upper) {
    }

    public boolean verify() {
        return true;
    }

    public int getErrorCode() {
        return INPUT_OK;
    }

    public void setLength(int length) {
    }

    public void addValueChangeListener(ValueChangeListener l) {
        this.l = l;
        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                BooleanInput.this.l.valueChanged();
            }
        });
    }

    private Color oldColor;
    public void setMarked(boolean marked) {
        if (marked == true) {
            oldColor = getBackground();
            this.setBackground(new Color(255, 0, 0));
        } else {
            this.setBackground(oldColor);
        }
    }

    public void setHelpText(String text) {
        text = "<html>" + text + "</html>";
        getComponent().setToolTipText(text);
    }
}
