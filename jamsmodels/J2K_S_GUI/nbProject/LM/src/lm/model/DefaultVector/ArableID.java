package lm.model.DefaultVector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.TreeMap;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMArableVector;
/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ArableID implements LMArableID,Cloneable{


    private Integer ID;
    private Color color;
    private int duration;
    private TreeMap<Integer,LMArableVector> treeMap;
    private int cropID;
    private int relativeBegin;
    private int relativeEnd;
    private Boolean isFallow;
    
    private double nitrogen;
    private double phosphorus;
    private int example3;
    private int example4;

    public ArableID(Integer ID,Color color,int duration,TreeMap<Integer,LMArableVector> treeMap,int cropID,int relativeBegin,int relativeEnd,Boolean isFallow){
        this.ID=ID;
        this.color=color;
        this.duration=duration;
        this.treeMap=treeMap;
        this.cropID=cropID;
        this.relativeBegin=relativeBegin;
        this.relativeEnd=relativeEnd;
        this.isFallow=isFallow;
    }

    public ArableID(Integer ID,int cropID){
        this.ID=ID;
        this.cropID=cropID;
        this.color = new Color((int)(Math.random() * 0xFFFFFF));
        this.duration=0;
        this.treeMap=new TreeMap();
        this.isFallow=false;
        
    }
    public ArableID(int begin , int end ,int duration){
        this.duration=duration;
        this.relativeBegin=begin;
        this.relativeEnd=end;
        this.isFallow=true;
        this.treeMap=null;
    }
    public ArableID(int duration){
        this.duration=duration;
        this.isFallow=true;
    }

    public Boolean isEmpty(){
        return treeMap.size()==0;
    }

    public void AddVector(Integer i){
        if(!treeMap.isEmpty()){
            Integer key=treeMap.lastKey();
            while(true){
                if(key<=i){
                    if(key==i){
                        treeMap.put(key+1, treeMap.get(key));
                    }
                    treeMap.put(i, new ArableVector());
                    break;
                }else{
                    treeMap.put(key+1, treeMap.get(key));
                }
                key=treeMap.lowerKey(key);
            }
        }else{
            treeMap.put(i,new ArableVector());
        }
        this.calculateBeginAndEnd();
        this.setYearsToVectors();
        this.calculateDuration();
        
    }
    
    public void AddVector(LMArableVector lmArableVector){
        if(treeMap.isEmpty()){
            treeMap.put(1, lmArableVector);
        }else{
            Integer key=treeMap.lastKey();
            treeMap.put(key+1,lmArableVector);
        }
        this.calculateBeginAndEnd();
        this.setYearsToVectors();
        this.calculateDuration();
        
    }

    public void DeleteVector(Integer i) {
        Integer key=i;
        while(true){
            if(treeMap.higherKey(key)!=null){
                treeMap.put(key,treeMap.get(key+1));
            }else{
                treeMap.remove(treeMap.lastKey());
                break;
            }
            key=treeMap.higherKey(key);
        }
        this.calculateBeginAndEnd();
        this.setYearsToVectors();
        this.calculateDuration();
        
        
    }

    private void calculateDuration() {
        if(!treeMap.isEmpty()){
            if(treeMap.size()==1){duration=1;}
            if(treeMap.size()>1){
                int first=treeMap.firstEntry().getValue().getDate();
                System.out.println("erstes datum =" + first);
                int last=treeMap.lastEntry().getValue().getDate();
                System.out.println("letztes datum =" + last);
                int firstYear=treeMap.firstEntry().getValue().getYear();
                System.out.println("erstes Jahr =" + firstYear);
                int lastYear=treeMap.lastEntry().getValue().getYear();
                System.out.println("letztes Jahr =" + lastYear);
                if(firstYear==lastYear){
                    duration=last-first+1;
                }
                if(lastYear-firstYear==1){
                    duration=last + (365-first)+1;
                }
                if(lastYear-firstYear>1){
                    duration=((lastYear-firstYear+1)*365)+last + (365-first)+1;
                }
            }
        }else{
            duration=0;
        }
    }

    private void calculateBeginAndEnd(){
        if(!treeMap.isEmpty()){
            this.relativeBegin=treeMap.firstEntry().getValue().getDate();
            this.relativeEnd=treeMap.lastEntry().getValue().getDate();
        }else{
            this.relativeBegin=0;
            this.relativeEnd=0;
        }
    }
    public LMArableID  clone(){
       TreeMap<Integer, LMArableVector> newTreeMap=new TreeMap();
       if(treeMap!=null){
           if(!treeMap.isEmpty()){
               Integer key =treeMap.firstKey();
               while(key!=null){
                   newTreeMap.put(key, treeMap.get(key).clone());
                   key=treeMap.higherKey(key);
               }
           }
       }
       return new ArableID(ID,color,duration, newTreeMap,cropID,relativeBegin,relativeEnd,isFallow);
    }

    public void ChangeAYearAndCheckTheRest(int vector,int year){
        
        treeMap.get(vector).setYear(year);
        if(vector!=treeMap.lastKey()){
            Integer key=vector+1;
            while(key!=null){
                //Wenn das nächste kleiner ist
                if(treeMap.get(key).getYear()<treeMap.get(key-1).getYear()){
                    System.out.println("Hier");
                    //Wenn das nächste kleiner ist und das Datum kleiner ist
                    if(treeMap.get(key).getDate()<treeMap.get(key-1).getDate()){
                        treeMap.get(key).setYear(treeMap.get(key-1).getYear()+1);
                    }else{
                        treeMap.get(key).setYear(treeMap.get(key-1).getYear());
                    }
                // Wenn das nächste Größer ist    
                }else{
                        if(treeMap.get(key).getDate()<treeMap.get(key-1).getDate()){
                            treeMap.get(key).setYear(treeMap.get(key-1).getYear()+1);
                      
                    }
                }

                key=treeMap.higherKey(key);
            }
        }
        calculateDuration();
    }

    public void setYearsToVectors(){
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            int zwi=0;
            int year=1;
            while(key!=null){
                if(zwi<=treeMap.get(key).getDate()){
                    treeMap.get(key).setYear(year);
                }else{
                    treeMap.get(key).setYear(year+1);
                    year++;
                }
                zwi=treeMap.get(key).getDate();
                key=treeMap.higherKey(key);
            }
        }
    }
    
    public void setADate(){
        calculateBeginAndEnd();
        calculateDuration();
        
    }

    public void print(){
        System.out.println(ID + " / " + relativeBegin + " / " + relativeEnd);
    }

    public Boolean isFallow(){
        return this.isFallow;
    }
    //Getter And Setter MEthods

    public void setColor(Color color) {
        this.color=color;
    }

    public Color getColor() {
        return this.color;
    }

    public void setDuration(int duration) {
        this.duration=duration;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setArableList(TreeMap treeMap) {
        this.treeMap=treeMap;
    }

    public TreeMap<Integer,LMArableVector> getTreeMap() {
        return this.treeMap;
    }

    public void setCropID(int i) {
        this.cropID=i;
    }

    public int getCropID() {
        return this.cropID;
    }

    public void setRelativeBegin(int i) {
        this.relativeBegin=i;
    }

    public int getRelativeBegin() {
        return this.relativeBegin;
    }

    public void setRelativeEnd(int i) {
        this.relativeEnd=i;
    }

    public int getRelativeEnd() {
        return this.relativeEnd;
    }

    public void setID(Integer ID) {
        this.ID=ID;
    }

    public Integer getID() {
        return ID;
    }
    
    public void setIsFallow(Boolean b){
        this.isFallow=b;
    }

    public void set_Nitrogen(double i) {
        this.nitrogen=i;
    }

    public double get_Nitrogen() {
        return nitrogen;
    }

    public void set_Phosphorus(double i) {
        this.phosphorus=i;
    }

    public double get_Phosphorus() {
        return phosphorus;
    }

    public void set_Example3(int i) {
        this.example3=i;
    }

    public int get_Example3() {
        return example3;
    }

    public void set_Example4(int i) {
        this.example4=i;
    }

    public int get_Example4() {
        return example4;
    }

    

    
}
