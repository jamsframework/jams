/*
 * RuntimeManager.java
 * Created on 28. November 2008, 08:39
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.runtime;

import jams.data.Attribute;
import jams.data.JAMSCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 *
 * This singleton class is used to mangage all JAMSRuntime objects that are
 * currently executing a model
 */
public class RuntimeManager extends Observable {

    private HashMap<JAMSRuntime, RuntimeInfo> runtimeInstances = new HashMap<JAMSRuntime, RuntimeInfo>();
    private static RuntimeManager instance;

    private RuntimeManager() {
    }

    /**
     * Returns the singleton RuntimeManager instance
     *
     * @return A RuntimeManager object
     */
    public static RuntimeManager getInstance() {
        if (instance == null) {
            instance = new RuntimeManager();
        }
        return instance;
    }

    /**
     * Adds a JAMSRuntime object
     *
     * @param runtime A JAMSRuntime object
     */
    public void addRuntime(JAMSRuntime runtime) {

        // runtime will only be added if runstate is "run"
        if (runtime.getState() != JAMSRuntime.STATE_RUN) {
            return;
        }

        RuntimeInfo rtInfo = new RuntimeInfo(Calendar.getInstance(), runtime);
        runtimeInstances.put(runtime, rtInfo);
        this.setChanged();
        this.notifyObservers(rtInfo);

        runtime.addStateObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {

                JAMSRuntime rt = (JAMSRuntime) o;
                if (rt.getState() == JAMSRuntime.STATE_STOP || rt.getState() == JAMSRuntime.STATE_PAUSE) {
                    removeRuntime(rt);
                }
            }
        });
    }

    private void removeRuntime(JAMSRuntime runtime) {
        RuntimeInfo rtInfo = runtimeInstances.get(runtime);
        if (rtInfo == null) {
            return;
        }
        rtInfo.setEnd(Calendar.getInstance());
        runtimeInstances.remove(runtime);
        this.setChanged();
        this.notifyObservers(rtInfo);
    }

    /**
     * Returns number of currently managed JAMSRuntime instances
     *
     * @return The number of currently managed JAMSRuntime instances
     */
    public int getNumberofInstances() {
        return runtimeInstances.size();
    }

    public class RuntimeInfo {

        private Calendar startTime, endTime;
        private JAMSRuntime runtime;
//        private String stringRepresentation = null;

        public RuntimeInfo(Calendar startTime, JAMSRuntime runtime) {
            this.startTime = startTime;
            this.runtime = runtime;
        }

        private String getProgressString() {
            long max = runtime.getModel().getProgress()[1];
            long cur = runtime.getModel().getProgress()[0];

            if (max == 0) {
                return String.format("%2d%%", max);
            } else {
                return String.format("%2.0f%%", (float) 100*cur/max);                
            }
        }

        @Override
        public String toString() {
            String result;
//            if (stringRepresentation == null) {
            result = runtime.getModel().getName() + " [";
            SimpleDateFormat sdf = new SimpleDateFormat(Attribute.Calendar.DEFAULT_FORMAT_PATTERN + ":ss");
            result += "start=" + sdf.format(startTime.getTime()) + ", ";
            result += "progress=" + getProgressString() + ", ";
            if (runtime != null) {
                if (runtime.getModel().getWorkspace() != null) {
                    result += "workspace=" + runtime.getModel().getWorkspace().getDirectory().getAbsolutePath();
                } else {
                    result += "workspace=null";
                }
            }
            result += "]";
//            stringRepresentation = result;
//            } else {
//                result = stringRepresentation;
////                if (endTime != null) {
////                    SimpleDateFormat sdf = new SimpleDateFormat();
////                    result += "-[" + sdf.format(endTime.getTime()) + "]";
////                }
//            }
            return result;
        }

        public void setEnd(Calendar endTime) {
            this.endTime = endTime;
        }

        /**
         * @return the startTime
         */
        public Calendar getStartTime() {
            return startTime;
        }

        /**
         * @return the endTime
         */
        public Calendar getEndTime() {
            return endTime;
        }

        /**
         * @return the runtime
         */
        public JAMSRuntime getRuntime() {
            return runtime;
        }
    }
}
