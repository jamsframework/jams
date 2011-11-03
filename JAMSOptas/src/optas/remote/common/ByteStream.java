/*
 * ByteStream.java
 * Created on 30. Mai 2007, 16:11
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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
package optas.remote.common;

/**
 *
 * @author Sven Kralisch
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

public class ByteStream {

    public static class ProgressInfo{
        public double percentage;
        public boolean isReceiving;
    }

    public static final int BUFFER_SIZE = 1024 * 8;

    private static byte[] toByteArray(int in_int) {
        byte a[] = new byte[4];
        for (int i = 0; i < 4; i++) {

            int b_int = (in_int >> (i * 8)) & 255;
            byte b = (byte) (b_int);

            a[i] = b;
        }
        return a;
    }

    public static Object toObject(InputStream in) throws IOException {
        return toObject(in, null);
    }
    public static Object toObject(InputStream in, ProgressInfo p) throws IOException {
        Object o = null;
        try {
            int length = toInt(in);
            byte[] data = toByteArray(in, length, p);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return o;
    }

    private static int toInt(byte[] byte_array_4) {
        int ret = 0;
        for (int i = 0; i < 4; i++) {
            int b = (int) byte_array_4[i];
            if (i < 3 && b < 0) {
                b = 256 + b;
            }
            ret += b << (i * 8);
        }
        return ret;
    }

    public static int toInt(InputStream in) throws java.io.IOException {
        try {
            byte[] byte_array_4 = new byte[4];

            byte_array_4[0] = (byte) in.read();
            byte_array_4[1] = (byte) in.read();
            byte_array_4[2] = (byte) in.read();
            byte_array_4[3] = (byte) in.read();

            return toInt(byte_array_4);
        } catch (SocketException se) {
            return -1;
        }
    }

    public static String toString(InputStream ins) throws java.io.IOException {
        return toString(ins, null);
    }
    public static String toString(InputStream ins, ProgressInfo p) throws java.io.IOException {
        try {
            int len = toInt(ins);
            if (len < 0) {
                return null;
            } else {
                return toString(ins, len, p);
            }
        } catch (SocketException se) {
            return null;
        }
    }

    private static String toString(InputStream ins, int len, ProgressInfo p) throws java.io.IOException {
        String ret = new String();
        if (p!=null){
            p.isReceiving = true;
            p.percentage = 0;
        }
        for (int i = 0; i < len; i++) {
            ret += (char) ins.read();
            if (p!=null)
                p.percentage = ((double)i/(double)len);
        }
        return ret;
    }

    public static void toStream(OutputStream os, Object o) throws java.io.IOException {
        toStream(os,o, null);
    }
    public static void toStream(OutputStream os, Object o, ProgressInfo p) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.flush();
        oos.close();

        baos.flush();
        byte data[] = baos.toByteArray();
        baos.close();

        toStream(os,data.length);

        int offset = 0;
        if (p!=null){
            p.isReceiving = false;
        }
        while(offset < data.length){
            int numWrite = Math.min(BUFFER_SIZE, data.length-offset);
            os.write(data, offset, numWrite);
            offset += numWrite;
            if (p!=null)
                p.percentage = (double)offset / (double)data.length;
        }
        //os.write(data);
        os.flush();
    }

    public static void toStream(OutputStream os, int i) throws java.io.IOException {
        byte[] byte_array_4 = toByteArray(i);
        os.write(byte_array_4);
    }

    public static void toStream(OutputStream os, String s) throws java.io.IOException {
        toStream(os, s, null) ;
    }
    public static void toStream(OutputStream os, String s, ProgressInfo p) throws java.io.IOException {
        int len_s = s.length();
        toStream(os, len_s);
        if (p!=null)
            p.isReceiving = false;

        for (int i = 0; i < len_s; i++) {
            if (p!=null)
                p.percentage = (double)i / (double)(len_s);
            os.write((byte) s.charAt(i));
        }
        os.flush();
    }

    private static byte[] toByteArray(InputStream ins, int an_int, ProgressInfo p) throws
            java.io.IOException,
            Exception {

        if (p!=null)
            p.isReceiving = true;

        byte[] ret = new byte[an_int];

        int offset = 0;
        int numRead = 0;
        int outstanding = an_int;

        while ((offset < an_int)
                && ((numRead = ins.read(ret, offset, outstanding)) > 0)) {
            if (p!=null)
                p.percentage = (double)offset / (double)an_int;
            offset += numRead;
            outstanding = an_int - offset;
        }
        if (offset < ret.length) {
            throw new Exception("Could not completely read from stream, numRead=" + numRead + ", ret.length=" + ret.length); // ???
        }
        return ret;
    }

    private static void toFile(InputStream ins, FileOutputStream fos, long len, int buf_size, ProgressInfo p) throws
            java.io.FileNotFoundException,
            java.io.IOException {

        byte[] buffer = new byte[buf_size];

        int len_read = 0;
        int total_len_read = 0;

        if (p != null){
            p.isReceiving = true;
        }
        while (total_len_read + buf_size <= len) {
            len_read = ins.read(buffer);
            total_len_read += len_read;            
            fos.write(buffer, 0, len_read);
            if (p!=null)
                p.percentage = (double)total_len_read / (double)len;
        }

        if (total_len_read < len) {
            toFile(ins, fos, len - total_len_read, buf_size / 2, p);
        }
    }

    private static void toFile(InputStream ins, File file, long len, ProgressInfo p) throws
            java.io.FileNotFoundException,
            java.io.IOException {

        FileOutputStream fos = new FileOutputStream(file);

        toFile(ins, fos, len, BUFFER_SIZE, p);

        fos.close();
    }

    public static void toFile(InputStream ins, File file) throws
            java.io.FileNotFoundException,
            java.io.IOException {
        toFile(ins,file,null);
    }
    public static void toFile(InputStream ins, File file, ProgressInfo p) throws
            java.io.FileNotFoundException,
            java.io.IOException {

        String name = (String)toObject(ins);
        long len = (Long)toObject(ins);

        if (file.isDirectory())
            toFile(ins, new File(file.getAbsolutePath() + "/" + name), len, p);
        else
            toFile(ins, file, len, p);
    }

    public static void toStream(OutputStream os, File file)
            throws java.io.FileNotFoundException,
            java.io.IOException {
        toStream(os, file, null);
    }

    public static void toStream(OutputStream os, File file, ProgressInfo p)
            throws java.io.FileNotFoundException,
            java.io.IOException {

        if (file==null)
            throw new FileNotFoundException("file is null");

        toStream(os, (Object)file.getName());
        toStream(os, new Long(file.length()));

        byte b[] = new byte[BUFFER_SIZE];
        InputStream is = new FileInputStream(file);
        int numRead = 0;

        if (p!=null){
            p.isReceiving = false;
        }

        long read_total = 0;
        while ((numRead = is.read(b)) > 0) {
            if (p!=null)
                p.percentage = (double)read_total / (double)file.length();
            os.write(b, 0, numRead);
        }
        os.flush();
        is.close();
    }
}
