/*
 * JAMSLogger.java
 * Created on 30. Juni 2007, 17:10
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSLogger extends Observable implements Logger {

    StringBuffer logString = new StringBuffer();
    String lastString;
    int bufferSize = 32000000;

    public void setBufferSize(int newBufferSize) {
        bufferSize = newBufferSize;
        trimBuffer();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    private void trimBuffer() {
        if (bufferSize != -1 && bufferSize < logString.length()) {
            logString.delete(0, logString.length() - bufferSize);
        }
    }

    public String getLogString() {
        return logString.toString();
    }
    
    @Override
    public String toString() {
        return getLogString();
    }    

    @Override
    public String getLastString() {
        return lastString;
    }

    @Override
    public void print(String str) {
        lastString = str;
        logString.append(str);
        trimBuffer();

        this.setChanged();
        this.notifyObservers(str);
    }

    @Override
    public void println(String str) {
        print(str + "\n");
    }    

    public void print(char c) {
        logString.append(c);
        this.setChanged();
        this.notifyObservers(c);
    }

    @Override
    public void addObserver(Observer o) {
        this.deleteObserver(o);
        super.addObserver(o);
    }
}