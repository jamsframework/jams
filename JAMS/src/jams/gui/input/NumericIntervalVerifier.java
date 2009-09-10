/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.gui.input;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author hbusch
 */

class NumericIntervalVerifier extends InputVerifier {

    double lower, upper;
    int result;

    public NumericIntervalVerifier(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public boolean verify(JComponent input) {

        double value;

        try {
            value = Double.parseDouble(((JTextField) input).getText());
            if ((value >= lower) && (value <= upper)) {
                result = InputComponent.INPUT_OK;
                return true;
            } else {
                result = InputComponent.INPUT_OUT_OF_RANGE;
                return false;
            }
        } catch (NumberFormatException nfe) {
            result = InputComponent.INPUT_WRONG_FORMAT;
        }
        return false;
    }
}
