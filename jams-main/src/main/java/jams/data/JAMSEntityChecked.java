/*
 * JAMSEntity.java
 * Created on 2. August 2005, 21:04
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
package jams.data;

import com.vividsolutions.jts.geom.Geometry;
import java.util.*;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class JAMSEntityChecked extends JAMSEntity {

    private HashMap<String, Object> values = new HashMap<String, Object>();

    private long id = -1;

    @Override
    public void setFloat(String name, float attribute) {
        JAMSFloatChecked v = (JAMSFloatChecked) this.values.get(name);
        if (v!=null){
            v.setValue(attribute);
        }else{
            this.values.put(name, new JAMSFloatChecked(attribute));
        }
    }

    @Override
    public void setDouble(String name, double attribute) {
        JAMSDoubleChecked v = (JAMSDoubleChecked) this.values.get(name);
        if (v!=null){
            v.setValue(attribute);
        }else{
            this.values.put(name, new JAMSDoubleChecked(attribute));
        }
    }
    
    @Override
    public float getFloat(String name) throws JAMSEntityChecked.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((Attribute.Float) values.get(name)).getValue();
        } else {
            throw new JAMSEntityChecked.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(float)_not_found!"));
        }
    }

    @Override
    public double getDouble(String name) throws JAMSEntityChecked.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((Attribute.Double) values.get(name)).getValue();
        } else {
            throw new JAMSEntityChecked.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(double)_not_found!"));
        }
    }

    //that's crap
    @Override
    public void setValue(String value) {
        StringTokenizer st1 = new StringTokenizer(value, "\t");
        while (st1.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "=");
            String name = st2.nextToken().trim();
            String val = st2.nextToken().trim();
            try {
                JAMSDoubleChecked d = new JAMSDoubleChecked(Double.parseDouble(val));
                values.put(name, d);
            } catch (NumberFormatException nfe) {
                System.out.println("\"" + val + "\" is not a valid double expression!");
                nfe.printStackTrace();
            }
        }
    }
}
