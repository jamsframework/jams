/*
 * JAMSException.java
 * Created on 19.09.2010, 23:24:46
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

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSException extends Exception {

    public static final int INFO_TYPE = 0, ERROR_TYPE = 1;
    private String header;
    private Throwable wrappedException;
    protected int type = ERROR_TYPE;

    public JAMSException(String message, String header) {
        super(message);
        this.header = header;
    }

    public JAMSException(String message, String header, Throwable wrappedException) {
        super(message);
        this.header = header;
        this.wrappedException = wrappedException;
    }

    public JAMSException(String message) {
        super(message);
    }

    public JAMSException(String message, Throwable wrappedException) {
        super(message);
        this.wrappedException = wrappedException;
    }

    public String getHeader() {
        return header;
    }

    public Throwable getWrappedException() {
        return wrappedException;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }
}