/*
 * EventCounter.java
 * Created on 05.09.2017, 10:33:59
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.calc;

import jams.data.*;
import jams.model.*;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "EventCounter",
        author = "Sven Kralisch",
        description = "Counts the number of occurrences of a specific event with"
        + "regard to numeric values, e.g. double attribute being "
        + "higher/lower than a certain value",
        date = "2017-09-05",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EventCounter extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double a;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double b;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double c;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double d;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double e;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double f;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double g;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double h;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double i;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Argument slot",
            defaultValue = "0"
    )
    public Attribute.Double j;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Event expression. This must be a boolean result type"
                    + " and follows Java syntax, e.g. \"sqrt(a) + b^2 <= c\"."
                    + " For a list of built-in functions, have a look at"
                    + " http://projects.congrace.de/exp4j.",
            defaultValue = "a + b > 0"
    )
    public Attribute.String expression;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Event occurrence? \"1\" if event occurred, \"0\" "
                    + "otherwise"
    )
    public Attribute.Double result;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Accumulated event occurrence",
            defaultValue = "0"
    )
    public Attribute.Double result_accum;

    private Operator st, steq, gt, gteq, eq;
    private Expression exp;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        st = new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] < values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };
        steq = new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] <= values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };
        gt = new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] > values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };
        gteq = new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] >= values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };
        eq = new Operator("==", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] == values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };

        exp = new ExpressionBuilder(expression.getValue())
                .operator(st)
                .operator(gt)
                .operator(steq)
                .operator(gteq)
                .operator(eq)
                .variables("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")
                .build();

    }

    @Override
    public void run() {

        double r = exp
                .setVariable("a", a.getValue())
                .setVariable("b", b.getValue())
                .setVariable("c", c.getValue())
                .setVariable("d", d.getValue())
                .setVariable("e", e.getValue())
                .setVariable("f", f.getValue())
                .setVariable("g", g.getValue())
                .setVariable("h", h.getValue())
                .setVariable("i", i.getValue())
                .setVariable("j", j.getValue())
                .evaluate();

        result.setValue(r);
        if (r == 1) {
            result_accum.setValue(result_accum.getValue() + 1);
        }
    }

    @Override
    public void cleanup() {
    }

    public static void main(String[] args) {

        double x = 212, y = 210;

        Operator st = new Operator("==", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] == values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };

        Expression e = new ExpressionBuilder("x - 1 == 0").operator(st)
                .variables("x", "y")
                .build();
//                .setVariable("x", getX())
//                .setVariable("y", y);

        System.out.println(1d == e.setVariable("x", getX()).evaluate());
        System.out.println(1d == e.setVariable("x", getX()).evaluate());

        long start;

        start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            double result = e.evaluate();
        }
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            boolean result = x - y < i;
        }
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println();
    }

    static double x = 1;

    public static double getX() {
        return x++;
    }
}
