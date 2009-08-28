/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.wizard.tlug;

import java.util.Map;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import reg.wizard.tlug.panels.DataDecisionPanel;


/**
 *
 * @author hbusch
 */
public class ExplorerWizard extends WizardBranchController {

    public ExplorerWizard(  ) {
        super( new InitialStep(  ) );
    }

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
