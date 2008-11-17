/*
 * JAMSDataFactory.java
 * Created on 24. November 2005, 07:33
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.data;

import jams.runtime.JAMSRuntime;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class JAMSDataFactory {

    //public JAMSDataFactory
    public static JAMSEntity createEntity() {
        return new JAMSCheckedEntity();
    }

    public static JAMSData getData(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            System.out.println(JAMS.resources.getString("class_not_found:") + className);
            System.exit(0);
        }
        JAMSData dataObject = (JAMSData) clazz.newInstance();

        return dataObject;
    }

    public static JAMSData getInstance(String className, JAMSRuntime rt) {
        JAMSData object = null;
        try {
            object = getData(className);
        } catch (ClassNotFoundException cnfe) {
            rt.handle(cnfe);
        } catch (InstantiationException ie) {
            rt.handle(ie);
        } catch (IllegalAccessException iae) {
            rt.handle(iae);
        }
        return object;
    }
    
    public static JAMSData getInstance(Class clazz, JAMSRuntime rt) {
        JAMSData object = null;
        try {
            object = (JAMSData) clazz.newInstance();
        } catch (InstantiationException ie) {
            rt.handle(ie);
        } catch (IllegalAccessException iae) {
            rt.handle(iae);
        }
        return object;
    }    
}
