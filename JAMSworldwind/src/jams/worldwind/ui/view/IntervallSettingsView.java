package jams.worldwind.ui.view;

import jams.worldwind.test.RandomNumbers;
import jams.worldwind.ui.IntervallSettingsPanel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class IntervallSettingsView {

    private JFrame intervallSettingsFrame;
    private IntervallSettingsPanel intervallSettingsPanel;
    
    private final List dataValues;
    
    public IntervallSettingsView(List values) {
        this.dataValues = values;
        createGUI();
    }
    
    private void  createGUI() {
        this.intervallSettingsFrame = new JFrame("INTERVALL/CLASSIFIER FRAME");
        this.intervallSettingsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.intervallSettingsPanel = new IntervallSettingsPanel(dataValues);
        
        
        this.intervallSettingsFrame.add(this.intervallSettingsPanel);
        
        this.intervallSettingsFrame.pack();
        //this.intervallSettingsFrame.setBounds(new Dimension(400, 800));
        //this.intervallSettingsFrame.setMaximizedBounds(new Rectangle(400, 800));
        this.intervallSettingsFrame.setVisible(true);
    }
    
    public static void main(String[] args) {
        RandomNumbers rn = new RandomNumbers(0, 100, 1000);
        new IntervallSettingsView(rn.getDoubleValues());
    }
}
