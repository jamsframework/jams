/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import lm.Componet.*;
import lm.Componet.Vector.*;
import lm.model.DefaultModel.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MultiReader extends Observable implements LMDefaultModel {
    
    private ArrayList<LMFertVector> fert=new ArrayList();
    private ArrayList<ArrayList> till=new ArrayList();
    private ArrayList<ArrayList> crop=new ArrayList();
    private ArrayList lmArable =new ArrayList();

    private String lmArablePath;
    private String cropPath;
    private String tillPath;
    private String fertPath;

    public MultiReader(){

    }
    public void setPaths(String lmArable,String crop,String till,String fert){
        lmArablePath=lmArable;
        cropPath=crop;
        tillPath=till;
        fertPath=fert;
    }

    public void createDefaultModel(){

    ArrayList<ArrayList<String>> cropArray =new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> tillArray =new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> fertArray =new ArrayList<ArrayList<String>>();


    try{
    FileReader readerCrop = new FileReader(cropPath);
    FileReader readerTill = new FileReader(tillPath);
    FileReader readerFert = new FileReader(fertPath);
    BufferedReader BuffReaderCrop = new BufferedReader(readerCrop);
    BufferedReader BuffReaderTill = new BufferedReader(readerTill);
    BufferedReader BuffReaderFert = new BufferedReader(readerFert);
    createFertList(BuffReaderFert);
//    fert=createEntity(BuffReaderFert);
    SystemPrint();
    crop=createEntity(BuffReaderCrop);
    till=createEntity(BuffReaderTill);

        }catch(IOException e){

        }
    }

    public void tellObservers(int i){
        setChanged();
        notifyObservers();
    }

    public ArrayList createEntity(BufferedReader toFile){
        ArrayList<ArrayList>ToUse=new ArrayList();
        try{
        String t="#";
        while(t.startsWith("#")){
            t= toFile.readLine();
        }
        t=toFile.readLine();
        int i=0;
        while(!t.startsWith("#")){
            System.out.println("Grösse des Strings " + t.length() );
            ToUse.add(new ArrayList());
            int p = 0;
            while(p<t.length()){
                    String l="";
                    while(t.charAt(p)!='\t'&&p<t.length()-1){
                        l=l+t.charAt(p);
                        p++;
                    }
                    ToUse.get(i).add(l);
                    System.out.println(l);
                    p++;
            }
            System.out.println("###########NEXTLINE###########");
            t=toFile.readLine();
            i++;

        }
        }catch(IOException e){

        }
        return ToUse;
    }

    public void createFertList(BufferedReader toFile){
        ArrayList<String>ToUse=new ArrayList();
        try{
        String t="#";
        while(t.startsWith("#")){
            t= toFile.readLine();
        }
        t=toFile.readLine();
        int i=0;
        while(!t.startsWith("#")){
            System.out.println("Grösse des Strings " + t.length() );
            ToUse=new ArrayList();
            int p = 0;
            while(p<t.length()){
                    String l="";
                    while(t.charAt(p)!='\t'&&p<t.length()-1){
                        l=l+t.charAt(p);
                        p++;
                    }
                    ToUse.add(l);
                    System.out.println(l);
                    p++;
            }
            System.out.println("###########NEXTLINE###########");
            t=toFile.readLine();
            i++;
            fert.add(new FertVector(ToUse));

        }
        }catch(IOException e){

        }
    }

    public ArrayList getFert(){
        return fert;
    }
    public ArrayList getTill(){
        return till;
    }
    public ArrayList getCrop(){
        return crop;
    }
    public ArrayList getlmArable(){
        return lmArable;
    }

    public ArrayList getRow(){
        return new ArrayList();
    }


    public void SystemPrint(){
        for (int i=0;i<fert.size();i++){
           System.out.print(fert.get(i).getID()+ "\t");
           System.out.print(fert.get(i).getfertnm()+ "\t");
           System.out.print(fert.get(i).getfminn()+ "\t");
           System.out.print(fert.get(i).getfminp()+ "\t");
           System.out.print(fert.get(i).getforgn()+ "\t");
           System.out.print(fert.get(i).getforgp()+ "\t");
           System.out.print(fert.get(i).getfnh3n()+ "\t");
           System.out.print(fert.get(i).getbactpdb()+ "\t");
           System.out.print(fert.get(i).getbactldb()+ "\t");
           System.out.print(fert.get(i).getbactddb()+ "\t");
           System.out.print(fert.get(i).getdesc()+ "\r\n");
        }
    }
    public boolean isEmpty(){
        if(fert.isEmpty()&&crop.isEmpty()&&till.isEmpty()){
            System.out.println("Kein Model");
            return true;
        }
        System.out.println("Ein Model");
        return false;
    }

}
