/*
 * SimpleComponent.java
 *
 * Created on 24. Juni 2005, 23:22
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
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSComponentDescription;
import org.unijena.jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */

public class SimpleComponent1 extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            protected int x = 12;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            protected String s = "STRIKE";    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            protected int y = 7;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            protected int z = 3;
    
    /** Creates a new instance of SimpleComponent */
    public SimpleComponent1() {
    }
    
    public void init(){}
    
    public void run(){
        System.out.println("SimpleComponent1");
    }
    
    public void cleanup(){}
        
}
