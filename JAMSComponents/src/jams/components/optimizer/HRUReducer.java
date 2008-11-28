/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import jams.data.*;
import jams.data.JAMSEntity;
import jams.data.JAMSEntity.NoSuchAttributeException;
import jams.data.JAMSEntityCollection;
import jams.model.JAMSComponent;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.model.Snapshot;

/**
 *
 * @author Christian Fischer
 */
public class HRUReducer extends JAMSContext{
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSBoolean active;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE, 
            update = JAMSVarDescription.UpdateType.INIT, 
            description = "Collection of hru objects")
    public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSDouble effValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSEntity snapshot;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSInteger method;
    
    public class HRU_Comparator implements Comparator {

    private String attribute;
    private int order = 1;
    
    public HRU_Comparator(String attribute,boolean decreasing_order) {
	this.attribute = attribute;
	if (decreasing_order)
	    order = -1;
	else
	    order = 1;
    }

    public int compare(Object d1, Object d2) {

        JAMSEntity b1 = (JAMSEntity)d1;
        JAMSEntity b2 = (JAMSEntity)d2;
        
        try{
            if (b1.getDouble(attribute) < b2.getDouble(attribute))
                return -1*order;
            else if (b1.getDouble(attribute) == b2.getDouble(attribute))
                return 0*order;
            else
                return 1*order;
        }catch(Exception e){
            System.out.print("Error during HRU sorting, because" + e.toString());
            return 0;
        }
    }
    
    
} 
    Snapshot mySnapShot = null;
            
    public double singleRun(){
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }    
        double result = effValue.getValue(); 
                
        return result;                
    }
    
    void restore(){
        if (mySnapShot != null){
            this.getModel().setModelState(mySnapShot);
        }
    }
            
    JAMSEntity[] copyEntityArray(JAMSEntity[] src){
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        JAMSEntity[] ea_ref = null;
        try{
            ea_ref = (JAMSEntity[])src;
            objOut = new ObjectOutputStream(outStream);
            objOut.writeObject(ea_ref);
            objOut.close();         
            ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
            ea_ref = (JAMSEntity[])objIn.readObject();
        }catch(Exception e){
            System.out.println("Error during HRU serialization" + e.toString());
        }    
        return ea_ref;
    }
    
    void setToPolyID(JAMSEntity src,ArrayList<JAMSEntity> array,int value){
        Object obj = null;
        try {
        obj = src.getObject("to_poly");
        }catch(Exception e){
            this.getModel().getRuntime().sendHalt("entity: " + src.toString() + " does not contain to_poly attribute!");
        }
        if (obj instanceof JAMSDouble)
            ((JAMSDouble)obj).setValue((double)value);            
        else if (obj instanceof JAMSEntity){
            try{
                JAMSEntity to_poly_entity = null;
                for (int i=0;i<array.size();i++){
                    if ( (int)(array.get(i).getDouble("ID")) == value){
                        to_poly_entity = array.get(i);
                        break;
                    }
                        
                }
                if (to_poly_entity != null)
                    src.setObject("to_poly", to_poly_entity);
            }catch(Exception e){
                this.getModel().getRuntime().sendHalt("entity: " + obj.toString() + " does not contain id attribute!");
            }
        }
        else if (obj instanceof JAMSInteger) 
            ((JAMSDouble)obj).setValue(value);  
    }
    
    void setToPolyID(JAMSEntity src,JAMSEntity[] array,int value){
        Object obj = null;
        try {
        obj = src.getObject("to_poly");
        }catch(Exception e){
            this.getModel().getRuntime().sendHalt("entity: " + src.toString() + " does not contain to_poly attribute!");
        }
        if (obj instanceof JAMSDouble)
            ((JAMSDouble)obj).setValue((double)value);            
        else if (obj instanceof JAMSEntity){
            try{
                JAMSEntity to_poly_entity = null;
                for (int i=0;i<array.length;i++){
                    if (array[i] != null){
                        if ( (int)(array[i].getDouble("ID")) == value){
                            to_poly_entity = array[i];
                            break;
                        }
                    }                        
                }
                if (to_poly_entity != null)
                    src.setObject("to_poly", to_poly_entity);
            }catch(Exception e){
                this.getModel().getRuntime().sendHalt("entity: " + obj.toString() + " does not contain id attribute!");
            }
        }
        else if (obj instanceof JAMSInteger) 
            ((JAMSDouble)obj).setValue(value);  
        else{
            try{
                JAMSEntity to_poly_entity = null;
                for (int i=0;i<array.length;i++){
                    if (array[i] != null){
                        if ( (int)(array[i].getDouble("ID")) == value){
                            to_poly_entity = array[i];
                            break;
                        }
                    }                        
                }
                if (to_poly_entity != null)
                    src.setObject("to_poly", to_poly_entity);
            }catch(Exception e){
                this.getModel().getRuntime().sendHalt("entity: " + obj.toString() + " does not contain id attribute!");
            }
        }
            
    }
    
    int getToPolyID(JAMSEntity src){
        Object obj = null;
        try {
        obj = src.getObject("to_poly");
        }catch(Exception e){
            this.getModel().getRuntime().sendHalt("entity: " + src.toString() + " does not contain to_poly attribute!");
        }
        if (obj instanceof JAMSDouble)
            return (int)((JAMSDouble)obj).getValue();
        else if (obj instanceof JAMSEntity){
            try{
                return (int)((JAMSEntity)obj).getDouble("ID");
            }catch(Exception e){
                return -1;
                //this.getModel().getRuntime().sendHalt("entity: " + obj.toString() + " does not contain id attribute!");
            }
        }
        else if (obj instanceof JAMSInteger) 
            return ((JAMSInteger)obj).getValue();
        return -1;
    }
    
    int getToReachID(JAMSEntity src){
        Object obj = null;
        try {
        obj = src.getObject("to_reach");
        }catch(Exception e){
            this.getModel().getRuntime().sendHalt("entity: " + src.toString() + " does not contain to_poly attribute!");
        }
        if (obj instanceof JAMSDouble)
            return (int)((JAMSDouble)obj).getValue();
        else if (obj instanceof JAMSEntity){
            try{
                return (int)((JAMSEntity)obj).getDouble("ID");
            }catch(Exception e){
                return -1;
                //this.getModel().getRuntime().sendHalt("entity: " + obj.toString() + " does not contain id attribute!");
            }
        }
        else if (obj instanceof JAMSInteger) 
            return ((JAMSInteger)obj).getValue();
        return -1;
    }
    
    int getLiveIndex(HashMap<Integer,JAMSEntity> ref_cpy_sortedByID,int id){
        try{
            JAMSEntity e = ref_cpy_sortedByID.get(new Integer(id));
            if (e == null)
                return -1;
            return e.getInt("$live_index");
        }catch(NoSuchAttributeException e){
            this.getModel().getRuntime().sendHalt("no attribute $live_index " + e.toString());
            return -1;
        }
    }
    
    
    ArrayList<JAMSEntity> Method3(double threshold,JAMSEntity[] reference,JAMSEntity[] live){ 
        return HRUunification(threshold,reference, live,  "area");
    }
    ArrayList<JAMSEntity> Method4(double threshold,JAMSEntity[] reference,JAMSEntity[] live){ 
        return HRUunification(threshold,reference, live,  "area");
    }
    
    //achtung: live muss modifiziert werden
     @SuppressWarnings("unchecked")
    ArrayList<JAMSEntity> HRUunification(double threshold,JAMSEntity[] reference,JAMSEntity[] live, String attribute){     
        //wieso die ganzen entities mehrfach?
        // live .. die tatsächliche entitycollection in diesem durchlauf .. dort sind keine informationen über einen tatsächlich modelllauf gespeichert
        // reference .. entities aus einem testlauf .. diese dürfen nicht verändert werden
        // ref_cpy kopie von reference die verändert werden darf                        
        ArrayList<JAMSEntity> reducedEntityList = new ArrayList();
        JAMSEntity[] tmp = copyEntityArray(reference);     
        
        //schnelle zugriffe erstellen
        ArrayList<JAMSEntity> ref_cpy = new ArrayList<JAMSEntity>();
        HashMap<Integer,JAMSEntity> ref_cpy_sortedByID = new HashMap<Integer,JAMSEntity>();
        HashMap<Integer,HashSet<JAMSEntity>> ref_cpy_sortedByToPolyID = new HashMap<Integer,HashSet<JAMSEntity>>();
        
        for (int i=0;i<tmp.length;i++){
            ref_cpy.add(tmp[i]);
            try{
                ref_cpy_sortedByID.put(new Integer((int)tmp[i].getDouble("ID")), tmp[i]);
                int to_poly = getToPolyID(tmp[i]);
                HashSet<JAMSEntity> set = ref_cpy_sortedByToPolyID.get(to_poly);
                if (set != null)
                    set.add(tmp[i]);
                else{
                    set = new HashSet<JAMSEntity>();
                    set.add(tmp[i]);
                    ref_cpy_sortedByToPolyID.put(new Integer(to_poly), set);
                }
            }catch(NoSuchAttributeException e){
                this.getModel().getRuntime().sendHalt("HRU without ID");
            }
        }

        try{
            for (int i=0;i<live.length;i++){            
                int id = (int)live[i].getDouble("ID");
                JAMSEntity ref_entity = ref_cpy_sortedByID.get(new Integer(id));
                ref_entity.setInt("$live_index", i);
            }
        }catch(NoSuchAttributeException e){
            this.getModel().getRuntime().sendHalt("no attribute $live_index " + e.toString());
        }
                       
        int offsetCounter = 0;
        while (ref_cpy.size() > threshold*reference.length){                        
            Collections.sort(ref_cpy, new HRU_Comparator("area",false));                
            
            try{
                int id = (int)ref_cpy.get(offsetCounter).getDouble("ID");
                
                //int index_src = searchInEntityArray(id,live);
                
                int live_index = getLiveIndex(ref_cpy_sortedByID,id);
                JAMSEntity src = live[live_index];                                
                int to_poly = getToPolyID(src);
                
                if (to_poly == -1){
                    //versuchen mit hru zu vereeinigen die in denselben reach entwässert
                    int to_reach = getToReachID(live[live_index]);
                    if (to_reach != -1){
                        //isolierte hrus??
                        for (int i=0;i<live.length;i++){
                            if (live[i] != null){
                                if (getToReachID(live[i]) == to_reach && live[i] != src){
                                    to_poly = (int)live[i].getDouble("ID");
                                    HashSet<JAMSEntity> eSet = ref_cpy_sortedByToPolyID.get(new Integer(to_poly));
                                    setToPolyID(src,live,to_poly);
                                    setToPolyID(ref_cpy.get(offsetCounter),ref_cpy,to_poly);
                                    if (eSet == null){
                                        eSet = new HashSet<JAMSEntity>();
                                        eSet.add(ref_cpy.get(offsetCounter));
                                        ref_cpy_sortedByToPolyID.put(to_poly, eSet);
                                    }else{
                                        eSet.add(ref_cpy.get(offsetCounter));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    else{
                        //isolierte hrus??
                        this.getModel().getRuntime().println("isolated hru with id" + id);
                    }
                    if (to_poly == -1){
                        offsetCounter++;//etwas problematisch, weil die sortierung sich ändern kann .. dadurch können einige hrus übersehen werden, die eigentlich vereinigt werden könnten!
                        //there are no mory hrus which can be unified!
                        if (offsetCounter >= ref_cpy.size()){
                            this.getModel().getRuntime().println("stopped reduction with " + offsetCounter + " not removeable units");
                            break;
                        }                        
                        continue;
                    }
                }
                    
                JAMSEntity dest = live[getLiveIndex(ref_cpy_sortedByID,to_poly)];
                /*int index_dest = searchInEntityArray(to_poly,live);            
                JAMSEntity dest = live[index_dest];*/
                                                
                //nach hrus suchen die in src entwässern                
                HashSet<JAMSEntity> reRoutingSet_refcpy_id = ref_cpy_sortedByToPolyID.get(new Integer(id));
                HashSet<JAMSEntity> reRoutingSet_refcpy_to_poly = ref_cpy_sortedByToPolyID.get(new Integer(to_poly));
                if (reRoutingSet_refcpy_id != null){
                    Iterator<JAMSEntity> iter = reRoutingSet_refcpy_id.iterator();
                    while(iter.hasNext()){
                        JAMSEntity e = iter.next();
                        setToPolyID(e,ref_cpy,to_poly);
                        int index = e.getInt("$live_index");
                        if (index != -1 && live[index] != null)
                            setToPolyID(live[index],live,to_poly);
                    }
                    if (reRoutingSet_refcpy_to_poly != null)
                        reRoutingSet_refcpy_to_poly.addAll(reRoutingSet_refcpy_id);
                    else
                        ref_cpy_sortedByToPolyID.put(new Integer(to_poly), reRoutingSet_refcpy_id);
                }
                                                                                
                /*
                for (int i=0;i<live.length;i++){
                    if (live[i]!=null){
                        if (getToPolyID(live[i]) == id){
                            setToPolyID(live[i],live,to_poly);
                            //live[i].setDouble("to_poly", to_poly);
                        }
                        if (ref_cpy.size() > i){
                            if (getToPolyID(ref_cpy.get(i)) == id){
                                setToPolyID(ref_cpy.get(i),ref_cpy,to_poly);
                                //ref_cpy.get(i).setDouble("to_poly", to_poly);
                            }
                        }
                        
                    }
                }*/
                double area_src = src.getDouble("area");
                double area_dest = dest.getDouble("area");
                double area_new = area_dest+area_src;
                dest.setDouble("area", area_new);
                
                //in ref_cpy nach dest suchen               
                JAMSEntity dest_ref = ref_cpy_sortedByID.get(new Integer(to_poly));
                dest_ref.setDouble("area", area_new);
                                                
                ref_cpy_sortedByID.remove(new Integer(id));
                ref_cpy_sortedByToPolyID.remove(new Integer(id));
                ref_cpy.remove(offsetCounter);                   
                
                live[live_index] = null;                                    
                
            }catch(Exception e){
                System.out.println("failure! " + e.toString());
            }
        }
        for (int i=0;i<live.length;i++){
            if (live[i]!=null)
                reducedEntityList.add(live[i]);
        }
        return reducedEntityList;
    }
    
    ArrayList<JAMSEntity> reduce(double threshold,JAMSEntity[] reference,JAMSEntity[] live){
        if (this.method.getValue() == 1){
            return Method1(threshold,reference,live);
        }
        if (this.method.getValue() == 2){
            return Method2(threshold,reference,live);
        }
        if (this.method.getValue() == 3){
            return Method3(threshold,reference,live);
        }
        if (this.method.getValue() == 4){
            return Method4(threshold,reference,live);
        }
        return null;
    }
     @SuppressWarnings("unchecked")
    ArrayList<JAMSEntity> Method1(double threshold,JAMSEntity[] reference,JAMSEntity[] live){
        java.util.Arrays.sort(reference,new HRU_Comparator("area",true));
            
        ArrayList<JAMSEntity> reducedEntityList = new ArrayList<JAMSEntity>();
        for (int i=0;i<threshold*reference.length;i++){
            for (int j=0;j<live.length;j++){
                try{
                    if ( live[j].getDouble("ID") == reference[i].getDouble("ID")){
                        reducedEntityList.add(live[j]);
                        break;
                    }
                }catch(Exception e){
                    this.getModel().getRuntime().println("HRU - Entity does not contain ID!");
                }
            }            
        }

        return reducedEntityList;
    }
     @SuppressWarnings("unchecked")
    ArrayList<JAMSEntity> Method2(double threshold,JAMSEntity[] reference,JAMSEntity[] live){      
        java.util.Arrays.sort(reference,new HRU_Comparator("hruReductionValue",true));
            
        ArrayList<JAMSEntity> reducedEntityList = new ArrayList<JAMSEntity>();
        for (int i=0;i<threshold*reference.length;i++){
            for (int j=0;j<live.length;j++){
                 try{
                    if ( live[j].getDouble("ID") == reference[i].getDouble("ID")){
                        reducedEntityList.add(live[j]);
                        break;
                    }
                }catch(Exception e){
                    this.getModel().getRuntime().println("HRU - Entity does not contain ID!");
                }
            }            
        }

        return reducedEntityList;
    }
            
    //this is not really nice, but it have to be done here, because subsequently following contexts
    //wont have 
    public void init(){ 
        try{
            mySnapShot = (Snapshot)this.snapshot.getObject("snapshot");            
        }catch(Exception e){
            System.out.println("Could not find snapshot attribute: " + e.toString());
        }
                                                
        double maximumAcceptableLoss = 0.15;
        
        double maximumValue = 0.0;
        double bestAcceptablePoint = 1.0;
        double bestAcceptableValue = 0.0;
        
        double nextTestPoint = 0.5;
        double value = 0.0;
        double interval = 0.25;
        
        //erster durchlauf!
        maximumValue = singleRun();
        JAMSEntity ea_ref[] = copyEntityArray(this.hrus.getEntityArray());
        restore();
                
        bestAcceptableValue = maximumValue;
                        
        this.getModel().getRuntime().println("Starting HRU Reduction with Method " + this.method.getValue());
                        
        while (interval > 0.001){                                    
            JAMSEntity[] ea = hrus.getEntityArray();
            hrus.setEntities(reduce(nextTestPoint,ea_ref,ea));   
            value = singleRun();
            restore();
            
            this.getModel().getRuntime().println("Try reduction of " + (1.0-nextTestPoint) + " percent of all HRUs: get " + (1.0 - value/maximumValue) + " loss!");
            if (1.0 - value/maximumValue < maximumAcceptableLoss){
                bestAcceptableValue = value;
                bestAcceptablePoint = nextTestPoint;
                nextTestPoint -= interval;                
                this.getModel().getRuntime().println("Successful!");
            }else{
                nextTestPoint += interval; 
                this.getModel().getRuntime().println("not successful!");
            }
            interval /= 2.0;                                
        }
        this.getModel().getRuntime().println("Reduction finished, with " + (1.0-bestAcceptablePoint) + " percent!");
        JAMSEntity[] ea = hrus.getEntityArray();
        hrus.setEntities(reduce(bestAcceptablePoint,ea_ref,ea));   
    }
    public void run(){
        return;
    }
    public void cleanup(){
        return;
    }
}
