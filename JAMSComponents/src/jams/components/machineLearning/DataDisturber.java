package jams.components.machineLearning;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;
import java.util.Random;
import Jama.*;
import Jama.Matrix;
import Jama.LUDecomposition;
import Jama.util.Maths;

/**
 *
 * @author Christian(web)
 */
public class DataDisturber extends JAMSContext {
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger startComponent;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger endComponent;
           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity Data;
            
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDouble NoiseAmpitude;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDouble ErrorAmpitude;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSBoolean AllowNegative;
     
    public DataDisturber() {
	 
    }
     
    public void init() {  
     
    }
     
          
    public void run() {  
	double data[][] = null;
				
	try {
	     data = (double[][])Data.getObject("data");	     
	}
	catch(Exception e) {
	    System.out.println("Konnte InputData nicht finden" + e.toString());
	}
			
	System.out.println("Optimiere Trainingsdaten!");
	
	int N = data.length;
	double A1 = this.NoiseAmpitude.getValue();
	double A2 = this.ErrorAmpitude.getValue();
	
	Random r = new Random();
	
	for (int j=0;j<N;j++) {
	    for (int k=this.startComponent.getValue()-1;k<=this.endComponent.getValue()-1;k++) {
		double z1 = A1*(2.0*r.nextDouble() - 1.0);
		double z2 = A2*(2.0*r.nextDouble() - 1.0);
		
		data[j][k] = data[j][k]*(1.0 + z1) + z2;
		
		if (!this.AllowNegative.getValue()) {
		    if (data[j][k] < 0.0) {
			data[j][k] = -data[j][k];
		    }
		}
	    }
	}	
    }
}