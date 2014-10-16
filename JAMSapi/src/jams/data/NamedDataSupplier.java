/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.data;

/**
 *
 * @author christian
 */
public interface NamedDataSupplier<T> extends DataSupplier<T>{
    abstract public String getName();
}