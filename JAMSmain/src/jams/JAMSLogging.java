/*
 * JAMSLogging.java
 * Created on 07.02.2014, 11:41:42
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
package jams;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSLogging extends Observable {

    private static final List<Logger> loggers = new ArrayList();
    private static final JAMSLogging instance = new JAMSLogging();

    /**
     * Register a new logger to common log handling
     *
     * @param logger
     */
    public static void registerLogger(Logger logger) {
        if (!loggers.contains(logger)) {
            loggers.add(logger);
            instance.setChanged();
            instance.notifyObservers(logger);
        }
    }

    /**
     * Unregister a logger from common log handling
     *
     * @param logger
     */
    public static boolean unregisterLogger(Logger logger) {
        boolean result = loggers.remove(logger);
        instance.setChanged();
        instance.notifyObservers(logger);
        return result;
    }

    /**
     * @return the loggers
     */
    public List<Logger> getLoggers() {
        return loggers;
    }

    @Override
    public void addObserver(Observer o) {
        instance.deleteObserver(o);
        super.addObserver(o);
    }

    public static JAMSLogging getInstance() {
        return instance;
    }

}
