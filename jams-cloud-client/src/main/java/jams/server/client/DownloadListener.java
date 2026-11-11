/*
 * DownloadListener.java
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
package jams.server.client;

/**
 * Receives progress notifications while a file is downloaded from the server.
 * Methods are called from the downloading thread.
 */
public interface DownloadListener {

    /**
     * called while bytes are received
     *
     * @param bytesRead bytes received so far
     * @param totalBytes expected size in bytes or -1 if unknown
     */
    void progress(long bytesRead, long totalBytes);

    /**
     * called before the request is sent; running the handler aborts the
     * download from any thread, the download then fails with an IOException
     *
     * @param abort aborts the running request
     */
    default void setAbortHandler(Runnable abort) {
    }

    /**
     * called after the download has finished and the received archive is
     * being unpacked
     */
    default void unpacking() {
    }
}
