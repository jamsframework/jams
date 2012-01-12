/*
 * AttributeCounter.java
 * Created on 10. Juni 2008, 13:54
 *
 * This file is a JAMS component
 * Copyright (C) FSU Jena
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
package jams.components.debug;

import jams.data.Attribute;
import java.util.HashMap;
import jams.data.JAMSEntity;
import jams.data.JAMSEntityCollection;
import jams.data.JAMSInteger;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(title = "Title",
author = "Author",
description = "Description")
public class AttributeCounter extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Entity set to analyse")
    public JAMSEntityCollection entities;

    /*
     *  Component run stages
     */
    public void init() {
    }

    public void run() {
    }

    public void cleanup() throws JAMSEntity.NoSuchAttributeException {

        int count = entities.getEntities().size();
        if (count < 1) {
            getModel().getRuntime().println("No entities found!");
            return;
        }

        double bytes = 0;
        int unknownAttribs = 0;
        HashMap<Class, Long> typeMap = new HashMap<Class, Long>();

        Attribute.Entity e = entities.getEntityArray()[0];
        int attributeCount = e.getKeys().length;
        for (Object o : e.getKeys()) {
            Class clazz = e.getObject(o.toString()).getClass();
            Long size = typeMap.get(clazz);
            if (size == null) {
                size = 32l;
            }
            if (size.longValue() == 0) {
                unknownAttribs++;
            }
            bytes += size.longValue();
            getModel().getRuntime().println(o.toString() + "\t(" + size + "B)");
        }

        getModel().getRuntime().println("Calculated " + Math.round(bytes) + "B per entity."); 
        bytes *= count;
        
        int factor = 0;
        while (bytes > 1023) {
            bytes /= 1024;
            factor += 1;
        }
        String unit = "";
        
        if (factor > 2) {
            unit = "G";
        } else if (factor > 1) {
            unit = "M";
        } else if (factor > 0) {
            unit = "k";
        }
        unit += "B";
        
        getModel().getRuntime().println("Found " + unknownAttribs + " attributes with unknown size");
        getModel().getRuntime().println("Calculated " + bytes + unit + " for " + count + 
                " entities based on " + (attributeCount-unknownAttribs) + " out of " + 
                attributeCount + " attributes.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getObjectSize(JAMSInteger.class));
    }

    /**
     * Return the approximate size in bytes, and return zero if the class
     * has no default constructor.
     *
     * @param aClass refers to a class which has a no-argument constructor.
     */
    public static long getObjectSize(Class<?> aClass) {
        long result = 0;

        //if the class does not have a no-argument constructor, then
        //inform the user and return 0.
        try {
            aClass.getConstructor(new Class[]{});
        } catch (NoSuchMethodException ex) {
            System.err.println(aClass + " does not have a no-argument constructor.");
            return result;
        }

        //this array will simply hold a bunch of references, such that
        //the objects cannot be garbage-collected
        Object[] objects = new Object[fSAMPLE_SIZE];

        //build a bunch of identical objects
        try {
            Object throwAway = aClass.newInstance();

            long startMemoryUse = getMemoryUse();
            for (int idx = 0; idx < objects.length; ++idx) {
                objects[idx] = aClass.newInstance();
            }
            long endMemoryUse = getMemoryUse();

            float approximateSize = (endMemoryUse - startMemoryUse) / 100f;
            result = Math.round(approximateSize);
        } catch (Exception ex) {
            System.err.println("Cannot create object using " + aClass);
        }
        return result;
    }    // PRIVATE //
    private static int fSAMPLE_SIZE = 100;
    private static long fSLEEP_INTERVAL = 100;

    private static long getMemoryUse() {
        putOutTheGarbage();
        long totalMemory = Runtime.getRuntime().totalMemory();

        putOutTheGarbage();
        long freeMemory = Runtime.getRuntime().freeMemory();

        return (totalMemory - freeMemory);
    }

    private static void putOutTheGarbage() {
        collectGarbage();
        collectGarbage();
    }

    private static void collectGarbage() {
        try {
            System.gc();
            Thread.currentThread().sleep(fSLEEP_INTERVAL);
            System.runFinalization();
            Thread.currentThread().sleep(fSLEEP_INTERVAL);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
