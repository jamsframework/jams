package jams.tspaces;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpace;
import java.io.*;
import org.unijena.jams.data.*;

import jams.tspaces.*;

// parameter:
// 1. jmc-file
// 2. Datei, in der der Tuplespace gespeichert werden soll

public class save {

	public static void main(String[] args) {
		String jmc=args[0];
		InitSpace initspace= new InitSpace(jmc);
		try{	
			TupleSpace space = new TupleSpace(initspace.name,initspace.host);
			//schreibt den gesamten Space-Inhalt in eine Datei
			/*Tuple template = new Tuple();
			for (int j=0; j<7; j++){
				template.add(new Field( Serializable.class));
			    Tuple scan = space.scan(template);
			    int size=scan.numberOfFields();
			    for (int i=0; i<size; i++){
			    	System.out.println(scan.getField(i).getValue());
			    	fw.write(scan.getField(i).getValue().toString());
			    	fw.write("\n");
			    }			
			} */
			
			Tuple template=new Tuple(new Field(new JAMSString("fileName")), new Field(JAMSString.class));
			String outputfile="";
			if (space.countN(template)>0){
				Tuple file = space.read(template);
				outputfile=((JAMSString) file.getField(1).getValue()).getValue();
			} else{
				outputfile=args[1];				
			}			
			Writer fw = new FileWriter(outputfile);
			StringBuffer info = new StringBuffer("");
			
			template=new Tuple(new Field(new JAMSString("requestedJVM")), new Field(JAMSInteger.class));
			info.append("requestedJVM: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue()+"\t");
			template=new Tuple(new Field(new JAMSString("assignedJVM")), new Field(JAMSInteger.class));
			info.append("assignedJVM: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue()+"\t");			
			template=new Tuple(new Field(new JAMSString("sampleCount")), new Field(JAMSInteger.class));
			info.append("sampleCount: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue()+"\t");			
			template=new Tuple(new Field(new JAMSString("currentCount")), new Field(JAMSInteger.class));
			info.append("currentCount: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue());
			fw.write(info.toString()+"\n");
			
			template=new Tuple(new Field(new JAMSString("header")), new Field(JAMSString.class));
			String header=((JAMSString)space.read(template).getField(1).getValue()).getValue();
			fw.write(header.replaceAll(" ", "\t")+"\n");
			
			template = new Tuple(new Field(new JAMSString("result")), new Field(JAMSString.class));
			Tuple scan = space.scan(template);
			int size=scan.numberOfFields();
		    for (int i=0; i<size; i++){
		    	String result=((JAMSString) (((Tuple) scan.getField(i).getValue()).getField(1).getValue())).toString();
		    	//fw.write(result.replaceAll(" ","\t")+"\n");
		    	result=result.replaceAll(" ","\t");
		    	String komma="";
		    	for (int j=0; j<result.length(); j++){
		    		if (result.charAt(j)==46){
		    			komma=komma+",";
		    		}
		    		else{
		    			komma=komma+result.charAt(j);
		    		}
		    	}		    	
		    	fw.write(komma+"\n");
		    }			
			fw.close();
	    } catch ( IOException e ) { 
			  System.err.println( "Konnte Datei nicht erstellen" ); 
		} catch (Exception e) {
	        System.out.println(e);
		}
	}

}
