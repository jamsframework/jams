/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.core;

import optas.optimizer.management.SampleFactory;

/**
 *
 * @author christian
 */
    abstract public class AbstractDataSerie extends AbstractModel{
        abstract public SampleFactory.Sample getNext();    
        abstract public void reset();
    }
