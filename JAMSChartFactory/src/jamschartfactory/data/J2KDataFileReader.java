/*
 * J2KDataFileReader.java
 *
 * Created on 20. April 2006, 12:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jamschartfactory.data;

import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;

/**
 *
 * @author c0krpe
 */
public class J2KDataFileReader {
    public String[] dataCols = null;
    public double[][] dataMatrix = null;
    public int[][] dateMatrix = null;
    public int nTsteps = 0;
    public int nSeries = 0;
    public String fileName;
    
    /**
     * Creates a new instance of J2KDataFileReader
     */
    public J2KDataFileReader() {
        File inFile = this.browse_data_file();
        this.fileName = inFile.getName();
        this.openDataFile(inFile);
        
    }
    
    public File browse_data_file(){
        String filename = "n/a";
        JFileChooser pfc = new JFileChooser();
        pfc.setDefaultLocale(java.util.Locale.ENGLISH);
        pfc.setDialogTitle("Select data file: ");
        pfc.setCurrentDirectory(new File("D:/Development/JAMSChartFactory"));
        int returnval = pfc.showOpenDialog(null);
        
        if(returnval == pfc.APPROVE_OPTION){
            return pfc.getSelectedFile();
        }
        return null;
    }
    
    public void openDataFile(File inFile){
        if(!inFile.exists()){
            System.out.println("inFile is not existent");
        }
        else{
            //opening the station data file
            try{
                java.io.BufferedReader bufDR  = new java.io.BufferedReader(new FileReader(inFile));
                String dataLine;
                
                dataLine = bufDR.readLine();
                //skipping header
                while(dataLine.charAt(0) == '#')
                    dataLine = bufDR.readLine();
                
                StringTokenizer dataTok;
                dataTok = new StringTokenizer(dataLine, "\t");
                
                int cols = dataTok.countTokens();
                
                dataCols = new String[cols];
                for(int i = 0; i < cols; i++){
                    dataCols[i] = dataTok.nextToken();
                }
                int count = 0;
                boolean cont = true;
                while(cont){
                    dataLine = bufDR.readLine();
                    if(dataLine != null){
                        count++;
                        //System.out.println("count: " + count + " " + dataLine);
                    } else{
                        cont = false;
                        System.out.println("entries: " + count);
                    }
                }
                this.nTsteps = count;
                this.nSeries = cols - 1;
                this.dataMatrix = new double[nSeries][nTsteps];
                this.dateMatrix = new int[5][nTsteps];
                bufDR.close();
                
                bufDR  = new java.io.BufferedReader(new FileReader(inFile));
                dataLine = bufDR.readLine();
                //skipping header
                while(dataLine.charAt(0) == '#')
                    dataLine = bufDR.readLine();
                //header line
                dataLine = bufDR.readLine();
                for(int i = 0; i < nTsteps; i++){
                    dataTok = new StringTokenizer(dataLine, "\t");
                    String dateTok = dataTok.nextToken();
                    StringTokenizer dataCut = new StringTokenizer(dateTok, " ");
                    String datePart = dataCut.nextToken();
                    StringTokenizer dateCut = new StringTokenizer(datePart, "-");
                    for(int d = 0; d < 3; d++){
                        String da = dateCut.nextToken();
                        //System.out.println("da:" + da);
                        dateMatrix[d][i] = Integer.parseInt(da);
                    }
                    String timePart = dataCut.nextToken();
                    StringTokenizer timeCut = new StringTokenizer(timePart, ":");
                    for(int d = 3; d < 5; d++){
                        String ti = timeCut.nextToken();
                        //System.out.println("ti: " + ti);
                        dateMatrix[d][i] = Integer.parseInt(ti);
                    }
                    for(int c = 0; c < nSeries; c++){
                        dataMatrix[c][i] = Double.parseDouble(dataTok.nextToken());
                        //System.out.println("data: " + dataMatrix[c][i]);
                    }
                    dataLine = bufDR.readLine();
                }
                bufDR.close();
                
            }catch(java.io.FileNotFoundException e){
                System.out.println("Datafile could not be opened because: "+e.getMessage());
                return;
            }catch(java.io.IOException ioe){
                System.out.println("Error during datafile reading: "+ioe.getMessage());
                return;
            }
        }
        
    }
    
}
