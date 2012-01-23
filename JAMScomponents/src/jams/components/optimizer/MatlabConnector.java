/*
 * MatlabConnector.java
 *
 * Created on 10. M^rz 2008, 11:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

/**
 *
 * @author Christian Fischer
 */    
import jams.tools.JAMSTools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import jams.data.*;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

@JAMSComponentDescription(
        title="Branch and Bound Optimizer",
        author="Christian Fischer",
        description="under construction!!"
        )
public class MatlabConnector extends SOOptimizer{
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
           
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "objective function name"
            )
            public JAMSString effMethodName;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the prediction series"
            )
            public JAMSDouble effValue;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization mode"
            )
            public JAMSInteger mode;
          
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum runs"
            )
            public JAMSInteger maxn;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling/disabling this sampler"
            )
            public JAMSBoolean enable;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString outputFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString regularSampleFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString SampleDumpFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString ParameterFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString EffFileName;

    BufferedWriter writer = null;

    /** Creates a new instance of MatlabConnector */
    public MatlabConnector() {
    }
    
    public void init(){
        super.init();
                
        try {
            writer = new BufferedWriter(new FileWriter(this.dirName + "/" + SampleDumpFileName.getValue()));
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }
    
    public void procedure() {
        
        //read parameterfile
        try{
        BufferedReader param_reader = new BufferedReader(new FileReader(this.dirName + "/" + ParameterFileName.getValue()));
        double x[] = new double[n];
        for (int i=0;i<n;i++)
            x[i] = Double.parseDouble(param_reader.readLine());
        
        double eff = this.funct(x);
        
        BufferedWriter eff_writer = new BufferedWriter(new FileWriter(this.dirName + "/" + EffFileName.getValue()));
        //write eff file
        eff_writer.write(Double.toString(eff));
        eff_writer.flush();
        System.out.println("TESSSSSSSSSSSSSSSSST2222");
        writer.close();
        eff_writer.close();
        param_reader.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    
}
