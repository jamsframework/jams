package lm.model;

import lm.Componet.*;
import lm.Componet.Vector.*;
import lm.model.DefaultVector.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Observable;
import java.util.TreeMap;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MultiModel extends Observable implements LMDefaultModel,Cloneable {
    
//    private ArrayList<LMFertVector> fert=new ArrayList();
//    private ArrayList<LMTillVector> till=new ArrayList();
//    private ArrayList<LMCropVector> crop=new ArrayList();
//    private ArrayList<LMCropRotationVector>cropRotation=new ArrayList();
//    private ArrayList<LMArableVector> lmArable =new ArrayList();

    private TreeMap<Integer,LMTillVector> LMTill =new TreeMap();
    private TreeMap<Integer,LMCropVector> LMCrop =new TreeMap();
    private TreeMap<Integer,LMFertVector> LMFert =new TreeMap();
    private TreeMap<Integer,LMCropRotationVector> LMCropRotation =new TreeMap();

    private TreeMap<ArableID,LMArableVector> LMArable =new TreeMap();

    private String lmArablePath;
    private String cropPath;
    private String tillPath;
    private String fertPath;
    private String cropRotationPath;

    public MultiModel(){

    }
    public void setPaths(String lmArable,String crop,String till,String fert,String cropRotation){
        lmArablePath=lmArable;
        cropPath=crop;
        tillPath=till;
        fertPath=fert;
        cropRotationPath=cropRotation;
    }

    public MultiModel clone() throws CloneNotSupportedException{
       return (MultiModel)super.clone();
    }

    public void createDefaultModel(){


    try{
    FileReader readerCrop = new FileReader(cropPath);
    FileReader readerTill = new FileReader(tillPath);
    FileReader readerFert = new FileReader(fertPath);
    FileReader readerLmArable =new FileReader(lmArablePath);
    FileReader readerCropRotation =new FileReader(cropRotationPath);
    BufferedReader BuffReaderCrop = new BufferedReader(readerCrop);
    BufferedReader BuffReaderTill = new BufferedReader(readerTill);
    BufferedReader BuffReaderFert = new BufferedReader(readerFert);
    BufferedReader BuffReaderLmArable = new BufferedReader(readerLmArable);
    BufferedReader BuffReaderCropRotation = new BufferedReader(readerCropRotation);
    createEntity(BuffReaderFert,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FERT"));
    createEntity(BuffReaderTill,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("TILL"));
    createEntity(BuffReaderCrop,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("CROP"));
    createCropRotation(BuffReaderCropRotation);
    createLMArable(BuffReaderLmArable);



        }catch(IOException e){

        }
    }

    public void tellObservers(int i){
        setChanged();
        notifyObservers();
    }

    public void createEntity(BufferedReader toFile, String s){

        ArrayList<String>ToUse=new ArrayList();
        try{
        String t=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("#");
        while(t.startsWith(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("#"))){
            t= toFile.readLine();
        }
        t=toFile.readLine();
        int i=0;
        while(!t.startsWith(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("#"))){
            System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("GRÖSSE DES STRINGS ") + t.length() );
            ToUse=new ArrayList();
            int p = 0;
            while(p<t.length()){
                    String l=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                    while(t.charAt(p)!='\t'&&p<t.length()-1){
                        l=l+t.charAt(p);
                        p++;
                    }
                    if(p==t.length()-1){
                        l=l+t.charAt(p);
                    }
                    ToUse.add(l);
                    
                    p++;
            }
            System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("###########NEXTLINE###########"));
            t=toFile.readLine();
            i++;
            if(s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("TILL"))){
                TillVector Vector=new TillVector(ToUse);
//                till.add(Vector);
                    getLMTill().put(Vector.getTID(),Vector);
                Vector.print();
            }
            if(s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FERT"))){
                LMFertVector Vector = new FertVector(ToUse);
//                fert.add(Vector);
                    LMFert.put(Vector.getID(),Vector);

            }
            if(s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("CROP"))){
                LMCropVector Vector = new CropVector(ToUse);
//                crop.add(Vector);
                    LMCrop.put(Vector.getID(),Vector);
            }
        }
        }catch(IOException e){

        }
    }

    public void createLMArable(BufferedReader toFile){
        LMArableID arableID;
        ArableID Key;
        LMCropVector cropVector=new CropVector();
        String date;
        LMTillVector tillVector;
        LMFertVector fertVector;
        Double faMount;
        Boolean plant;
        Boolean harvest;
        Double fracharv;

        try{
                String t="#";
            while(t.startsWith("#")){
                t= toFile.readLine();
            }
            t= toFile.readLine();
            int q =0;
            while(!t.startsWith("#")){
                System.out.println(t);
                int p=0;
                String cid=("");
                // Read CropID
                while(t.charAt(p)!='\t'){
                    cid=cid+t.charAt(p);
                    p++;
                }
                // Check if new ArableID is Part of the Last ArableID in the model
                int check=Integer.parseInt(cid);
                if(this.LMArable.isEmpty()){
                    arableID=new ArableID(1,1,1);
                    Key=new ArableID(1,1);
                    
                }else{
                    if(this.LMArable.get(LMArable.lastKey()).getCID().getID()==check){
                        System.out.println(this.LMArable.get(LMArable.lastKey()).getCID().getID() +" ==" + check);
                        LMArableID last=this.LMArable.get(this.LMArable.lastKey()).getAID();
                        arableID=new ArableID(last.getID(),last.getStufe()+1,last.getMaxStufe()+1);
                        Key=new ArableID(last.getID(),last.getStufe()+1);
                    }
                    // set new ArableID is a new Part in the model
                    else{
                        System.out.println(this.LMArable.get(LMArable.lastKey()).getCID().getID() +" !=" + check);
                        arableID =new ArableID(this.LMArable.get(this.LMArable.lastKey()).getAID().getID()+1,1,1);
                        Key=new ArableID(this.LMArable.lastKey().getID()+1,1);
                      }

                    // set MaxStufe in the same AID to new MaxStufe
                    ArableID s = LMArable.firstKey();
                    while(s!=null){
                        if(arableID.getID()==LMArable.get(s).getAID().getID()){
                            LMArable.get(s).getAID().setMaxStufe(arableID.getMaxStufe());
                        }
                        s=LMArable.higherKey(s);
                    }
                }
                Integer i=LMCrop.firstKey();
                while(i!=null){
                    
                    if(LMCrop.get(i).getID()==check){
                        System.out.println("Crop ID ------------->" +LMCrop.get(i).getID());
                        cropVector=LMCrop.get(i);
                        i=null;
                    }else{
                        i=LMCrop.higherKey(i);
                    }
                }

//                for(int i=1;i<=LMCrop.size();i++){
//                    System.out.println(LMCrop.get(i).getID());
//                    if (LMCrop.get(i).getID()==check){
//                        cropVector=LMCrop.get(i);
//
//                    }
//                }

                p++;
                String s=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                while(t.charAt(p)!='\t'){
                    s=s+t.charAt(p);
                    p++;
                }
                date=s;
                p++;
                s=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                while(t.charAt(p)!='\t'){
                    s=s+t.charAt(p);
                    p++;
                }
                if(!s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("-"))){
                    tillVector=LMTill.get(Integer.parseInt(s));
                }else{
                    tillVector=new TillVector();
                }
                p++;
                s=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                while(t.charAt(p)!='\t'){
                    s=s+t.charAt(p);
                    p++;
                }
                if(!s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("-"))){
                    fertVector=LMFert.get(Integer.parseInt(s));
                }else{
                    fertVector=new FertVector();
                }
                p++;
                s=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                while(t.charAt(p)!='\t'){
                    s=s+t.charAt(p);
                    p++;
                }
                if(!s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("-"))){
                    faMount=Double.valueOf(s);
                }else{
                    faMount=null;
                }
                p++;
                if(t.charAt(p)=='-'){
                    plant=false;
                }else{
                    plant=true;
                }
                p++;
                p++;
                if(t.charAt(p)=='-'){
                    harvest=false;
                }else{
                    harvest=true;
                }
                s=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                while(t.charAt(p)!='\t'){
                    s=s+t.charAt(p);
                    p++;
                }
                if(!s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("-"))){
                    fracharv=Double.valueOf(s);
                }else{
                    fracharv=null;
                }
                
                //hinzufügen des neuen Arablevectors
                LMArableVector Vector = new ArableVector(arableID,cropVector,date,tillVector,fertVector,faMount,plant,harvest,fracharv);
                LMArable.put(Key,Vector);
                System.out.print(Vector.getAID().toString() + "   ");
                Vector.print();
                t= toFile.readLine();

                
            }

        }catch(IOException e){}
    }

    public void createCropRotation(BufferedReader toFile){
        int ID;
        ArrayList<Integer>CID=new ArrayList();
        try{
                String t=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("#");
            while(t.startsWith(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("#"))){
                t= toFile.readLine();
            }
            t= toFile.readLine();
            while(!t.startsWith(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("#"))){
                int p=0;
                String id=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                // Read CropID
                while(t.charAt(p)!='\t'){
                    id=id+t.charAt(p);
                    p++;
                    
                }
                ID=Integer.parseInt(id);
                p++;
                while(p<t.length()){
                    String s=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
                    while(t.charAt(p)!='\t'&&p<t.length()-1){
                        s=s+t.charAt(p);
                        p++;
                    }
                    if((p==t.length()-1)&&(t.charAt(p)!='\t')){
                        s=s+t.charAt(p);
                    }
                    p++;
                    if(!s.equals(java.util.ResourceBundle.getBundle("ressources/Ressources").getString(""))){
                    CID.add(Integer.parseInt(s));
                    }
                }
                t= toFile.readLine();
//                cropRotation.add(new CropRotationVector(ID,CID));

                System.out.println();

            }
        }catch(IOException e){}



    }

//    public ArrayList getFert(){
//        return fert;
//    }
//    public ArrayList getTill(){
//        return till;
//    }
//    public ArrayList getCrop(){
//        return crop;
//    }
//    public ArrayList getlmArable(){
//        return lmArable;
//    }
//
//    public ArrayList getCropRotation(){
//        return cropRotation;
//    }


    public void SystemPrint(){
        
    }
    public boolean isEmpty(){
        if(LMFert.isEmpty()&&LMCrop.isEmpty()&&LMTill.isEmpty()){
            System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("KEIN MODEL"));
            return true;
        }
        System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("EIN MODEL"));
        return false;
    }

    /**
     * @return the LMTill
     */
    public TreeMap<Integer, LMTillVector> getLMTill() {
        return LMTill;
    }
    
    public TreeMap<Integer, LMCropVector> getLMCrop() {
        return LMCrop;
    }
    
    public TreeMap<Integer, LMFertVector> getLMFert() {
        return LMFert;
    }
    
    public TreeMap<Integer, LMCropRotationVector> getLMCropRotation() {
        return LMCropRotation;
    }
    
    public TreeMap<ArableID, LMArableVector> getLMArable() {
        return LMArable;
    }






}
