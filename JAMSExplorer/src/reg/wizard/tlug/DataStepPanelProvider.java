/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.wizard.tlug;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;
import org.h2.util.StringUtils;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;
import reg.wizard.tlug.panels.DataDecisionPanel;

/**
 *
 * @author hbusch
 */
public abstract class DataStepPanelProvider  extends WizardPanelProvider {

    public DataStepPanelProvider( String[] names, String[] descriptions) {
            super (names, descriptions);
    }


    @Override
    protected Object finish (Map settings) throws WizardException {

        System.out.println("DataStepPanelProvider.finish");
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
        return settings;
    }

}
