package jams.explorer.gui;

import jams.data.Attribute.TimeInterval;
import jams.explorer.gui.DataCollectionView.DataType;

public interface DataCollectionViewDelegate {
    
    public DataType[] getAvailableDataTypes();
    public Object[] getItemIdentifiersForDataType(DataType type);
    public Object getItemForIdentifier(Object identifier);
    public TimeInterval getTimeInterval(Object item);
    public Integer[] getSimulationIDs();
    public boolean hasTimeInterval(Object item);
    public boolean isMultirun(Object item);
    public void itemIsBeingDisplayed(Object item);
}
