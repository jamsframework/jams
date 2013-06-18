/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.data;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author christian
 * This class provides static memory management of double arrays. This avoids
 * extensive use of the garbage collector. 
 * History: DoubleArrayPool was static, but this requires use of synchronized
 *          blocks, which result in tremendous performance losses. 
 */
public class ArrayPool<T> implements Serializable {

    static final int DefaultInitalSize = 100;
    
    private class IdList implements Serializable {

        Object[] freeArrays;
        int lastIndex;
        static final int initialAlloc = 8;

        public IdList() {
            freeArrays = null;
            lastIndex = -1;
        }

        public T alloc() {
            if (lastIndex >= 0) {
                return (T)freeArrays[lastIndex--];
            } else {
                return null;
            }
        }

        public void free(T array) {
            lastIndex++;
            if (freeArrays == null) {
                freeArrays = new Object[initialAlloc];//(T[]) Array.newInstance(c,initialAlloc);// new T[initialAlloc][];
            }
            if (freeArrays.length <= lastIndex) {
                freeArrays = Arrays.copyOf(freeArrays, freeArrays.length * 2);
            }
            freeArrays[lastIndex] = array;           
        }
    }
    IdList[] freeIdLists;
    int maxSize = 0;
    
    int allocatedObjects = 0;
    int Power = 1000000;
        
    Class c;
    public ArrayPool(Class c){
        this.c = c;
        expandPool(DefaultInitalSize);
    }
    
    public ArrayPool(Class c, int n){
        expandPool(n);        
    }
    
    public T alloc(int size) {        
        if (size >= maxSize) {
            allocatedObjects++;
            return (T) Array.newInstance(c,size); 
        }
        IdList freeIdList = freeIdLists[size];
        
        T freeId = freeIdList.alloc();
        if (freeId == null) {
            allocatedObjects++;
            if (allocatedObjects >= Power) {
                System.out.println("Memory Pool allocated: " + allocatedObjects);
                Power *= 10;
            }
            return (T) Array.newInstance(c,size); 
        }
        return freeId;
    }
    
    public T free(T array) {
        int size = Array.getLength(array);

        if (size >= maxSize) {
            expandPool(size);            
        }        
        //Arrays.fill((double[])array, 0);
        freeIdLists[size].free(array);
        return null;
    }

    private void expandPool(int size) {

        int oldSize = maxSize;
        maxSize = size + 1;

        IdList oldList[] = freeIdLists;
        freeIdLists = (IdList[]) Array.newInstance(IdList.class,maxSize);
        if (oldList != null)
            System.arraycopy(oldList, 0, freeIdLists, 0, oldSize);
        
        for (int i = oldSize; i < maxSize; i++) {
            freeIdLists[i] = new IdList();
        }
    }
}
