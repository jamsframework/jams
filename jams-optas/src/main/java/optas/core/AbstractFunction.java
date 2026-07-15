/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.core;

/**
 *
 * @author christian
 */
    abstract public class AbstractFunction extends AbstractModel{
        abstract public double[] evaluate(double x[]) throws SampleLimitException, ObjectiveAchievedException;
    }
