package jams.worldwind.ui.view;

import jams.tools.JAMSTools;
import jams.worldwind.data.DataTransfer3D;
import jams.worldwind.data.RandomNumbers;
import jams.worldwind.ui.ColorRamp;
import jams.worldwind.ui.IntervallSettingsPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
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

    /*
    public IntervallSettingsView(String[] attributes, List<Double> values, int selected) {
        this.dataValues = null;
        this.attributes = attributes;
        createGUI(selected);
    }
*/
    private void createGUI() {
        this.intervallSettingsFrame = new JFrame("Data Classification");
        this.intervallSettingsFrame.setIconImages(JAMSTools.getJAMSIcons());        
        this.intervallSettingsPanel = new IntervallSettingsPanel(this, dataValues, this.attributes);

        this.intervallSettingsFrame.add(this.intervallSettingsPanel);
        this.intervallSettingsFrame.pack();
        this.intervallSettingsFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                intervallSettingsFrame.setEnabled(true);
            }
        });
        this.intervallSettingsFrame.setVisible(true);
    }

    public void dispose() {
        this.intervallSettingsFrame.setVisible(false);
        this.intervallSettingsFrame.dispose();
    }

    public void show() {
        intervallSettingsFrame.setVisible(true);
        intervallSettingsFrame.toFront();
    }
    
    public void hide() {
        intervallSettingsFrame.setVisible(false);
    }
    
    public void setIntervallAndColorRamp(List<Double> intervall, ColorRamp colorRamp) {
        this.intervallSettingsPanel.setIntervallAndColorRamp(intervall,colorRamp);
    }
    
    public int getSelectedAttributeIndex() {
        return this.intervallSettingsPanel.getSelectedAttributeIndex();
    }
    
    public static void main(String[] args) {
        RandomNumbers rn = new RandomNumbers(0, 100, 1000);
        DataTransfer3D d = readTestObjectFromDisk();
        //new IntervallSettingsView(new String[]{"precip", "tmean"}, rn.getDoubleValues());
        if (d != null) {
            new IntervallSettingsView(d, d.getSortedAttributes());
        }
    }

    public static DataTransfer3D readTestObjectFromDisk() {
        DataTransfer3D data = null;
        try {
            FileInputStream fin = new FileInputStream("../../JAMSworldwind/src/jams/worldwind/test/DataTransfer3DTestData.ser");
            ObjectInputStream ois = new ObjectInputStream(fin);
            data = (DataTransfer3D) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}