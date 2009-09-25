/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.workspace;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface DataSet {

    DataValue[] getData();

    void setData(int index, DataValue data);

    void setData(DataValue[] data);

    @Override
    String toString();

}
