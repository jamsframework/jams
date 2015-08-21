package lm.model;

import lm.Componet.*;
import lm.Componet.Vector.*;
import lm.model.DefaultVector.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.TreeMap;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MultiModel extends Observable implements Cloneable,LMDefaultModel {

    private TreeMap<Integer,LMTillVector> LMTill =new TreeMap();
    private TreeMap<Integer,LMCropVector> LMCrop =new TreeMap();
    private TreeMap<Integer,LMFertVector> LMFert =new TreeMap();
    private TreeMap<Integer,LMCropRotationVector> LMCropRotation =new TreeMap();
    private TreeMap<Integer,LMLanduseVector>LMLanduse=new TreeMap();
    private TreeMap<Integer,LMHru_rot_ackerVector>LMHru_rot_acker =new TreeMap();

    private TreeMap<Integer,LMArableID> LMArable =new TreeMap();

    private String lmArablePath;
    private String cropPath;
    private String tillPath;
    private String fertPath;
    private String cropRotationPath;
    private String landusePath;
    private String hru_rot_ackerPath;

    public MultiModel(){

    }
    public void setPaths(String[] s){
        lmArablePath=s[0];
        cropPath=s[1];
        fertPath=s[2];
        tillPath=s[3];
        cropRotationPath=s[4];
        landusePath=s[5];
        hru_rot_ackerPath=s[6];
    }

    public Object clone() throws CloneNotSupportedException{
        MultiModel b= new MultiModel();
        b.setLMArable(new TreeMap<Integer,LMArableID>());
        b.setLMCrop(new TreeMap<Integer,LMCropVector>());
        b.setLMCropRotation(new TreeMap<Integer,LMCropRotationVector>());
        b.setLMFert(new TreeMap<Integer,LMFertVector>());
        b.setLMTill(new TreeMap<Integer,LMTillVector>());
        b.setLMLanduse(new TreeMap<Integer,LMLanduseVector>());
        b.setLMHru_rot_acker(new TreeMap<Integer,LMHru_rot_ackerVector>());
        
        Integer key;
        if(!LMArable.isEmpty()){
            key=LMArable.firstKey();
            while(key!=null){
                b.getLMArable().put(key, LMArable.get(key).clone());
                key=LMArable.higherKey(key);
            }
        }
        if(!LMCrop.isEmpty()){
            key=LMCrop.firstKey();
            while(key!=null){
                b.getLMCrop().put(key, LMCrop.get(key).clone());
                key=LMCrop.higherKey(key);
            }
        }
        if(!LMTill.isEmpty()){
            key=LMTill.firstKey();
            while(key!=null){
                b.getLMTill().put(key, LMTill.get(key).clone());
                key=LMTill.higherKey(key);
            }
        }
        if(!LMFert.isEmpty()){
            key=LMFert.firstKey();
            while(key!=null){
                b.getLMFert().put(key, LMFert.get(key).clone());
                key=LMFert.higherKey(key);
            }
        }
        if(!LMCropRotation.isEmpty()){
            key=LMCropRotation.firstKey();
            while(key!=null){
                b.getLMCropRotation().put(key, LMCropRotation.get(key).clone(b.getLMArable()));
                key=LMCropRotation.higherKey(key);
            }
        }
        if(!LMLanduse.isEmpty()){
            key=LMLanduse.firstKey();
            while(key!=null){
                b.getLMLanduse().put(key, LMLanduse.get(key).clone());
                key=LMLanduse.higherKey(key);
            }
        }
        if(!LMHru_rot_acker.isEmpty()){
            key=LMHru_rot_acker.firstKey();
            while(key!=null){
                b.getLMHru_rot_acker().put(key,LMHru_rot_acker.get(key).clone());
                key=LMHru_rot_acker.higherKey(key);
            }
        }
        
        return b;
    }

    public void createEmptyModel(){

    }

    public void createDefaultModel(){


    try{
    FileReader readerCrop = new FileReader(cropPath);
    FileReader readerTill = new FileReader(tillPath);
    FileReader readerFert = new FileReader(fertPath);
    FileReader readerLmArable =new FileReader(lmArablePath);
    FileReader readerCropRotation =new FileReader(cropRotationPath);
    FileReader readerLanduse =new FileReader (landusePath);
    FileReader readerHru_rot_acker =new FileReader(hru_rot_ackerPath);
    BufferedReader BuffReaderCrop = new BufferedReader(readerCrop);
    BufferedReader BuffReaderTill = new BufferedReader(readerTill);
    BufferedReader BuffReaderFert = new BufferedReader(readerFert);
    BufferedReader BuffReaderLmArable = new BufferedReader(readerLmArable);
    BufferedReader BuffReaderCropRotation = new BufferedReader(readerCropRotation);
    BufferedReader BuffReaderLanduse =new BufferedReader(readerLanduse);
    BufferedReader BuffReaderHru_rot_acker = new BufferedReader(readerHru_rot_acker);
    createEntity(BuffReaderFert,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("FERT"));
    createEntity(BuffReaderTill,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("TILL"));
    createEntity(BuffReaderCrop,java.util.ResourceBundle.getBundle("ressources/Ressources").getString("CROP"));
    createLanduse(BuffReaderLanduse);
    createHru_rot_acker(BuffReaderHru_rot_acker);
    createLMArable(BuffReaderLmArable);
    createCropRotation(BuffReaderCropRotation);



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
        Integer id;
        Integer cropVector;
        Integer year;
        Integer date;
        Integer tillVector;
        Integer fertVector;
        Double faMount;
        Boolean plant;
        Boolean harvest;
        Double fracharv;

        try{
                String t="#";
            while(t.startsWith("#")){
                t= toFile.readLine();
            }
            if(t.startsWith("ID")){
                t=toFile.readLine();
                String[] array;
                while(!t.startsWith("#")){
                    array=t.split("\t");
                    id=Integer.parseInt(array[0]);
                    cropVector=Integer.parseInt(array[1]);
                    year=Integer.parseInt(array[2]);
                    date=Integer.parseInt(array[3]);
                    if(array[4].equals("-")){
                        tillVector=0;
                    }else{
                        tillVector=Integer.parseInt(array[4]);
                    }
                    if(array[5].equals("-")){
                        fertVector=0;
                    }else{
                        fertVector=Integer.parseInt(array[5]);
                    }
                    if(array[6].equals("-")){
                        faMount=null;
                    }else{
                        faMount=Double.valueOf(array[6]);
                    }
                    if(array[7].equals("-")){
                        plant=false;
                    }else{
                        plant=true;
                    }
                    if(array[8].equals("-")){
                        harvest=false;
                    }else{
                        harvest=true;
                    }
                    if(array[9].equals("-")){
                        fracharv=null;
                    }else{
                        fracharv=Double.valueOf(array[9]);
                    }
                    if(LMArable.isEmpty()||!LMArable.containsKey(id)){
                        this.LMArable.put(id, new ArableID(id,cropVector));
                    }
                    this.LMArable.get(id).AddVector(new ArableVector(cropVector,year,date,tillVector,fertVector,faMount,plant,harvest,fracharv));
                    t=toFile.readLine();

                }
            }else{
                t=toFile.readLine();
                String[] array;
                while(!t.startsWith("#")){
                    array=t.split("\t");
                    int check=Integer.parseInt(array[0]);
                    cropVector=check;
                    date=Integer.parseInt(array[1]);
                    if(array[2].equals("-")){
                        tillVector=0;
                    }else{
                        tillVector=Integer.parseInt(array[2]);
                    }
                    if(array[3].equals("-")){
                        fertVector=0;
                    }else{
                        fertVector=Integer.parseInt(array[3]);
                    }
                    if(array[4].equals("-")){
                        faMount=null;
                    }else{
                        faMount=Double.valueOf(array[4]);
                    }
                    if(array[5].equals("-")){
                        plant=false;
                    }else{
                        plant=true;
                    }
                    if(array[6].equals("-")){
                        harvest=false;
                    }else{
                        harvest=true;
                    }
                    if(array[7].equals("-")){
                        fracharv=null;
                    }else{
                        fracharv=Double.valueOf(array[7]);
                    }
                    LMArableVector Vector = new ArableVector(cropVector,date,tillVector,fertVector,faMount,plant,harvest,fracharv);

                     if(this.LMArable.isEmpty()){
                        this.LMArable.put(1, new ArableID(1,check));
                    }else{
                        if(this.LMArable.get(LMArable.lastKey()).getCropID()!=check){
                            this.LMArable.put(LMArable.lastKey()+1, new ArableID(LMArable.lastKey()+1,check));
                        }
                    }
                    this.LMArable.get(this.LMArable.lastKey()).AddVector(Vector);
                    Vector.print();
                    t= toFile.readLine();
                }
            }

            

        }catch(IOException e){}
    }

    public void createCropRotation(BufferedReader toFile){
        ArrayList<LMArableID>AIDs=new ArrayList();
        try{
                String t=toFile.readLine();
                while(t.startsWith("#")){
                    t=toFile.readLine();
                }
                t=toFile.readLine();
                String[] array;
                while(!t.startsWith("#")){
                    AIDs=new ArrayList();
                    array=t.split("\t");
                    Integer id = Integer.parseInt(array[0]);
                    for(int i=1;i<array.length;i++){
                        if(array[i].startsWith("/")){
                            array[i]=array[i].replace("/", "");
                           AIDs.add(new ArableID(Integer.parseInt(array[i])));
                        }else{
                           AIDs.add(LMArable.get(Integer.parseInt(array[i])));
                        }
                    }
                    LMCropRotation.put(id, new CropRotationVector(AIDs,id));
                   t=toFile.readLine();
                }

        }catch(IOException e){}

    }
    
    public void createLanduse(BufferedReader toFile){
        ArrayList<String> a;
        try{
            String t =toFile.readLine();
            while(t.startsWith("#")){
                t=toFile.readLine();
            }
            String[] array;
            t=toFile.readLine();
            while(!t.startsWith("#")){
                if(!t.startsWith("n")){
                    a=new ArrayList();
                    array=t.split("\t");
                    for(int i=0;i<array.length;i++){
                        a.add(array[i]);
                    }
                    LMLanduse.put(Integer.parseInt(a.get(0)),new LanduseVector(a));
                }
                t=toFile.readLine();
                
                
            }
        }catch(IOException e){}
    }
    
    public void createHru_rot_acker(BufferedReader toFile){
        ArrayList<String> a;
        try{
            String t =toFile.readLine();
            while(t.startsWith("#")){
                t=toFile.readLine();
            }
            String[] array;
            t=toFile.readLine();
            while(!t.startsWith("#")){
                if(!t.startsWith("n")){
                    a=new ArrayList();
                    array=t.split("\t");
                    for(int i=0;i<array.length;i++){
                        a.add(array[i]);
                    }
                    LMHru_rot_acker.put(Integer.parseInt(a.get(0)),new Hru_rot_ackerVector(a));
                }
                t=toFile.readLine();
                
                
            }
        }catch(IOException e){}
    }


    public void SystemPrint(){
        
    }
    public boolean isEmpty(){
        if(LMFert.isEmpty()&&LMCrop.isEmpty()&&LMTill.isEmpty()){
            return true;
        }
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
    
    public TreeMap<Integer,LMArableID> getLMArable() {
        return LMArable;
    }

    /**
     * @param LMTill the LMTill to set
     */
    public void setLMTill(TreeMap<Integer, LMTillVector> LMTill) {
        this.LMTill = LMTill;
    }

    /**
     * @param LMCrop the LMCrop to set
     */
    public void setLMCrop(TreeMap<Integer, LMCropVector> LMCrop) {
        this.LMCrop = LMCrop;
    }

    /**
     * @param LMFert the LMFert to set
     */
    public void setLMFert(TreeMap<Integer, LMFertVector> LMFert) {
        this.LMFert = LMFert;
    }

    /**
     * @param LMCropRotation the LMCropRotation to set
     */
    public void setLMCropRotation(TreeMap<Integer, LMCropRotationVector> LMCropRotation) {
        this.LMCropRotation = LMCropRotation;
    }

    /**
     * @param LMArable the LMArable to set
     */
    public void setLMArable(TreeMap<Integer, LMArableID> LMArable) {
        this.LMArable = LMArable;
    }
    
    public void setLMLanduse(TreeMap<Integer, LMLanduseVector> LMLanduse) {
        this.LMLanduse = LMLanduse;
    }
    
    public void setLMHru_rot_acker(TreeMap<Integer, LMHru_rot_ackerVector> LMHru_rot_acker) {
        this.LMHru_rot_acker = LMHru_rot_acker;
    }
    
    

    public TreeMap getLMLanduse() {
        return LMLanduse;
    }

    public TreeMap getLMHru_rot_acker() {
        return LMHru_rot_acker;
    }
    
}
