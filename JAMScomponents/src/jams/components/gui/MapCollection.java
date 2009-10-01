package jams.components.gui;

import java.util.Map;
import java.util.Set;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.styling.Style;

public class MapCollection {
    
    private String desc;
    private FeatureCollection fc;
    private Set<Double> s;
    private DefaultMapContext map;
    private Style st;
    private Map colors;
    private String rangeColor;
    private int numRanges;
    private CollectionDataStore cds;
    
    @SuppressWarnings("deprecation")
    public MapCollection(String desc, FeatureCollection fc, Set<Double> s, String color, int numRanges) throws Exception {
        this.desc = desc;
        this.fc = fc;
        this.s = s;
        this.st = null;
        this.map = new DefaultMapContext();
        this.rangeColor = color;
        this.numRanges = numRanges;
        this.cds = new CollectionDataStore(this.fc);
        Map a = MapLegend.coloring(this.s, this.numRanges, this.rangeColor);
        this.st = MapLegend.style;
        this.colors = a;
        DefaultMapLayer layer = new DefaultMapLayer(this.fc, this.st );
        map.addLayer(layer);
    }
    
    public String getDesc() {
        return this.desc;
    }
    
    public DefaultMapContext getMapContext() {
        return this.map;
    }
    
    public Object[] getRanges() {
        return this.colors.values().toArray();
    }
    
    public Object[] getColors() {
        return this.colors.keySet().toArray();
    }
    
    public FeatureType getCollectionType() {
        return this.fc.features().next().getFeatureType();
    }
    
    public CollectionDataStore asCollectionDataStore() {
        return this.cds;
    }
    
    public Style getStyle() {
        return this.st;
    }
    
    
}
