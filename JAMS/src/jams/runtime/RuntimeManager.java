/*
 * RuntimeManager.java
 * Created on 28. November 2008, 08:39
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

package jams.runtime;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class RuntimeManager {

    private HashMap<JAMSRuntime, RuntimeInfo> runtimeInstances = new HashMap<JAMSRuntime, RuntimeInfo>();

    public void addRuntime(JAMSRuntime runtime) {

        // runtime will only be added if on runstate "run"
        if (runtime.getRunState() != JAMSRuntime.RUNSTATE_RUN) {
            return;
        }

        RuntimeInfo rtInfo = new RuntimeInfo(Calendar.getInstance(), runtime);
        runtimeInstances.put(runtime, rtInfo);
        runtime.addRunStateObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                
                JAMSRuntime rt = (JAMSRuntime) o;
                if (rt.getRunState() == JAMSRuntime.RUNSTATE_STOP) {
                    removeRuntime(rt);
                }
            }
        });
    }

    private void removeRuntime(JAMSRuntime runtime) {
        runtimeInstances.remove(runtime);
    }

    public int getNumberofInstances() {
        return runtimeInstances.size();
    }

    class RuntimeInfo {
        Calendar startTime;
        JAMSRuntime runtime;

        public RuntimeInfo(Calendar startTime, JAMSRuntime runtime) {
            this.startTime = startTime;
            this.runtime = runtime;
        }
    }
}
