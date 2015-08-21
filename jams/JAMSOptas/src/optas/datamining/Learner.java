
/*
 * NNLearner.java
 *
 * Created on 10. April 2007, 14:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package optas.datamining;

import jams.data.Attribute;
import java.util.*;
import jams.data.JAMSEntity;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
/**
 *
 * @author Christian(web)
 */
public class Learner extends JAMSComponent{   
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Entity trainData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity validationData;
    
    protected int DataLength = 0;
    protected int TrainLength = 0;
    
    protected double data[][];
    protected double result[];
    
    double normed_data[][];
	    
    protected double min[],max[],base[];
    protected double pmin,pmax,pbase;
    
    Random generator = new Random();
    
    public boolean normalizeData;
    
    /** Creates a new instance of NNLearner */
    public Learner() {
    }
    
    public void setTrainingData(double data[][],double result[]) {
	TrainLength = data.length;
	DataLength = data[0].length;	
	
	this.data = data;
	this.result = result;
	
	min = new double[DataLength];
	max = new double[DataLength];
	base = new double[DataLength];
	
	for (int i = 0; i < DataLength; i++) {
	    min[i] = Double.POSITIVE_INFINITY;
	    max[i] = Double.NEGATIVE_INFINITY;
	    
	    for (int j = 0; j < TrainLength; j++) {
		if (data[j][i] < min[i])
		    min[i] = data[j][i];
		if (data[j][i] > max[i])
		    max[i] = data[j][i];
	    }	    
	    base[i] = (min[i] + max[i]) / 2.0;
	    if (min[i] == max[i]) {
		max[i] = min[i] + 1.0;
	    }
	}
	
	for (int i = 0; i < DataLength; i++) {
	    pmin = Double.POSITIVE_INFINITY;
	    pmax = Double.NEGATIVE_INFINITY;
	    
	    for (int j = 0; j < TrainLength; j++) {
		if (data[j][i] < pmin)
		    pmin = data[j][i];
		if (data[j][i] > pmax)
		    pmax = data[j][i];
	    }	    
	    pbase = (pmin + pmax) / 2.0;	    
	}	
    }
    
    public double[] normalize(double x[]) {
	double result[] = new double[x.length];
	for (int i=0;i<x.length;i++) {
	    result[i] = 2.0*(x[i] - base[i]) / (max[i] - min[i]);
	}
	return result;
    }
    
    public void normalizeAll() {
	normed_data = new double[data.length][data[0].length];
	for (int i=0;i<data.length;i++) {
	    normed_data[i] = normalize(data[i]);
	}
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException { 
	double t1[][] = (double[][])this.trainData.getObject("data");
	double t2[] = (double[])this.trainData.getObject("predict");
	setTrainingData(t1,t2);
	normalizeAll();
    }
}

