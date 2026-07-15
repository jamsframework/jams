package optas.gui.wizard;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;

class NumericKeyListener implements KeyListener {

    public static final int MODE_LOWERBOUND = 0;
    public static final int MODE_UPPERBOUND = 1;
    public static final int MODE_STARTVALUE = 2;
    public static final int MODE_PARAMETERVALUE = 3;

    NumericKeyListener() {
    }

    public Integer getModeLowerBound() {
        return new Integer(MODE_LOWERBOUND);
    }

    public Integer getModeUpperBound() {
        return new Integer(MODE_UPPERBOUND);
    }

    public Integer getModeStartValue() {
        return new Integer(MODE_STARTVALUE);
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if (!java.lang.Character.isDigit(keyChar) && keyChar != '.') {
            e.consume();
        }
        JTextField src = (JTextField) e.getSource();
        /*
        Parameter p = (Parameter) src.getClientProperty("parameter");
        Integer mode = (Integer) src.getClientProperty("mode");
        try {
        switch (mode.intValue()) {
        case MODE_LOWERBOUND:
        p.lowerBound = Double.parseDouble(src.getText() + keyChar);
        break;
        case MODE_UPPERBOUND:
        p.upperBound = Double.parseDouble(src.getText() + keyChar);
        break;
        case MODE_STARTVALUE:
        p.startValue = Double.parseDouble(src.getText() + keyChar);
        p.startValueValid = true;
        break;
        case MODE_PARAMETERVALUE:
        NumericOptimizerParameter p2 = (NumericOptimizerParameter) src.getClientProperty("property");
        p2.value = Double.parseDouble(src.getText() + keyChar);
        break;
        }
        } catch (NumberFormatException nfe) {
        e.consume();
        }*/
    }
}
