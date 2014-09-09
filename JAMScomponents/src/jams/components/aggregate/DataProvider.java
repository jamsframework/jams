/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.aggregate;

/**
 *
 * @author christian
 */
public interface DataProvider<T> {
    abstract public int size();
    abstract T get(int i);
}