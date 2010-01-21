/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.wizard.tlug;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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

        String propertyFileName = (String) settings.get(DataDecisionPanel.KEY_CONFIG_FILENAME);
        if (!StringUtils.isNullOrEmpty(propertyFileName)) {
            Properties properties = new Properties();

            Set keys = settings.keySet();
            Object value;
            String theType;
            for (Object key : keys) {
                value = settings.get(key);
                theType = value.getClass().getSimpleName();
                if (theType.equals("String"))
                    properties.put(key, settings.get(key));
            }
            //properties.putAll(settings); // does not work for float !!??

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
