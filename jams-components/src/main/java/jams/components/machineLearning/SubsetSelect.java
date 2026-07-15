package jams.components.machineLearning;

import jams.model.*;
import jams.data.*;
import java.util.*;
import java.util.Random;

/**
 *
 * @author Christian(web)
 */
public class SubsetSelect extends JAMSContext {
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Integer n;
           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Entity InputData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Entity OutputData;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Boolean active;
     
    public SubsetSelect() {
	 
    }
     
    public void init() {  
     
    }
     
          
    public void run() {  
	double data[][] = null;
	double predict[] = null;
	
	HashSet<Double> HashFilter = new HashSet<Double>();
	
	try {
	     data = (double[][])InputData.getObject("data");
	     predict = (double[])InputData.getObject("predict");
	}
	catch(Exception e) {
            this.getModel().getRuntime().sendHalt("Could not find input data for subset selection" + e.toString());
	    return;
	}
	
	if (active.getValue() == false) {
	    OutputData.setObject("data",data);
	    OutputData.setObject("predict",predict);
	    return;
	}
	this.getModel().getRuntime().sendInfoMsg("Optimiere Trainingsdaten!");

        if ( n.getValue() <= 0){
            this.getModel().getRuntime().sendHalt("subsetsize less or equal zero!");
	    return; 
        }
        
	int Ngoal = n.getValue();
	int N0 = data.length;
        if (Ngoal > N0){
            Ngoal = N0;
        }
	int M = data[0].length;
		
	int set[] = new int[Ngoal];
	
	double variance[] = new double[N0];
	double hash_value[] = new double[N0];
	boolean inSet[] = new boolean[N0];
	for (int j=0;j<N0;j++) {
	    variance[j] = 0.0;
	    inSet[j] = false;
	    hash_value[j] = 0;
	    for (int k=0;k<M;k++) {
		hash_value[j] += data[j][k];
	    }	    
	}
	inSet[0] = true;
	set[0] = 0;
	
	for (int i=1;i<Ngoal;i++) {
	    int bestIndex = -1;
	    double bestValue = -1.0;
	    
	    for (int j=0;j<N0;j++) {		
		if (inSet[j])
		    continue;
				
		for (int l=0;l<M;l++) {
		    variance[j] += (data[j][l] - data[set[i-1]][l])*(data[j][l] - data[set[i-1]][l]);
		}
		if (variance[j] > bestValue) {
		    bestValue = variance[j];
		    bestIndex = j;
		}
	    }
	    if (bestValue == -1) {
                this.getModel().getRuntime().sendHalt("subset size large than number of available datasets");
	    }
	    inSet[bestIndex] = true;
	    HashFilter.add(new Double(hash_value[bestIndex]));
	    set[i] = bestIndex;	 	    
	    
	    for (int k=1;k<N0;k++) {
		if (Math.abs(hash_value[k] - hash_value[bestIndex])<0.0000001 ) {
		    inSet[k] = true;
		}
	    }
	}
	
	double optimizedata[][] = new double[Ngoal][];
	double optimizepredict[] = new double[Ngoal];
	
	//now shake it
	Random r = new Random();
	for (int i=0;i<10;i++) {
	    for (int j=0;j<Ngoal;j++) {
		int Shuffle = r.nextInt(Ngoal);
		int tmp = set[j];
		set[j] = set[Shuffle];
		set[Shuffle] = tmp;
	    }
	}
	
	for (int i=0;i<Ngoal;i++) {
	    optimizedata[i] = data[set[i]];
	    optimizepredict[i] = predict[set[i]];
	}	
	OutputData.setObject("data",optimizedata);
	OutputData.setObject("predict",optimizepredict);
	
        this.getModel().getRuntime().sendInfoMsg("finished subset selection");	
    }
}
