/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.util.HashMap;

/**
 *
 * @author chris
 */
public class ObjectPool<T extends Identifiable> {

    T pool[];
    HashMap<Integer, Integer> map;
    int size = 0;
    int freeIndex[];
    int lastFreeIndex;

    ObjectPool(T[] pool) {
        this.pool = pool;
        init(pool.length);
    }

    private void init(int size){
        map = new HashMap<Integer, Integer>();

        this.size = size;
        freeIndex = new int[size];
        reset();
    }

    T allocate() {
        return pool[freeIndex[lastFreeIndex--]];
    }

    public double load() {
        return 1.0 - ((double) this.lastFreeIndex / (double) this.size);
    }

    void reset() {
        for (int i = 0; i < size; i++) {
            freeIndex[i] = i;
            map.put(pool[i].getID(), i);
        }
        lastFreeIndex = size - 1;
    }

    void free(T obj) {
        int index = map.get(obj.getID());
        freeIndex[++lastFreeIndex] = index;
    }
}
