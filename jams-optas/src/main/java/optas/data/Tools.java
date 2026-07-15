/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.StringTokenizer;
import optas.io.ImportMonteCarloData;
import optas.io.ImportMonteCarloException;

/**
 *
 * @author chris
 */
public class Tools {

    static public void main(String args[]) throws IOException{
        String directory = args[0];
        String rangeFile = args[1];
        String outFile   = args[2];

        BufferedReader reader = new BufferedReader(new FileReader(rangeFile));

        HashMap<String, double[]> rangeMap = new HashMap<String, double[]>();

        String line = null;
        while( (line=reader.readLine())!=null){
            StringTokenizer tok = new StringTokenizer(line,";");
            if (tok.countTokens() >= 3){
                String name = tok.nextToken();
                double low  = Double.parseDouble(tok.nextToken());
                double up  = Double.parseDouble(tok.nextToken());
                rangeMap.put(name, new double[]{low,up});
            }
        }

        File fDirectory = new File(directory);

        if (!fDirectory.isDirectory())
            return;

        DataCollection completeSet = null;
        DecimalFormat df = new DecimalFormat("000000");
        for (File f: fDirectory.listFiles()){
            if (f.getName().startsWith("scalar") && f.getName().endsWith(".dat")){
                System.out.println("Working on " + f);
                ImportMonteCarloData importer = new ImportMonteCarloData();
                DataCollection dc = null;
                try{
                    importer.addFile(f);
                    dc = importer.getEnsemble();
                    importer.finish();
                }catch(ImportMonteCarloException imce){
                    imce.printStackTrace();
                    System.out.println(imce);
                }
                String parts[] = f.getName().split("[_\\.]");
                long value = Long.parseLong(parts[2]);
                String newName = f.getParent() + "/" + "timeseries_" + parts[1] + "_" + df.format(value) + ".dat";
                File f2 = new File(newName);
                int counter = 0;
                while (!f2.isFile()){
                    value++;
                    newName = f.getParent() + "/" + "timeseries_" + parts[1] + "_" + df.format(value) + ".dat";
                    System.out.println("try: " + newName);
                    f2 = new File(newName);
                    counter++;
                    if (counter>10){
                        System.err.println("counter exceed max.");
                        return;
                    }
                }

                ImportMonteCarloData importer2 = new ImportMonteCarloData();
                DataCollection dc2 = null;
                try{
                importer2.addFile(f2);
                dc2 = importer2.getEnsemble();
                }catch(ImportMonteCarloException imce){
                    imce.printStackTrace();
                    System.out.println(imce);
                }
                importer2.finish();
                dc.unifyDataCollections(dc2);
                for (String attr : rangeMap.keySet()){
                    dc.filter(attr, rangeMap.get(attr)[0], rangeMap.get(attr)[1],false);
                }
                dc.commitFilter();
                if (completeSet == null)
                    completeSet = dc;
                else
                    completeSet.mergeDataCollections(dc);
            }
        }
        completeSet.save(new File(outFile));
    }
}
