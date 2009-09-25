/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.workspace.stores;

import java.io.IOException;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface OutputDataStore extends DataStore {

    void flush() throws IOException;

    String[] getAttributes();

    Filter[] getFilters();

    void open() throws IOException;

    void write(Object o) throws IOException;

}
