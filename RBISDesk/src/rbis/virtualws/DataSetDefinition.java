package rbis.virtualws;

import java.util.ArrayList; 

public class DataSetDefinition {

    private int colNumber;

    private ArrayList<Class> dataTypes;

    public AttributeDefinition getAttribute (String title) {
        return null;
    }

    public Object getAttributeValue (AttributeDefinition attribute, int column) {
        return null;
    }

    public ArrayList<Object> getAttributeValues (AttributeDefinition attribute) {
        return null;
    }

    public ArrayList<Object> getAttributeValues (int column) {
        return null;
    }

    public void removeAttribute (AttributeDefinition attribute) {
    }

    public void addAttribute (Class type) {
    }

    ArrayList<AttributeDefinition> getAttributes () {
        return null;
    }

}

