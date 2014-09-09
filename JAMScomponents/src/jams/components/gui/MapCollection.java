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
import java.util.TreeSet;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MapCollection {
    
    private String desc;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> fc;
    private TreeSet<Double> s;
    private DefaultMapContext map;
    private Style st;
    private Map colors;
    private String rangeColor;
    private int numRanges;
    private CollectionDataStore cds;
    private String attributeName;
    
    public MapCollection(String desc, FeatureCollection<SimpleFeatureType, SimpleFeature> fc, String attibuteName, String color, int numRanges, CoordinateReferenceSystem crs) {
        this.desc = desc;
        this.fc = fc;
        this.attributeName = attibuteName;
        this.s = buildValueSet(fc, attibuteName);
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
    
    public void update(){
        TreeSet<Double> s2 = buildValueSet(fc, attributeName);
        if (s2.first() < s.first() || s2.last() > s.last()) {
            s.addAll(s2);

            Map a = MapLegend.coloring(this.s, this.numRanges, this.rangeColor);
            this.colors = a;
            this.st = MapLegend.style;
            for (MapLayer layer : map.getLayers()) {
                layer.setStyle(st);
            }
        }        
    }
    private TreeSet<Double> buildValueSet(FeatureCollection<SimpleFeatureType, SimpleFeature> fc, String attibuteName){
        TreeSet<Double> s = new TreeSet<Double>();
        FeatureIterator fi = fc.features();
        while(fi.hasNext()){
            Feature f = fi.next();
            if ( f instanceof SimpleFeature){
                SimpleFeature sf = (SimpleFeature)f;                
                try{
                    double value = (Double)sf.getAttribute(attibuteName);
                    s.add(value);
                }catch(Throwable t){
                    t.printStackTrace();
                }
            }
        }
        return s;
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
