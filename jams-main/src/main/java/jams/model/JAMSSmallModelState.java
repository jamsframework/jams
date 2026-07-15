/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import jams.tools.JAMSTools;
import jams.workspace.stores.DataStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Fischer
 */
public class JAMSSmallModelState implements SmallModelState{    
    HashMap<String, byte[]> dataStoreStateMap;
    long executionTime;
    transient ClassLoader loader;
    
    public JAMSSmallModelState(ClassLoader loader){       
        this.loader = loader;
        dataStoreStateMap = new HashMap<String, byte[]>();
    }
                    
    public class ClassLoaderObjectInputStream extends ObjectInputStream {

        private ClassLoader classLoader;

        public ClassLoaderObjectInputStream(InputStream in) throws IOException {
            super(in);
            classLoader = null;
        }

        public void setClassLoader(ClassLoader loader){
            this.classLoader = loader;
        }
        
        public ClassLoader getClassLoader(){
            return this.classLoader;
        }
        
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

            try {
                //this should be the default behaviour of ObjectInputStream
                if (classLoader == null){
                    classLoader = Thread.currentThread().getContextClassLoader();
                }
                String name = desc.getName();
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException e) {
                return super.resolveClass(desc);
            }
        }
    }
    
    @Override
    public void recoverDataStoreState(DataStore store) throws IOException{
        Logger.getLogger(JAMSSmallModelState.class.getName()).log(Level.FINE, "recover:{0}", store.getID());
        byte[] data = dataStoreStateMap.get(store.getID());
        if (data == null){
            Logger.getLogger(JAMSSmallModelState.class.getName(),"recover:" + store.getID());
            Logger.getLogger(JAMSSmallModelState.class.getName()).log(Level.SEVERE, "could not recover:{0}", store.getID());
            throw new IOException("could not recover:" + store.getID());
        }
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        ClassLoaderObjectInputStream in = new ClassLoaderObjectInputStream(inStream);
        in.setClassLoader(loader);
        try{            
            JAMSTools.cloneInto(store, in.readObject(),store.getClass());            
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }
    }
    @Override
    public void saveDataStoreState(DataStore state){
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try{
            ObjectOutputStream objOut = new ObjectOutputStream(outStream);            
            objOut.writeObject(state);
            objOut.flush();
            objOut.close();
            outStream.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println(ioe);
        }
        dataStoreStateMap.put(state.getID(), outStream.toByteArray());
    }
    
    @Override
    public void setExecutionTime(long time){
        executionTime = time;
    }
    @Override
    public long getExecutionTime(){
        return executionTime;
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();        
        //try to get 
        if (in instanceof JAMSFullModelState.ClassLoaderObjectInputStream){
            this.loader = ((JAMSFullModelState.ClassLoaderObjectInputStream)in).getClassLoader();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();        
    }
}
