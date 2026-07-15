/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import jams.explorer.ensembles.implementation.ClimateEnsemble;
import jams.explorer.ensembles.implementation.ClimateModel;
import jams.explorer.gui.CancelableWorkerDlg;
import jams.gui.WorkerDlg;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

/**
 *
 * @author christian
 */
abstract public class AbstractClimateDataTab extends AbstractDataTab{
    static final Logger logger = Logger.getLogger(AbstractClimateDataTab.class.getName());
    {
        EnsembleControlPanel.registerLogHandler(logger);
    }
    ClimateEnsemble ensemble;
    ClimateModel currentModel;
    String output;
    int n = 0;
    
    //static such that user have to choose preferred directory only once
    static JFileChooser fileChooser = new JFileChooser();
    
    public AbstractClimateDataTab(String name) {
        super(name);        
    }

    public void setOutput(String output) {
        this.output = output;
        setChanged();
    }

    public void setClimateEnsemble(ClimateEnsemble ensemble) {
        this.ensemble = ensemble;
        n = ensemble.getSize();
        setChanged();
    }

    public ClimateEnsemble getClimateEnsemble() {
        return ensemble;
    }
    
    abstract protected void calculate();
    
    @Override
    public void refresh() {
        if (!isShowing() || !isChanged())
            return;
        
        if (ensemble == null || output == null) {
            return;
        }
        
        WorkerDlg worker = new CancelableWorkerDlg(null, "I am busy");
        worker.setTask(new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                try{
                    calculate();
                }catch(Throwable t){
                    logger.log(Level.SEVERE, "Sorry, I am unable to refesh the climate data tab", t);
                }
                return null;
            }
        });
        worker.execute();
    }

}
