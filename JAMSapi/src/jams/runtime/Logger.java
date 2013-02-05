/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.runtime;

import java.io.Serializable;
import java.util.Observer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Logger extends Serializable {

    void addObserver(Observer o);

    String getLastString();

    void print(String str);

    void println(String str);

    String toString();
    
}
