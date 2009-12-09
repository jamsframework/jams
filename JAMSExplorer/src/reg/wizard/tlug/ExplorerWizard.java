/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.wizard.tlug;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import reg.wizard.tlug.panels.BaseDataPanel;
import reg.wizard.tlug.panels.DataDecisionPanel;
import reg.wizard.tlug.panels.RegMethodPanel;


/**
 *
 * @author hbusch
 */
public class ExplorerWizard extends WizardBranchController {

    public ExplorerWizard() {
        super( new InitialStep() );
    }

    /**
     * mapping of  wizardKey -> key(s) used in model
     **/
    public static final Map<String,String[]> KEY_MODEL_MAPPING = createMapping();
    private static Map<String, String[]> createMapping () {
        Map<String, String[]> resultMap = new HashMap<String, String[]>();

        resultMap.put(BaseDataPanel.KEY_INTERVAL, new String[] {"Interpolation.timeInterval"});
        resultMap.put(RegMethodPanel.KEY_SCHWELLENWERT,
                new String[] {  "Regionaliser.rsqThreshold"
                             }
        );
        resultMap.put(RegMethodPanel.KEY_GEWICHTUNG,
                new String[] {  "Weights.pidw"
                             }
        );
        resultMap.put(RegMethodPanel.KEY_STATION,
                new String[] {  "Regionaliser.nidw"
                             }
        );

        return Collections.unmodifiableMap(resultMap);
    }

    @Override
    protected WizardPanelProvider getPanelProviderForStep(String step, Map collectedData) {
        //There's only one branch point, so we don't need to test the
        //value of step
        Object data = collectedData.get(DataDecisionPanel.KEY_DATA);
        if (DataDecisionPanel.VALUE_SPATIAL.equals(data)) {
            return getSpatialDataSteps();
        } else if (DataDecisionPanel.VALUE_STATION.equals(data)) {
            return getStationDataSteps();
        } else {
            return null;
        }
    }

    private WizardPanelProvider getStationDataSteps() {
        if (stationDataSteps == null) {
            stationDataSteps = new StationDataSteps();
        }
        return stationDataSteps;
    }

    private WizardPanelProvider getSpatialDataSteps() {
        if (spatialDataSteps == null) {
            spatialDataSteps = new SpatialDataSteps();
        }
        return spatialDataSteps;
    }

    private SpatialDataSteps spatialDataSteps = null;
    private StationDataSteps stationDataSteps = null;

}
