
/* @(#)BufferedWriter.java 1.26 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jams.io;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.channels.FileChannel;

/**
 * Write text to a character-output stream, buffering characters so as to
 * provide for the efficient writing of single characters, arrays, and strings.
 *
 * <p> The buffer size may be specified, or the default size may be accepted.
 * The default is large enough for most purposes.
 *
 * <p> A newLine() method is provided, which uses the platform's own notion of
 * line separator as defined by the system property <tt>line.separator</tt>.
 * Not all platforms use the newline character ('\n') to terminate lines.
 * Calling this method to terminate each output line is therefore preferred to
 * writing a newline character directly.
 *
 * <p> In general, a Writer sends its output immediately to the underlying
 * character or byte stream. Unless prompt output is required, it is advisable
 * to wrap a BufferedWriter around any Writer whose write() operations may be
 * costly, such as FileWriters and OutputStreamWriters. For example,
 *
 * <pre>
 * PrintWriter out
 * = new PrintWriter(new BufferedWriter(new FileWriter("foo.out")));
 * </pre>
 *
 * will buffer the PrintWriter's output to the file. Without buffering, each
 * invocation of a print() method would cause characters to be converted into
 * bytes that would then be written immediately to the file, which can be very
 * inefficient.
 *
 * @see PrintWriter
 * @see FileWriter
 * @see OutputStreamWriter
 *
 * @version 1.26, 03/12/19
 * @author Mark Reinhold
 * @since JDK1.1
 */
public class BufferedFileWriter extends Writer {

    private Writer out;
    private char cb[];
    private int nChars,  nextChar;
    private static int defaultCharBufferSize = 8192;
    public static final String 
            CHARSET_NAME_ISO_8859_1 = "ISO-8859-1",
            CHARSET_NAME_UTF_8 = "UTF-8";
    transient private FileChannel outChannel;
    /**
     * Line separator string. This is the value of the line.separator
     * property at the moment that the stream was created.
     */
    private String lineSeparator;

    /**
     * Creates a buffering character-input stream that uses a default-sized
     * input buffer.
     *
     * @param  is   A FileInputStream with default encoding "ISO-8859-1"
     */
    public BufferedFileWriter(FileOutputStream is) {
        this(is, defaultCharBufferSize, CHARSET_NAME_ISO_8859_1);
    }
    
    /**
     * Create a buffered character-output stream that uses a default-sized
     * output buffer.
     *
     * @param out A Writer
     */
    public BufferedFileWriter(FileOutputStream out, String charsetName) {
        this(out, defaultCharBufferSize, charsetName);
    }

    /**
     * Create a new buffered character-output stream that uses an output
     * buffer of the given size.
     *
     * @param out A Writer
     * @param sz Output-buffer size, a positive integer
     *
     * @exception IllegalArgumentException If sz is <= 0
     */
    public BufferedFileWriter(FileOutputStream is, int sz, String charsetName) {
        outChannel = is.getChannel();
        try {
            out = new OutputStreamWriter(is, charsetName);
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Charset \"" + charsetName + "\" could not be found, using default charset!");
            out = new OutputStreamWriter(is);
        }
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = out;

        if (sz <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }

        cb = new char[sz];
        nChars = sz;
        nextChar = 0;
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (out == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Flush the output buffer to the underlying character stream, without
     * flushing the stream itself. This method is non-private only so that it
     * may be invoked by PrintStream.
     */
    void flushBuffer() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar == 0) {
                return;
            }
            out.write(cb, 0, nextChar);
            nextChar = 0;
        }
    }

    /**
     * Write a single character.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void write(int c) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar >= nChars) {
                flushBuffer();
            }
            cb[nextChar++] = (char) c;
        }
    }

    /**
     * Our own little min method, to avoid loading java.lang.Math if we've run
     * out of file descriptors and we're trying to print a stack trace.
     */
    private int min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    /**
     * Write a portion of an array of characters.
     *
     * <p> Ordinarily this method stores characters from the given array into
     * this stream's buffer, flushing the buffer to the underlying stream as
     * needed. If the requested length is at least as large as the buffer,
     * however, then this method will flush the buffer and write the characters
     * directly to the underlying stream. Thus redundant
     * <code>BufferedWriter</code>s will not copy data unnecessarily.
     *
     * @param cbuf A character array
     * @param off Offset from which to start reading characters
     * @param len Number of characters to write
     *
     * @exception IOException If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }

            if (len >= nChars) {
                /* If the request length exceeds the size of the output buffer,
                flush the buffer and then write the data directly. In this
                way buffered streams will cascade harmlessly. */
                flushBuffer();
                out.write(cbuf, off, len);
                return;
            }

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                System.arraycopy(cbuf, b, cb, nextChar, d);
                b += d;
                nextChar += d;
                if (nextChar >= nChars) {
                    flushBuffer();
                }
            }
        }
    }

    /**
     * Write a portion of a String.
     *
     * <p> If the value of the <tt>len</tt> parameter is negative then no
     * characters are written. This is contrary to the specification of this
     * method in the {@linkplain java.io.Writer#write(java.lang.String,int,int)
     * superclass}, which requires that an {@link IndexOutOfBoundsException} be
     * thrown.
     *
     * @param s String to be written
     * @param off Offset from which to start reading characters
     * @param len Number of characters to be written
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void write(String s, int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                s.getChars(b, b + d, cb, nextChar);
                b += d;
                nextChar += d;
                if (nextChar >= nChars) {
                    flushBuffer();
                }
            }
        }
    }

    /**
     * Write a line separator. The line separator string is defined by the
     * system property <tt>line.separator</tt>, and is not necessarily a single
     * newline ('\n') character.
     *
     * @exception IOException If an I/O error occurs
     */
    public void newLine() throws IOException {
        write(lineSeparator);
    }

    /**
     * Flush the stream.
     *
     * @exception IOException If an I/O error occurs
     */
    public void flush() throws IOException {
        synchronized (lock) {
            flushBuffer();
            out.flush();
        }
    }

    public long getPosition() {        
        try {            
            flush();
            return this.outChannel.position();
        } catch (IOException e) {
            return 0;
        }

    }

    public void setPosition(long position) throws IOException {        
        outChannel = this.outChannel.position(position);        
    }
    
    /**
     * Close the stream.
     *
     * @exception IOException If an I/O error occurs
     */
    public void close() throws IOException {
        synchronized (lock) {
            if (out == null) {
                return;
            }
            flushBuffer();
            out.close();
            out = null;
            cb = null;
        }
    }
}
