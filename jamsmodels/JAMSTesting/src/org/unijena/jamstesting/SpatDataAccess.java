/*
 * SpatDataAccess.java
 * Created on 2. November 2005, 13:12
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.jamstesting;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */
public class SpatDataAccess extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            ) 
    public Attribute.Entity entity;   

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            ) 
    public double d;   
    
/*
 *
 *-model D:\jamsmodels\JAMSTesting\smallmodel.xml -libs D:\jamsprojects\Thornthwaite\dist\Thornthwaite.jar D:\jamsprojects\JAMSTesting\dist\JAMSTesting.jar
 *
 */
    long x;
    Attribute.Long l;
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        //x = entity.getLong("x"); //771
        x = ((Attribute.Long) entity.getObject("x")).getValue();
        System.out.println(x);
    }
}
