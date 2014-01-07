/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.test;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.dsproc.DataStoreProcessor;
import jams.workspace.dsproc.SimpleSerieProcessor;

/**
 *
 * @author chris
 */
public class J2KFileComparator {
        static public class Report{
            ArrayList<String> idsNotEqual = new ArrayList<String>();
            ArrayList<String> dataNotEqual = new ArrayList<String>();

            boolean failure;
            String errorMessage;

            protected void setFailure(String msg){
                failure = true;
                errorMessage = msg;
            }

            public String getFailure(){
                if (failure)
                    return errorMessage;
                return null;
            }
            public boolean isReportEmpty(){
                return !(dataNotEqual.size()>0 || idsNotEqual.size()>0);
            }

            public void addLine(String str){
                dataNotEqual.add(str);
            }

            public void print(PrintStream out){
                if (this.failure){
                    out.println(errorMessage);
                    return;
                }
                out.println("different ids:");
                Collections.sort(idsNotEqual);
                for (int i=0;i<idsNotEqual.size();i++){
                    out.println(idsNotEqual.get(i));
                }

                out.println("different data lines:");
                for (int i=0;i<dataNotEqual.size();i++){
                    out.println(dataNotEqual.get(i));
                }
            }
        }

        //static public void find

        static public Report compare(File fileA, File fileB, double accuracy ){
            Report report = new Report();

            SimpleSerieProcessor sspA  = new SimpleSerieProcessor(fileA);
            SimpleSerieProcessor sspB  = new SimpleSerieProcessor(fileB);
            boolean empty = sspA.isEmpty();
            System.out.println("wert2:" + sspA.isEmpty());
            if ( (sspA.isEmpty() && !sspB.isEmpty()) ||
                 (!sspA.isEmpty() && sspB.isEmpty())){
                report.setFailure("one of the compared files is empty");
                return report;
            }

            for (DataStoreProcessor.AttributeData attrib : sspA.getDataStoreProcessor().getAttributes()){
                attrib.setSelected(true);
            }
            for (DataStoreProcessor.AttributeData attrib : sspB.getDataStoreProcessor().getAttributes()){
                attrib.setSelected(true);
            }
            try{
                /*String idsA[] = sspA.getIDs();
                String idsB[] = sspB.getIDs();*/

                Set<String> idsA = new HashSet<String>(Arrays.asList(sspA.getIDs()));
                Set<String> idsB = new HashSet<String>(Arrays.asList(sspB.getIDs()));
                if ( !idsA.containsAll(idsB)){                    
                    idsB.removeAll(idsA);
                    report.idsNotEqual.addAll(idsB);
                }
                idsA = new HashSet<String>(Arrays.asList(sspA.getIDs()));
                idsB = new HashSet<String>(Arrays.asList(sspB.getIDs()));
                if ( !idsB.containsAll(idsA)){
                    idsA.removeAll(idsB);
                    report.idsNotEqual.addAll(idsA);
                }
                
                if (report.idsNotEqual.size()>0)
                    return report;

                idsA = new TreeSet<String>(Arrays.asList(sspA.getIDs()));
                Iterator<String> iter = idsA.iterator();
                
                while (iter.hasNext()) {
                    String key = iter.next();
                    DataMatrix dataA = sspA.getData(new String[]{key});
                    DataMatrix dataB = sspB.getData(new String[]{key});
                    if (dataA.getRowDimension() != dataB.getRowDimension()){
                        throw new Exception("different row dimension!");
                    }
                    for (int i = 0; i < dataA.getRowDimension(); i++) {
                        double rowA[] = dataA.getRow(i);                                                
                        double rowB[] = dataB.getRow(i);
                        boolean identical = true;

                        if (rowA.length!=rowB.length)
                            identical = false;

                        for (int j = 0; j < rowA.length && identical == true; j++) {
                            if (Double.isNaN(rowA[j])) 
                                continue;                            
                            if (Math.abs(rowA[j] - rowB[j]) > accuracy) 
                                identical = false;                            
                        }
                        if (!identical) {
                            report.addLine("different line:"+dataA.getIds()[i] + i + "\nnew:" + Arrays.toString(rowA) + "\nold:" + Arrays.toString(rowB));
                        }
                    }

                }
            }catch(Exception e){
                report.setFailure(e.toString());
            }
            return report;        
        }

                
}
