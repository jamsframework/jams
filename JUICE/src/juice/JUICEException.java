/*
 * JUICEException.java
 * Created on 20. März 2007, 22:50
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

package juice;

/**
 *
 * @author Sven Kralisch
 */
public class JUICEException extends Exception {
    
    public static NameAlreadyUsedException getNameAlreadyUsedException(String text) {
        return new NameAlreadyUsedException(text);
    }
    
    public static class NameAlreadyUsedException extends Exception {
        public NameAlreadyUsedException(String text) {
            super(text);
        }
    };
}
