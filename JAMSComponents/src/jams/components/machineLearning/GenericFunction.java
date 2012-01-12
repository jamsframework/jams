/*
 * GenericCost.java
 * Created on 12. Mai 2006, 19:37
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package jams.components.machineLearning;

import java.util.Vector;

/**
 *
 * @author Christian Fischer
 */

public class GenericFunction {
    private static long nextID = 0;
    
    private ActivationFunction Function = null;
    private ActivationFunction dFunction = null;
    private String description, name;
    private int polyDegree;
    private int functionType;
    public Long id = null;
    private Matrix M;
    private static Vector<Long> idList = new Vector<Long>();
    
    public GenericFunction(Long id, String name, String description, int functionType, int polyDegree, Matrix M) {
        this.registerID(id);
        if (functionType == ActivationFunction.EXPO)
            this.Function = new Exponential(M);
        else if (functionType == ActivationFunction.LINAPPROX)
            this.Function = new LinApprox(M);
        else if (functionType == ActivationFunction.POLY)
            this.Function = new Polynom(M,polyDegree);
        this.dFunction = Function.derive();
        this.description = description;
        this.name = name;
        this.polyDegree = polyDegree;
        this.functionType = functionType;
        this.M = M;
    }
    
    public GenericFunction(ActivationFunction af) {
        this.Function = af;
        this.dFunction = Function.derive();
        this.description = "";
        this.name = "";
        this.polyDegree = 1;
        this.functionType = af.getType();
        this.M = null;
    }
    
    public GenericFunction(String name, String description, int functionType, int polyDegree, Matrix M) {
        this(getNewID(), name, description, functionType, polyDegree, M);
    }
    
    private static Long getNewID() {
        
        Long id = new Long(nextID++);
        while (idList.contains(id)) {
            id = new Long(nextID++);
        }
        return id;
    }
    
    private void registerID(Long id) {
        
        this.id = id;
        this.idList.add(id);
        
    }
    
    public ActivationFunction getFunction() {
        return this.Function;
    }
    
    public ActivationFunction getDFunction() {
        return this.dFunction;
    }
    
    public String getName() {
        return name;
    }
        
    public String getDescription() {
        return this.description;
    }
    
    public int getPolyDegree() {
        return this.polyDegree;
    }
    
    public Matrix getMatrix() {
        return M;
    }
    
    public int getFunctionType() {
        return this.functionType;
    }
    
    public Long getID() {
        return this.id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }
    
    public void setPolyDegree(int polyDegree) {
        this.polyDegree = polyDegree;
    }
    
    public void setMatrix(Matrix M) {
        this.M = M;
    }
        
    public String toString() {
        return this.name;
    }
}
