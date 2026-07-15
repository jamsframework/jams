package optas.gui.wizard;

import optas.optimizer.management.NumericOptimizerParameter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

class NumericFocusListener implements FocusListener {

    public static final int MODE_LOWERBOUND = 0;
    public static final int MODE_UPPERBOUND = 1;
    public static final int MODE_STARTVALUE = 2;
    public static final int MODE_PARAMETERVALUE = 3;

    NumericFocusListener() {
    }

    public void focusLost(FocusEvent e) {
        JTextField src = (JTextField) e.getSource();
        Parameter p = (Parameter) src.getClientProperty("parameter");
        Integer mode = (Integer) src.getClientProperty("mode");
        try {
            //try to convert text
            double value = Double.parseDouble(src.getText());
            if (mode.intValue() == MODE_PARAMETERVALUE) {
            }
            switch (mode.intValue()) {
                case MODE_LOWERBOUND:
                    p.setLowerBound(Double.parseDouble(src.getText()));
                    break;
                case MODE_UPPERBOUND:
                    p.setUpperBound(Double.parseDouble(src.getText()));
                    break;
                case MODE_STARTVALUE:
                    p.setStartValue(new double[]{Double.parseDouble(src.getText())});
                    break;
                case MODE_PARAMETERVALUE:
                    NumericOptimizerParameter p2 = (NumericOptimizerParameter) src.getClientProperty("property");
                    p2.setValue(Double.parseDouble(src.getText()));
                    NumericOptimizerParameter param = (NumericOptimizerParameter) src.getClientProperty("property");
                    if (value < param.getLowerBound() || value > param.getUpperBound()) {
                        throw new NumberFormatException();
                    }
                    break;
            }
        } catch (NumberFormatException nfe) {
            switch (mode.intValue()) {
                case MODE_LOWERBOUND:
                    src.setText(Double.toString(p.getLowerBound()));
                    break;
                case MODE_UPPERBOUND:
                    src.setText(Double.toString(p.getUpperBound()));
                    break;
                case MODE_STARTVALUE:
                    if (p.getStartValue().length>0) {
                        src.setText(Double.toString(p.getStartValue()[0]));
                    } else {
                        src.setText("");
                    }
                    break;
                case MODE_PARAMETERVALUE:
                    src.setText(Double.toString(((NumericOptimizerParameter) src.getClientProperty("property")).getValue()));
                    break;
            }
        }
    }

    public void focusGained(FocusEvent e) {
    }
}
