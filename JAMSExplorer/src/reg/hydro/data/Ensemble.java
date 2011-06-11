/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.hydro.data;

import java.util.HashMap;

/**
 *
 * @author chris
 */
public class Ensemble extends DataSet{
    protected int size;
    protected Integer id[];
    private HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

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
    protected Integer getId(int index){
        return this.id[index];
    }
    public int getSize(){
        return this.size;
    }
    public String getName(){
        return name;
    }
}
