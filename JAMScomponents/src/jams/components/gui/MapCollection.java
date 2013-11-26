/*
 * MapCollection.java
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.gui;

import java.util.Map;
import java.util.Set;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.styling.Style;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MapCollection {
    
    private String desc;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> fc;
    private Set<Double> s;
    private DefaultMapContext map;
    private Style st;
    private Map colors;
    private String rangeColor;
    private int numRanges;
    private CollectionDataStore cds;
    
    public MapCollection(String desc, FeatureCollection<SimpleFeatureType, SimpleFeature> fc, Set<Double> s, String color, int numRanges, CoordinateReferenceSystem crs) {
        this.desc = desc;
        this.fc = fc;
        this.s = s;
        this.st = null;
        this.map = new DefaultMapContext(crs);
        this.rangeColor = color;
        this.numRanges = numRanges;
        this.cds = new CollectionDataStore(this.fc);
        Map a = MapLegend.coloring(this.s, this.numRanges, this.rangeColor);
        this.st = MapLegend.style;
        this.colors = a;
        DefaultMapLayer layer = new DefaultMapLayer(this.fc, this.st);
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
    
    public CollectionDataStore asCollectionDataStore() {
        return this.cds;
    }
    
    public Style getStyle() {
        return this.st;
    }
}
