/*
 * FloatInput.java
 * Created on 7. September 2006, 10:47
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.jams.gui.input;

import java.awt.BorderLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.unijena.jams.gui.*;

/**
 *
 * @author S. Kralisch
 */
public class FloatInput extends JPanel implements InputComponent {

    private static final long serialVersionUID = 2424203601132061595L;
    private JTextField text = new JTextField();
    private ValueChangeListener l;

    public FloatInput() {
        super();
        setRange((-1 * Double.MAX_VALUE) + 1, Double.MAX_VALUE);
        setLayout(new BorderLayout());
        add(text, BorderLayout.WEST);
    }

    public String getValue() {
        return text.getText();
    }

    public void setValue(String value) {
        text.setText(value);
    }

    public JComponent getComponent() {
        return text;
    }

    class NumericIntervalVerifier extends InputVerifier {

        double lower, upper;
        int result;

        public NumericIntervalVerifier(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public boolean verify(JComponent input) {

            double value;

            try {
                value = Double.parseDouble(((JTextField) input).getText());
                if ((value >= lower) && (value <= upper)) {
                    result = INPUT_OK;
                    return true;
                } else {
                    result = INPUT_OUT_OF_RANGE;
                    return false;
                }
            } catch (NumberFormatException nfe) {
                result = INPUT_WRONG_FORMAT;
            }
            return false;
        }
    }

    public void setRange(double lower, double upper) {
        this.setInputVerifier(new NumericIntervalVerifier(lower, upper));
    }

    public boolean verify() {
        return this.getInputVerifier().verify(text);
    }

    public int getErrorCode() {
        return ((NumericIntervalVerifier) this.getInputVerifier()).result;
    }

    public void setLength(int length) {
    }

    public void addValueChangeListener(ValueChangeListener l) {
        this.l = l;
        this.text.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                FloatInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                FloatInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                FloatInput.this.l.valueChanged();
            }
        });
    }
}
