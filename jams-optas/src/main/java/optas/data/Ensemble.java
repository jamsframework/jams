/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author chris
 */
public abstract class Ensemble extends DataSet{
    protected int size;
    private Integer id[];
    private HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    @Override
    abstract public Ensemble clone();

    public Ensemble(int size){
        this.size = size;
        id = new Integer[size];
    }
    public Ensemble(Ensemble e){
        super(e);
        this.size = e.size;
        this.id = e.id;
        this.map = e.map;
    }
    protected void update(){
        for (int i=0;i<id.length;i++)
            map.put(id[i], new Integer(i));
    }
    protected int getIndex(Integer id){
        return map.get(id);
    }
    protected void set(int index, Integer id){
        map.put(this.id[index], null);

        this.id[index] = id;
        map.put(id, index);
    }
    public Integer getId(int index){
        return this.id[index];
    }
    public int getSize(){
        return this.size;
    }
    public String getName(){
        return name;
    }
    public Integer[] getIds(){
        return Arrays.copyOf(id, size);
    }

    public void retainIds(Integer ids[]){
        TreeSet<Integer> set1 = new TreeSet<Integer>();
        set1.addAll(Arrays.asList(ids));

        TreeSet<Integer> set2 = new TreeSet<Integer>();
        set2.addAll(Arrays.asList(getIds()));

        set2.removeAll(set1);
        for (Integer i : set2)
            removeId(i);
    }

    public void removeId(Integer id){
        size--;
        int index = getIndex(id);
        this.map.remove(id);

        if(size>0){
            if (index != size){
                this.id[index] = this.id[size];
                map.put(this.id[index], index);
            }
        }
    }
}
