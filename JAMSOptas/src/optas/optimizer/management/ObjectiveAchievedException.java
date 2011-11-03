/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import java.util.Arrays;

/**
 *
 * @author chris
 */
public class ObjectiveAchievedException extends Exception {

    String msg;

    ObjectiveAchievedException(double value[], double target[]) {
        this.msg = "Objectives " + Arrays.toString(target) + " is achieved with value " + Arrays.toString(value);
    }

    ObjectiveAchievedException(double value, double target) {
        this.msg = "Objective " + target + " is achieved with value " + value;
    }

    @Override
    public String toString() {
        return msg;
    }
}
