package jams.worldwind.ui.view;

import jams.worldwind.data.DataTransfer3D;
import jams.worldwind.test.RandomNumbers;
import jams.worldwind.ui.IntervallSettingsPanel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JList;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class IntervallSettingsView {

    private JFrame intervallSettingsFrame;
    private IntervallSettingsPanel intervallSettingsPanel;
    
    private final DataTransfer3D dataValues;
    private final String[] attributes;
    
    public IntervallSettingsView(DataTransfer3D d, String[] attributes) {
        this.dataValues = d;
        this.attributes = attributes;
        createGUI();
    }
    
    public IntervallSettingsView(String[] attributes, List<Double> values) {
        this.dataValues = null;
        this.attributes = attributes;
        createGUI();
    }
    
    private void  createGUI() {
        this.intervallSettingsFrame = new JFrame("INTERVALL/CLASSIFIER FRAME");
        this.intervallSettingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.intervallSettingsPanel = new IntervallSettingsPanel(dataValues,this.attributes);
        
        this.intervallSettingsFrame.add(this.intervallSettingsPanel);
        this.intervallSettingsFrame.pack();
        //this.intervallSettingsFrame.setBounds(new Dimension(400, 800));
        //this.intervallSettingsFrame.setMaximizedBounds(new Rectangle(400, 800));
        this.intervallSettingsFrame.setVisible(true);
    }
    
    public static void main(String[] args) {
        RandomNumbers rn = new RandomNumbers(0, 100, 1000);
        new IntervallSettingsView(new String[]{"precip","tmean"},rn.getDoubleValues());
    }
}
