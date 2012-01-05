/*
 * JAMSLog.java
 * Created on 30. Juni 2007, 17:10
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSLog extends Observable implements Serializable {

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

    public String getLastString() {
        return lastString;
    }

    public void print(String str) {
        lastString = str;
        logString.append(str);
        trimBuffer();

        this.setChanged();
        this.notifyObservers(str);
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