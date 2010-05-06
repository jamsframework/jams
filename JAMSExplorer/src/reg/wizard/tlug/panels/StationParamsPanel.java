/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StationParamsPanel.java
 *
 * Created on 28.08.2009, 15:07:22
 */

package reg.wizard.tlug.panels;

import jams.data.JAMSTimeInterval;
import jams.gui.input.InputComponent;
import jams.gui.input.InputComponentFactory;
import jams.gui.input.TimeintervalInput;
import jams.gui.input.ValueChangeListener;
import jams.gui.tools.GUIHelper;
import java.awt.GridBagLayout;
import java.util.Map;
import javax.swing.JRadioButton;
import org.h2.util.StringUtils;
import org.netbeans.spi.wizard.WizardController;

/**
 *
 * @author hbusch
 */
public class StationParamsPanel extends javax.swing.JPanel {

    private final WizardController controller;
    private final Map wizardData;

    public static final String KEY_COMPUTATION = "computation";
    public static final String VALUE_EVAPOR = "evaporation";
    public static final String VALUE_RADIATION = "radiation";
    public static final String VALUE_AHUM = "ahum";
    public static final String VALUE_PRECIPCORR = "precipCorrection";
    public static final String KEY_INTERVAL = "j2kInterval";

    // all field contents
    private String r_computation = null;
    private String r_interval = null;

    /** Creates new form  */
    public StationParamsPanel(WizardController controller, Map wizardData) {

        this.controller = controller;
        this.wizardData = wizardData;

        initComponents();

        // group buttons
        buttonGroup1.add(jRadioEvapor);
        buttonGroup1.add(jRadioRadiation);
        buttonGroup1.add(jRadioPrecipCorr);
        buttonGroup1.add(jRadioHumidity);

        jRadioEvapor.putClientProperty(KEY_COMPUTATION, VALUE_EVAPOR);
        jRadioRadiation.putClientProperty(KEY_COMPUTATION, VALUE_RADIATION);
        jRadioPrecipCorr.putClientProperty(KEY_COMPUTATION, VALUE_PRECIPCORR);
        jRadioHumidity.putClientProperty(KEY_COMPUTATION, VALUE_AHUM);

        initFromWizardData();
        checkProblems();

    }

    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jRadioEvapor = new javax.swing.JRadioButton();
        jRadioRadiation = new javax.swing.JRadioButton();
        jRadioHumidity = new javax.swing.JRadioButton();
        jRadioPrecipCorr = new javax.swing.JRadioButton();

        jRadioEvapor.setText("Verdunstung");
        jRadioEvapor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioEvaporActionPerformed(evt);
            }
        });

        jRadioRadiation.setText("Strahlungsberechnung");
        jRadioRadiation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioRadiationActionPerformed(evt);
            }
        });

        jRadioHumidity.setText("absolute Luftfeuchte");
        jRadioHumidity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioHumidityActionPerformed(evt);
            }
        });

        jRadioPrecipCorr.setText("Niederschlagskorrektur");
        jRadioPrecipCorr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioPrecipCorrActionPerformed(evt);
            }
        });
        jComputationLabel = new javax.swing.JLabel();
        jComputationLabel.setText("Berechnung von");

        jIntervalLabel = new javax.swing.JLabel();
        jIntervalLabel.setText("Zeitintervall");
        jIntervall = InputComponentFactory.createInputComponent(JAMSTimeInterval.class, false);
        jIntervall.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChanged() {
                try {
                    checkProblems();
                } catch (Exception e) {
                    // do nothing
                }
            }
        });

        // defaults
        r_interval = "1990-01-01 7:30 2000-12-31 7:30 6 1";


        // set the layout and positions
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

        GUIHelper.addGBComponent(this, layout, jComputationLabel, 2, 1, 2, 1, 0, 0);
        GUIHelper.addGBComponent(this, layout, jRadioHumidity, 2, 2, 2, 1, 0, 0);
        GUIHelper.addGBComponent(this, layout, jRadioRadiation, 2, 3, 2, 1, 0, 0);
        GUIHelper.addGBComponent(this, layout, jRadioEvapor, 2, 4, 2, 1, 0, 0);
        GUIHelper.addGBComponent(this, layout, jRadioPrecipCorr, 2, 5, 2, 1, 0, 0);

        GUIHelper.addGBComponent(this, layout, jIntervalLabel, 1, 7, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, layout, jIntervall.getComponent(), 2, 7, 3, 3, 0, 0);

    }

    private void jRadioRadiationActionPerformed(java.awt.event.ActionEvent evt) {
        computationSelected(evt);
    }

    private void jRadioEvaporActionPerformed(java.awt.event.ActionEvent evt) {
        computationSelected(evt);
    }

    private void jRadioHumidityActionPerformed(java.awt.event.ActionEvent evt) {
        computationSelected(evt);
    }

    private void jRadioPrecipCorrActionPerformed(java.awt.event.ActionEvent evt) {
        computationSelected(evt);
    }

    private void computationSelected(java.awt.event.ActionEvent evt) {

        Object val = ((JRadioButton) evt.getSource()).getClientProperty(KEY_COMPUTATION);
        r_computation = (String) val;
        checkProblems();
    }


    /**
     * init display data from wizard data
     */
    private void initFromWizardData() {

        String computation = (String) wizardData.get(KEY_COMPUTATION);
        if (!StringUtils.isNullOrEmpty(computation)) {
            r_computation = computation;
            if (computation.equals(VALUE_EVAPOR))
                jRadioEvapor.setSelected(true);
            if (computation.equals(VALUE_RADIATION))
                jRadioRadiation.setSelected(true);
            if (computation.equals(VALUE_AHUM))
                jRadioHumidity.setSelected(true);
            if (computation.equals(VALUE_PRECIPCORR))
                jRadioPrecipCorr.setSelected(true);
        }
        String interval = (String) wizardData.get(KEY_INTERVAL);
        if (!StringUtils.isNullOrEmpty(interval)) {
            r_interval = interval;
        }
        if (!StringUtils.isNullOrEmpty(r_interval)) {
            ((TimeintervalInput) jIntervall).setValue(r_interval);
        }

    }

    private void checkProblems() {

        controller.setProblem(null);
        if (StringUtils.isNullOrEmpty(r_computation)) {
            controller.setProblem("Bitte Berechnungsart auswählen.");
        } else {
            wizardData.put(KEY_COMPUTATION, r_computation);

            int errorCode = jIntervall.getErrorCode();
            if (errorCode > 0) {
                controller.setProblem("Bitte Zeitintervall auswählen.");

            } else {
                r_interval = jIntervall.getValue();
                wizardData.put(KEY_INTERVAL, r_interval);
            }
        }
    }


    // Variables declaration
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton jRadioEvapor;
    private javax.swing.JRadioButton jRadioHumidity;
    private javax.swing.JRadioButton jRadioPrecipCorr;
    private javax.swing.JRadioButton jRadioRadiation;
    private InputComponent jIntervall;
    private javax.swing.JLabel jIntervalLabel;
    private javax.swing.JLabel jComputationLabel;
    // End of variables declaration

}
