/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.tools;

import java.awt.Color;
import javax.swing.JTextField;

/**
 *
 * @author christian
 */
public class Tools {
    public static double readField(JTextField f, double defaultValue){
        double value = defaultValue;
    
        try{
            if (f.getText().isEmpty()){
                f.setText(Double.toString(defaultValue));
            }else{
                value = Double.parseDouble(f.getText());
            }
            f.setOpaque(true);
        }catch(NumberFormatException nfe){
            f.setOpaque(false);
            f.setBackground(Color.red);
        }
        return value;
    }
}
