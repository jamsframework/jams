/*
 * DataLoader.java
 *
 * Created on 3. Juli 2007, 12:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning;

import jams.model.*;
import jams.data.*;
import java.util.*;
import java.io.*;
import jams.JAMS;
import jams.JAMSTools;
/**
 *
 * @author Christian(web)
 */
public class DataLoader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Precip Data"
            )
             public JAMSString datafile;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
             public JAMSInteger ExampleLength;
                                              
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger relevantTime;
                                    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity Data;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger dataShift;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString Gaps;
    
    public DataLoader() {        
    }
    
    public void init() throws JAMSEntity.NoSuchAttributeException {  
	BufferedReader reader = null;
	HashMap<Integer, double[]> rawData = new HashMap<Integer,double[]>();
	HashMap<Integer, double[]> rawPredict = new HashMap<Integer,double[]>();
	Vector<Integer> excluded = new Vector<Integer>();

        if (Gaps != null){
            StringTokenizer GapTokenizer = new StringTokenizer(Gaps.getValue(),";");
            while(GapTokenizer.hasMoreElements()){
                excluded.add(new Integer(GapTokenizer.nextToken()));
            }
        }
	int ExamplLength = ExampleLength.getValue();
	int numOfExampl  = 0;
	int RelevantTime = this.relevantTime.getValue();
	
        if (ExamplLength <= 0){
            this.getModel().getRuntime().sendHalt("inputDimension is less or equal zero!");
            return;
        }
        if (RelevantTime <= 0){
            this.getModel().getRuntime().sendHalt("Relevant timesteps is less or equal zero!");
            return;
        }
        
        try {
            reader = new BufferedReader(new FileReader(JAMSTools.CreateAbsoluteFileName(this.getModel().getWorkspaceDirectory().getAbsolutePath(),
                    datafile.getValue())));
        } catch (IOException ioe) {
            this.getModel().getRuntime().sendHalt("could not open datafile " + datafile.getValue() + "; wrong path?");
            JAMS.handle(ioe);
            return;
        }
	String nextString = null;
	try {
	    int i = 0;
	    while ((nextString = reader.readLine()) != null) {	    	    
                StringTokenizer st = new StringTokenizer(nextString, "\t");
		double[] Example = new double[ExamplLength];
		double[] Predict = new double[1];
	                                    
		try {
		    for (int j = 0; j < ExamplLength; j++) {		
			Example[j] = (new Double(st.nextToken())).doubleValue();
		    }
		    Predict[0] = (new Double(st.nextToken())).doubleValue();
		}catch(NoSuchElementException e) {
                    this.getModel().getRuntime().sendHalt("Error in row " + i + "\nstop reading! (not enough numbers in row\nExpected " + (ExamplLength+1) + ")");
		    JAMS.handle(e);
                    break;
		}catch(NumberFormatException e) {
                    this.getModel().getRuntime().sendHalt("Error in row " + i + "\nstop reading!\nNot a number!!");
		    JAMS.handle(e);
                    break;
                }
		if (st.hasMoreTokens()) {
		    this.getModel().getRuntime().sendHalt("Error in row " + i + ";stop reading! (too many numbers in row\nExpected " + (ExamplLength+1) + ")");                    
		    break;
		}
	    
		rawData.put(new Integer(i),Example);
		rawPredict.put(new Integer(i),Predict);
	    
		i++;
	    }	
            numOfExampl = i;
	} catch (IOException ioe) {
            this.getModel().getRuntime().sendHalt("could not read datafile " + datafile.getValue());
            JAMS.handle(ioe);
        }

        Vector<double[]> data = new Vector<double[]>();
        Vector<Double> predict = new Vector<Double>();       
        
        boolean isExcluded = false;
	for (int i=0;i<numOfExampl-RelevantTime;i++) {
            isExcluded = false;
            double Sample[] = new double[RelevantTime*ExamplLength];
	    for (int j=0;j<RelevantTime;j++) {                
                if (excluded.contains(new Integer(i+j))){
                    isExcluded = true;
                    break;
                }                
		double entry[] = rawData.get(new Integer(i+j));
                
		for (int k=0;k<ExamplLength;k++) {
		    Sample[j*ExamplLength+k] = entry[k];
		}
                
	    }
            
            if (isExcluded)
                continue;
	    if (i+RelevantTime-1+dataShift.getValue() < 0) {                
		continue;
	    }
            if (excluded.contains(new Integer(i+RelevantTime-1+dataShift.getValue()))){
                isExcluded = true;
                continue;
            } 
            data.add(Sample);
	    predict.add(rawPredict.get(new Integer(i+RelevantTime-1+dataShift.getValue()))[0]);
	}
	double dataAsArray[][] = new double[data.size()][RelevantTime*ExamplLength];
        double predictAsArray[] = new double[data.size()];
        for (int i=0;i<data.size();i++){
            dataAsArray[i] = data.get(i);
            predictAsArray[i] = predict.get(i);
        }
	Data.setObject("data",dataAsArray);
	Data.setObject("predict",predictAsArray);
    }    
}
