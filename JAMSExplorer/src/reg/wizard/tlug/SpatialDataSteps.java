/*
 * this class defines the steps for spatial data
 */

package reg.wizard.tlug;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import org.h2.util.StringUtils;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;
import reg.wizard.tlug.panels.AddCompsPanel;
import reg.wizard.tlug.panels.BaseDataPanel;
import reg.wizard.tlug.panels.DataDecisionPanel;
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

    protected Object finish (Map settings) throws WizardException {

        Set keys = settings.keySet();
        for (Object key : keys) {
            System.out.println(key + "-->>" + settings.get(key));
        }

        String propertyFileName = (String) settings.get(DataDecisionPanel.KEY_CONFIG_FILENAME);
        if (!StringUtils.isNullOrEmpty(propertyFileName)) {
            Properties properties = new Properties();
            properties.putAll(settings);
            try {
                System.out.println("storing properties to  " + propertyFileName);
                properties.store(new FileOutputStream(propertyFileName), "wizard configuration");
            } catch (Exception e) {
                System.out.println("error at storing properties: " + e.getMessage());
            }
        }
        return null;
    }


}
