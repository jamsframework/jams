package reg.gui;

import jams.data.Attribute.TimeInterval;
import reg.gui.DataCollectionView.DataType;

public interface DataCollectionViewDelegate {
    
    public DataType[] getAvailableDataTypes();
    public Object[] getItemIdentifiersForDataType(DataType type);
    public Object getItemForIdentifier(Object identifier);
    public TimeInterval getTimeInterval(Object item);
    public boolean hasTimeInterval(Object item);
    public void itemIsBeingDisplayed(Object item);
}
