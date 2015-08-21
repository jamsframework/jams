/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import optas.data.time.api.TimeFilter;

/**
 *
 * @author chris
 */
public abstract class DefaultTimeFilter implements Serializable, TimeFilter {

    String name;
    boolean isAdditive = false;
    boolean isEnabled = true;
    boolean isInverted = false;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setAdditive(boolean isAdditive) {
        this.isAdditive = isAdditive;
    }

    @Override
    public boolean isAdditive() {
        return isAdditive;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setInverted(boolean isInverted) {
        this.isInverted = isInverted;
    }

    @Override
    public boolean isInverted() {
        return isInverted;
    }

    @Override
    public boolean isFiltered(Date date) {
        checkNotNull(date);
        return false;
    }

    private TreeSet<Integer> getNonFilteredTimeSteps(Attribute.TimeInterval domain) {
        checkArgument(domain.getStart().getTimeInMillis() <= domain.getEnd().getTimeInMillis(),
                "Start time %s is after end time %s",
                domain.getStart().getValue(), domain.getEnd().getValue());

        TreeSet<Integer> timeSteps = new TreeSet<Integer>();

        Attribute.Calendar start = domain.getStart().clone();
        Attribute.Calendar end = domain.getEnd().clone();

        int t = 0;
        while (!start.after(end)) {
            if (!isFiltered(start.getTime())) {
                timeSteps.add(t);
            }
            start.add(domain.getTimeUnit(), domain.getTimeUnitCount());
            t++;
        }
        return timeSteps;
    }

    @Override
    public String toString(Attribute.TimeInterval domain) {
        checkNotNull(domain, "Time Domain must not be null!");

        String stringRepresentation = "";
        TreeSet<Integer> timeSteps = getNonFilteredTimeSteps(domain);
        Iterator<Integer> iter = timeSteps.iterator();

        while (iter.hasNext()) {
            int startIndex = iter.next();
            int currentIndex = startIndex;
            int endIndex = startIndex;

            while (iter.hasNext()) {
                int next = iter.next();

                if (next - currentIndex > 1) {
                    break;
                }
                endIndex = currentIndex + 1;
                currentIndex = next;
            }

            Attribute.TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();

            interval.getStart().setTime(domain.getStart().clone().getTime());
            interval.getEnd().setTime(domain.getStart().clone().getTime());
            interval.getStart().add(domain.getTimeUnit(), domain.getTimeUnitCount() * startIndex);
            interval.getEnd().add(domain.getTimeUnit(), domain.getTimeUnitCount() * endIndex);

            stringRepresentation += interval.toString() + ";";
        }
        return stringRepresentation;
    }

    @Override
    public String toFile(File f, Attribute.TimeInterval domain) throws IOException {
        checkNotNull(f, "File must not be null!");
        checkArgument(f.isFile(), "%s is not a file!", f.getAbsoluteFile());

        String stringRepresentation = toString(domain);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(stringRepresentation);
        }
        return stringRepresentation;
    }
}
