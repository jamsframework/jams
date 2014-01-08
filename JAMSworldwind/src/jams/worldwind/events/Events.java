package jams.worldwind.events;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public interface Events {
    public static final String ACTIVE_LAYER_CHANGED = "jams.worldwind.events.ActiveLayerChanged";
    public static final String BOUNDINGBOXOFSHAPEFILE = "jams.worldwind.events.BoundingBoxOfShapefile";
    public static final String DATATRANSFER3DDATA_APPEND = "jams.worldwind.events.DataTransfer3DAppend";
    public static final String FOUND_RENDERABLE_UNDER_CURSOR = "jams.worldwind.events.FoundRenderableUnderCursor";
    public static final String INTERVALL_CALCULATED = "jams.worldwind.events.IntervallCalculated";
    public static final String INTERVALL_COLORS_SET = "jams.worldwind.events.IntervallColorsSet";
    public static final String LAYER_ADDED = "jams.worldwind.events.LayerAdded";
    public static final String LAYER_CHANGED = "jams.worldwind.events.LayerChanged";
    public static final String LAYER_REMOVED = "jams.worldwind.events.LayerRemoved";
}
