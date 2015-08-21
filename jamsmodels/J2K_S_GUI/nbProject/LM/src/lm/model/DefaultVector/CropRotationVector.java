package lm.model.DefaultVector;

import java.util.ArrayList;
import java.util.TreeMap;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMCropRotationElement;
import lm.Componet.Vector.LMCropRotationVector;



public class CropRotationVector implements LMCropRotationVector{

    private TreeMap<Integer,LMCropRotationElement> cre;
    private TreeMap<Integer,Integer> arableUsesMap;
    private int lastDay;
    private int fallowDays;
    private int useDays;
    private int id;

    public CropRotationVector(int id){
        cre=new TreeMap();
        lastDay=0;
        this.id=id;
    }

    public CropRotationVector(TreeMap<Integer,LMCropRotationElement> cre,TreeMap<Integer,Integer> arableUsesMap,int lastDay,int fallowDays,int useDays,int id){
        this.cre=cre;
        this.arableUsesMap=arableUsesMap;
        this.lastDay=lastDay;
        this.fallowDays=fallowDays;
        this.useDays=useDays;
        this.id=id;
    }

    public CropRotationVector (ArrayList<LMArableID> a,int id){
        this.id=id;
        TreeMap<Integer,LMArableID>aidList=new TreeMap();
        cre=new TreeMap();
        lastDay=0;
        fallowDays=0;
        useDays=0;
        Boolean b=false;
        for(LMArableID aid:a){

            if(!aid.isFallow()){
               if(!b){
                   int fallowEnd=adjustBeginEnd(aid.getRelativeBegin()-1);
                    int fallowDuration=calculateFallowDuration((lastDay%365)+1,fallowEnd);
                    if(aidList.isEmpty()){
                        aidList.put(1,new ArableID((lastDay%365)+1,fallowEnd,fallowDuration));
                    }else{
                        aidList.put(aidList.lastKey()+1,new ArableID((lastDay%365)+1,fallowEnd,fallowDuration));
                    }
                    lastDay=lastDay+fallowDuration+aid.getDuration();
               }else{
                    lastDay=lastDay+aid.getDuration();
               }
               
               aidList.put(aidList.lastKey()+1, aid);
               
            }else{
                if(aidList.isEmpty()){
                    aid.setRelativeBegin(1);
                    aid.setRelativeEnd(adjustBeginEnd((1+aid.getDuration()-1)%365));
                    aidList.put(1, aid);
                    lastDay=lastDay+aid.getDuration();
                }else{
                    aid.setRelativeBegin(adjustBeginEnd(aidList.get(aidList.lastKey()).getRelativeEnd()+1));
                    aid.setRelativeEnd(adjustBeginEnd((aid.getRelativeBegin()+aid.getDuration()-1)%365));
                    aidList.put(aidList.lastKey()+1, aid);
                    lastDay=lastDay+aid.getDuration();
                }
            }
            b=aid.isFallow();

        }
        Integer key=aidList.firstKey();
        while(key!=null){
            cre.put(key, new CropRotationElement(this.id+"/"+key,aidList.get(key)));
            key=aidList.higherKey(key);
        }
        addInformations();


    }
    

    public void setElementAsFallow(Integer id) {
        if(cre.higherKey(id)!=null){
            LMArableID newAID=connectLMArable(cre.get(id-1).getLMArableID(),cre.get(id).getLMArableID(),cre.get(id+1).getLMArableID());
            cre.get(id-1).setLMArableID(newAID);
            cre.remove(id);
            cre.remove(id+1);
            id=cre.higherKey(id);
            while(id!=null){
                System.out.println("Add this Id --->" + id);
                cre.get(id).setActionCommand(this.id+"/"+(id-2));
                cre.put(id-2, cre.get(id));
                cre.remove(id);
                id=cre.higherKey(id);
            }
        }else{
            System.out.println(cre.get(id-1).getLMArableID());
            System.out.println(cre.get(id).getLMArableID());
            LMArableID newAID=connectLMArable(cre.get(id-1).getLMArableID(),cre.get(id).getLMArableID());
            cre.get(id-1).setLMArableID(newAID);
            cre.remove(id);
        }
        addInformations();
    }

    public void addElement(LMArableID lmArableID) {
        if(!cre.isEmpty()){
            int beginFallow=adjustBeginEnd(cre.get(cre.lastKey()).getEnd()+1);
            int endFallow=adjustBeginEnd(lmArableID.getRelativeBegin()-1);
            int durationFallow=calculateFallowDuration(beginFallow,endFallow);
            cre.put(cre.lastKey()+1, new CropRotationElement(id+"/"+(cre.lastKey()+1),new ArableID(beginFallow,endFallow,durationFallow)));
            cre.put(cre.lastKey()+1, new CropRotationElement(id+"/"+(cre.lastKey()+1),lmArableID));
        }else{
            int beginFallow=1;
            int endFallow=adjustBeginEnd(lmArableID.getRelativeBegin()-1);
            int durationFallow=calculateFallowDuration(beginFallow,endFallow);
            cre.put(1, new CropRotationElement(id+"/"+1,new ArableID(beginFallow,endFallow,durationFallow)));
            cre.put(2, new CropRotationElement(id+"/"+2,lmArableID));
        }
        addInformations();
    }

    public void addVector(TreeMap<Integer,LMCropRotationElement> cre,int multiplikator ){
        if(this.cre.size()==0){
            if(!cre.isEmpty()){
                Integer key=cre.firstKey();
                while(key!=null){
                    this.cre.put(key, new CropRotationElement(id+"/"+key,cre.get(key).getLMArableID()));
                    key=cre.higherKey(key);
                }
                if(multiplikator>1){
                    addVector(cre,multiplikator-1);
                }
            }
        }else{
            if(cre.size()!=0){
                for(int i=1;i<=multiplikator;i++){
                    int fallowBegin=adjustBeginEnd(this.cre.get(this.cre.lastKey()).getEnd()+1);
                    int fallowEnd=adjustBeginEnd(cre.get(cre.firstKey()).getBegin()-1);
                    int fallowDuration=((365-fallowBegin)+1)+cre.get(cre.firstKey()).getDuration();
                    int newKey=this.cre.lastKey()+1;
                    this.cre.put(newKey, new CropRotationElement(this.id+"/"+newKey ,new ArableID(fallowBegin,fallowEnd,fallowDuration)));
                    Integer key=cre.firstKey();
                    key=cre.higherKey(key);
                    while(key!=null){
                        newKey++;
                        this.cre.put(this.cre.lastKey()+1, new CropRotationElement(id+"/"+newKey,cre.get(key).getLMArableID()));
                        key=cre.higherKey(key);

                    }
                }
            }

        }
        addInformations();
    }

    public void reduceFallow(int element){
        if(element==1){
            cre.get(element).setDuration(adjustBeginEnd(cre.get(element+1).getBegin()-1));
        }else{
            cre.get(element).setDuration(this.calculateFallowDuration(cre.get(element-1).getEnd(), cre.get(element+1).getBegin()));
            
        }
        addInformations();
    }

    

    public void insertElementAtPosition(Integer position, LMArableID aid){
        int durationOfFallow=cre.get(position).getDuration();
        Integer key =cre.lastKey();
        while(key!=position){
            cre.put(key+2, cre.get(key));
            cre.get(key).setActionCommand(id+"/"+(key+2));
            cre.remove(key);
            key=cre.lowerKey(key);
        }
            cre.get(position).setEnd(adjustBeginEnd(aid.getRelativeBegin()-1));
        int duration=calculateFallowDuration(cre.get(position).getBegin(),cre.get(position).getEnd());
        cre.get(position).setDuration(duration);
        durationOfFallow=durationOfFallow-duration-aid.getDuration();
        cre.put(position+1, new CropRotationElement(id+"/"+(position+1),aid));
        cre.put(position+2, new CropRotationElement(id+"/"+(position+2),new ArableID(adjustBeginEnd(aid.getRelativeEnd()+1),adjustBeginEnd(cre.get(position+3).getBegin()-1),durationOfFallow)));
        addInformations();
    }

    public void insertElementLeftFromPosition(Integer position,LMArableID aid){
        System.out.println("diese Position wird aus ausgangspunkt genommen --->"+ position);
        Integer key=cre.lastKey();
        while(key!=position-1){
            System.out.println("dieser Key wird bearbeitet---->" + key);
            cre.put(key+2, cre.get(key));
            cre.get(key+2).setActionCommand(id +"/" + (key+2));
            cre.remove(key);
            key=cre.lowerKey(key);
        }
        position=position+2;
        int begin =adjustBeginEnd(aid.getRelativeEnd()+1);
        int end = adjustBeginEnd(cre.get(position).getBegin()-1);
        int duration=calculateFallowDuration(begin,end);
       cre.put(position-1,new CropRotationElement(id+"/"+(position-1),new ArableID(begin,end,duration)));
       cre.put(position-2, new CropRotationElement(id+"/"+(position-2),aid));
       cre.get(position-3).setEnd(adjustBeginEnd(aid.getRelativeBegin()-1));
       cre.get(position-3).setDuration(calculateFallowDuration(cre.get(position-3).getBegin(),cre.get(position-3).getEnd()));

        addInformations();
    }

    public void insertElementRightFromPosition(Integer position,LMArableID aid){
        System.out.println("diese Position wird aus ausgangspunkt genommen --->"+ position);
        Integer key=cre.lastKey();
        while(key!=position){
            System.out.println("dieser Key wird bearbeitet---->" + key);
            cre.put(key+2, cre.get(key));
            cre.get(key+2).setActionCommand(id +"/" + (key+2));
            cre.remove(key);
            key=cre.lowerKey(key);
        }
        int begin =adjustBeginEnd(cre.get(position).getEnd()+1);
        int end = adjustBeginEnd(aid.getRelativeBegin()-1);
        int duration=calculateFallowDuration(begin,end);
       cre.put(position+1,new CropRotationElement(id+"/"+(position+1),new ArableID(begin,end,duration)));
       cre.put(position+2, new CropRotationElement(id+"/"+(position+2),aid));
       if(position+2!=cre.lastKey()){
           System.out.println(cre.get(position+3).getActionCommand());
           cre.get(position+3).setBegin(adjustBeginEnd(aid.getRelativeEnd()+1));
           cre.get(position+3).setDuration(calculateFallowDuration(cre.get(position+3).getBegin(),cre.get(position+3).getEnd()));
       }
        addInformations();
    }

    public void maximizeFallow(Integer position, Integer years){
        cre.get(position).setDuration(cre.get(position).getDuration()+(365*years));
        addInformations();
    }

     public String getRowForSave(){
         String s=id +"\t";
         if(!cre.isEmpty()){
             Integer key=cre.firstKey();
             while (key!=null){
                 if(cre.get(key).isFallow()){
                     s=s+"/"+cre.get(key).getDuration();
                 }else{
                     s=s+cre.get(key).getLMArableID().getID();
                 }
                 if(key!=cre.lastKey()){
                     s=s+"\t";
                 }
                 key=cre.higherKey(key);
             }
         }

         return s;
     }

    private int adjustBeginEnd(int a){
        if(a==0){
            return 365;
        }else{
            if(a==366){
                return 1;
            }else{
                return a;
            }
        }
    }

    private void addInformations(){
        addAbsolutData();
        calculateCountOfDays();
        calculateArableUses();
    }

    private void calculateArableUses(){
        arableUsesMap=new TreeMap();
        if(!cre.isEmpty()){
            Integer key=cre.firstKey();
            while(key!=null){
                if(!cre.get(key).isFallow()){
                    if(arableUsesMap.containsKey(cre.get(key).getArableID())){
                        arableUsesMap.put(cre.get(key).getArableID(),arableUsesMap.get(cre.get(key).getArableID())+cre.get(key).getDuration());
                    }else{
                        arableUsesMap.put(cre.get(key).getArableID(), cre.get(key).getDuration());
                    }
                }
                key=cre.higherKey(key);
            }
        }
    }

    private void addAbsolutData(){
        int dayCache=1;
        if(!cre.isEmpty()){
            Integer key=cre.firstKey();
            while(key!=null){
                cre.get(key).setAbsolutBegin(dayCache);
                cre.get(key).setAbsolutEnd(dayCache+cre.get(key).getDuration()-1);
                dayCache=dayCache+cre.get(key).getDuration();
                key=cre.higherKey(key);
            }
        }
    }

    private void calculateCountOfDays(){
        if(!cre.isEmpty()){
            Integer key=cre.firstKey();
            while(key!=null){
                if(cre.get(key).isFallow()){
                    fallowDays=fallowDays+cre.get(key).getDuration();
                }else{
                    useDays=useDays+cre.get(key).getDuration();
                }
                key=cre.higherKey(key);
            }
        }else{
            fallowDays=0;
            useDays=0;
        }
    }

    private int calculateFallowDuration(int begin , int end){
        System.out.println("begin --->"+begin + "end ---->"+end);
        if(end+1-begin==0){
            return 0;
        }
        if(end==365 && begin==1){
            return 0;
        }
        if(begin<end){
            return (end-begin)+1;
        }
        if(begin>end){
            return (end+365)-begin+1;
        }
        return 1;
    }

    private LMArableID connectLMArable(LMArableID a,LMArableID b,LMArableID c){
        return new ArableID(a.getRelativeBegin(),c.getRelativeEnd(),a.getDuration()+b.getDuration()+c.getDuration());
    }

    private LMArableID connectLMArable(LMArableID a,LMArableID b){
        return new ArableID(a.getRelativeBegin(),b.getRelativeEnd(),a.getDuration()+b.getDuration());
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LMCropRotationVector clone(TreeMap<Integer,LMArableID> aidTree){
        TreeMap<Integer,LMCropRotationElement> newTree=new TreeMap();
        if(!cre.isEmpty()){
            Integer key=cre.firstKey();
            while(key!=null){
                if(cre.get(key).isFallow()){
                    newTree.put(key, cre.get(key).clone(cre.get(key).getLMArableID().clone()));
                }else{
                    newTree.put(key,cre.get(key).clone(aidTree.get(cre.get(key).getArableID())));
                }
                key=cre.higherKey(key);
            }
        }
        return new CropRotationVector(newTree, (TreeMap<Integer, Integer>) arableUsesMap.clone(),lastDay,fallowDays,useDays,id);
    }


    public void deleteElement(Integer i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LMCropRotationElement getLMCropRotationElement(Integer i) {
        return this.cre.get(i);
    }

    public void setCRE(TreeMap<Integer, LMCropRotationElement> a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TreeMap<Integer, LMCropRotationElement> getCRE() {
        return this.cre;
    }

    public void setAID(ArrayList a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList getAID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setUseDays(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getUseDays() {
        return useDays;
    }

    public void setFallowDays(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getFallowDays() {
        return fallowDays;
    }

    public void setArableUses(TreeMap<Integer, Integer> i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TreeMap<Integer, Integer> getArableUses() {
        return arableUsesMap;
    }

    public void setLastDay(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLastDay() {
        return this.lastDay;
    }


}