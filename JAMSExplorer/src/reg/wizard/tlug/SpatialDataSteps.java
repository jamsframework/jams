/*
 * this class defines the steps for spatial data
 */

package reg.wizard.tlug;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.wizard.WizardController;
import reg.wizard.tlug.panels.*;

/**
 *
 * @author hbusch
 */
public class SpatialDataSteps  extends DataStepPanelProvider {

    public SpatialDataSteps() {
        super (
            new String[] { "baseData", "regMethod", "finishIt"},
            new String[] { "Datenbasis bestimmen", "Parameter auswählen", "Konfiguration speichern" });
    }

    protected JComponent createPanel(WizardController controller, String id, Map settings) {
        switch (indexOfStep(id)) {
            case 0 :
                return new BaseDataPanel (controller, settings);
            case 1 :
                return new RegMethodPanel (controller, settings);
            case 2 :
                return new AddCompsPanel (controller, settings);
            default :
                throw new IllegalArgumentException (id);
        }
    }

}
