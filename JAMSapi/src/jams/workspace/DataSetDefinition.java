/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.workspace;

import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface DataSetDefinition {

    void addAttribute(String attributeName, Class type);

    Object getAttributeValue(String attributeName, int column);

    ArrayList<Object> getAttributeValues(String attributeName);

    ArrayList<Object> getAttributeValues(int column);

    Set<String> getAttributes();

    int getColumnCount();

    Class getType(String attributeName);

    void removeAttribute(String attributeName);

    boolean setAttributeValues(String attributeName, ArrayList<Object> values);

    boolean setAttributeValues(String attributeName, Object value);

    boolean setAttributeValues(int column, ArrayList<Object> values);

    String toASCIIString();

}
