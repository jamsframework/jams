/*
 * this class defines the steps for spatial data
 */

package reg.wizard.tlug;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import reg.wizard.tlug.panels.AddCompsPanel;
import reg.wizard.tlug.panels.BaseDataPanel;
import reg.wizard.tlug.panels.RegMethodPanel;

/**
 *
 * @author hbusch
 */
public class SpatialDataSteps  extends WizardPanelProvider {

    public SpatialDataSteps() {
        super (
            new String[] { "baseData", "regMethod", "addComps" },
            new String[] { "Datenbasis bestimmen", "Regionalisierungsverfahren auswählen", "Zusatzberechnungen" });
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
