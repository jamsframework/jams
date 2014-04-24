/*
 * JAMSClientException.java
 * Created on 20.04.2014, 15:00:07
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

package jams.server.client;

/**
 *
 * @author christian
 */
public class JAMSClientException extends Exception{
    public enum ExceptionType{
        USER_NOT_LOGGED_IN,
        FORBIDDEN,
        NOT_FOUND,
        INTERNAL_SERVER_ERROR,
        UNEXPECTED_RESULT,
        UNKNOWN;
    }
    String message;
    ExceptionType type;
    Exception underlyingException;
    
    JAMSClientException(String msg, ExceptionType type, Exception underlyingException){
        this.message = msg;
        this.type = type;
        this.underlyingException = underlyingException;
    }
    
    public ExceptionType getType(){
        return type;
    }
    
    @Override
    public String toString(){
        return message;
    }
}
