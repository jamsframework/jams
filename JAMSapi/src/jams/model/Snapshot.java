/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import java.io.Serializable;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Snapshot extends Serializable {

    byte[] getData();

}
