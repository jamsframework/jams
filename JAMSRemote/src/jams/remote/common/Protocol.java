/*
 * Protocol.java
 * Created on 28. Mai 2007, 18:46
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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

package jams.remote.common;

/**
 *
 * @author Sven Kralisch
 */
public class Protocol {
    
    public static final int GET_STATUS = 0;
    public static final int GET_FILE = 1;
    public static final int PUSH_FILE = 2;
    public static final int CLIENT_SHUT_DOWN = 3;
    public static final int SERVER_SHUT_DOWN = 4;
    public static final int CREATE_DIR = 5;
    public static final int LIST_PATH = 6;
    public static final int JAMS_RUN = 7;
    public static final int GET_RUN_COUNT = 8;
    public static final int GET_MAX_RUN_COUNT = 9;
    public static final int GET_ADDRESS = 10;
    public static final int ACCEPT_CONNECTION = 11;
    public static final int WRONG_ACCOUNT = 12;
    public static final int GET_BASE_DIR = 13;
    public static final int CLEAN_DIR = 14;
    public static final int SUCCESS = 15;
    public static final int ERROR = 16;
    public static final int GET_MODEL_INFO_LOG = 17;
    public static final int GET_MODEL_ERROR_LOG = 18;
    
    public static String getName(int command) {
        switch (command) {
            case GET_STATUS:
                return "GET_STATUS";
            case GET_FILE:
                return "GET_FILE";
            case PUSH_FILE:
                return "PUSH_FILE";
            case CLIENT_SHUT_DOWN:
                return "CLIENT_SHUT_DOWN";
            case SERVER_SHUT_DOWN:
                return "SERVER_SHUT_DOWN";
            case CREATE_DIR:
                return "CREATE_DIR";
            case LIST_PATH:
                return "LIST_PATH";
            case JAMS_RUN:
                return "JAMS_RUN";
            case GET_RUN_COUNT:
                return "GET_RUN_COUNT";
            case GET_MAX_RUN_COUNT:
                return "GET_MAX_RUN_COUNT";
            case GET_ADDRESS:
                return "GET_ADDRESS";
            case GET_BASE_DIR:
                return "GET_BASE_DIR";
            case CLEAN_DIR:
                return "CLEAN_DIR";
            case GET_MODEL_INFO_LOG:
                return "GET_MODEL_INFO_LOG";
            case GET_MODEL_ERROR_LOG:
                return "GET_MODEL_ERROR_LOG";
            default:
                return "\"" + command + "\"";
        }
    }
}
