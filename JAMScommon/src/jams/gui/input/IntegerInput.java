/*
 * IntegerInput.java
 * Created on 7. September 2006, 10:47
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
package jams.gui.input;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author S. Kralisch
 */
public class IntegerInput extends JPanel implements InputComponent {

    private JTextField text = new JTextField();

    private ValueChangeListener l;

    private String boundaryString = null;

    public IntegerInput() {
        super();
        setRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
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

    public void setRange(double lower, double upper) {
        this.boundaryString = "[" + (long) lower + "..." + (long) upper + "]";
        this.setInputVerifier(new IntegerIntervalVerifier((long) lower, (long) upper));
    }

    public boolean verify() {
        return this.getInputVerifier().verify(text);
    }

    public int getErrorCode() {
        return ((IntegerIntervalVerifier) this.getInputVerifier()).result;
    }

    public void setLength(int length) {
    }

    public void addValueChangeListener(ValueChangeListener l) {
        this.l = l;
        this.text.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                IntegerInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                IntegerInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                IntegerInput.this.l.valueChanged();
            }
        });
    }
    private Color oldColor;

    public void setMarked(boolean marked) {
        if (marked == true) {
            oldColor = text.getBackground();
            text.setBackground(new Color(255, 0, 0));
        } else {
            text.setBackground(oldColor);
        }
    }

    public void setHelpText(String text) {
        if (this.boundaryString != null) {
            text = this.boundaryString + "<br>" + text;
        }
        text = "<html>" + text + "</html>";
        getComponent().setToolTipText(text);
    }

    class IntegerIntervalVerifier extends InputVerifier {

        long lower, upper;

        int result;

        public IntegerIntervalVerifier(long lower, long upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public boolean verify(JComponent input) {

            int result;
            long value;

            try {
                value = Long.parseLong(((JTextField) input).getText());
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
}
