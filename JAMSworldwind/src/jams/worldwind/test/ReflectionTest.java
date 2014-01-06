/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.test;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
class A {

    private int a = 1;

    public void setA(int a) {
        this.a = a;
    }

    public int getA() {
        return this.a;
    }
}

class B extends A {

    private int b = 2;

    public void setB(int b) {
        this.b = b;
    }

    public int getB() {
        return this.b;
    }
}

/**
 *
 * @author bigr
 */
public class ReflectionTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ReflectionTest();
    }

    /**
     *
     */
    public ReflectionTest() {
        System.out.println("Starting...");
        //JamsShapeAttributes cb = new jams.worldwind.shapefile.JamsShapeAttributes();
        B cb = new B();
        BeanInfo beanInfo;
        Object[][] data;
        int i = 0;
        try {
            beanInfo = Introspector.getBeanInfo(cb.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
                    System.out.println("METHOD: " + pd.getReadMethod().getName());
                    
                    System.out.println("VALUE:  " + pd.getReadMethod().invoke(cb));

                }
                i++;
            }
        } catch (Exception ex) {
            //Logger.getLogger(PropertyEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Finished!");
    }
}
