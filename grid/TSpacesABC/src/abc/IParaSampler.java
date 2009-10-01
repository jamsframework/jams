/*
 * IParaSampler.java
 *
 * Created on 13. Februar 2007, 11:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package abc;

/**
 *
 * @author ncb
 */
public interface IParaSampler {
    void initialze();
    boolean hasNext();
    boolean getNext();
}
