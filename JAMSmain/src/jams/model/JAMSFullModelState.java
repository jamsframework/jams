/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import jams.runtime.JAMSClassLoader;
import jams.runtime.RuntimeLogger;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 *
 * @author Christian Fischer
 */
public class JAMSFullModelState implements FullModelState{
    private SmallModelState state;
    private byte[] data;
    
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
            return classLoader;
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
    
    public JAMSFullModelState(SmallModelState state, Model model) throws IOException{
        this.setSmallModelState(state);
        this.setModel(model);
    }
    
    public JAMSFullModelState(File file) throws ClassNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(file);
        ClassLoaderObjectInputStream objIn = new ClassLoaderObjectInputStream(fis);
        JAMSFullModelState _this = (JAMSFullModelState) objIn.readObject();
        this.state = _this.state;
        this.data = _this.data;        
        objIn.close();
        fis.close();

    }
    
    public SmallModelState getSmallModelState(){
        return state;
    }
    public void setSmallModelState(SmallModelState state){
        this.state = state;
    }

    public Model getModel(){
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        try {
            ClassLoaderObjectInputStream objIn = new ClassLoaderObjectInputStream(inStream);
             //has to be read first, because we must load the libaries before we can 
            //load the rest of JAMSRuntime
            String libs[] = (String[]) objIn.readObject();
            // load the libraries and create the class loader        
            ClassLoader classLoader = JAMSClassLoader.createClassLoader(libs, new RuntimeLogger());            
            objIn.setClassLoader(classLoader);
            
            Model model = (Model) objIn.readObject();                       
            objIn.close();
            inStream.close();
            return model;
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            return null;
        }    
    }
            
    public void setModel(Model model) throws IOException{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(outStream);
        //has to be written first, because we must load the libaries before we can 
        //load the rest of JAMSRuntime
        JAMSRuntime rt = model.getRuntime();
        if (rt instanceof StandardRuntime)
            objOut.writeObject(((StandardRuntime)rt).getLibs());
        else
            objOut.writeObject(new String[1]);
        
        objOut.writeObject(model);
        objOut.flush();
        outStream.flush();
        this.data = outStream.toByteArray();
        objOut.close();
        outStream.close();
    }
        
    public void writeToFile(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream objOut = new ObjectOutputStream(fos);        
        objOut.writeObject(this);
        objOut.flush();
        fos.flush();
        fos.close();
    }        
}
