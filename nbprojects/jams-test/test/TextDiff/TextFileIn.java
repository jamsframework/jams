package TextDiff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
11.  * This class represents a text file input from disk.  Usage:
12.  * <pre>
13.  * TextFileIn MyFileIn = new TextFileIn( fileid );
14.  * while ( ( MyLine = MyFileIn.readLine() ) != null )
15.  * {
16.  *    do something with MyLine
17.  * }
18.  * </pre>
19.  * Note: No close is necessary if you read to end of file.  If
20.  * you do not read to end of file, call close().
21.  */
public class TextFileIn {

    private BufferedReader mReader = null;

    private TextFileIn() {
    }

    /**
    32.      * Constructor opens named file for input.
    33.      * @param aFileName java.lang.String
    34.      */
    public TextFileIn(String aFileName) throws java.io.FileNotFoundException {
        mReader = new BufferedReader(new FileReader(aFileName));
    }

    /**
    40.      * Constructor opens provided file for input.
    41.      * @param aFile
    42.      */
    public TextFileIn(File aFile) throws FileNotFoundException {
        mReader = new BufferedReader(new FileReader(aFile));
    }

    /**
    48.      * Returns contents of a file as one string.  NewLine characters that
    49.      * delimit lines in the file are converted to single spaces.
    50.      * <p>
    51.      * 09/26/2000 Standley New
    52.      * @return String
    53.      */
    public String asString() throws java.io.IOException {
        String lLine;
        StringBuffer lReturn = new StringBuffer();

        while ((lLine = this.readLine()) != null) {
            lReturn.append(lLine);
            lReturn.append(" ");
        }
        return lReturn.toString();
    }

    /**
    67.      * Returns contents of a file as an array of Strings.
    68.      * @return String[]
    69.      */
    public String[] asArray() throws IOException {
        String lLine;
        List lList = new ArrayList();
        while (null != (lLine = this.readLine())) {
            lList.add(lLine);
        }
        return (String[]) lList.toArray(new String[]{});
    }

    /**
    81.      * Close the input file.  This is not necessary if the client
    82.      * reads to end of file.
    83.      * <p>11/14/00 JLS Made close on closed file not an error.
    84.      */
    public void close() {
        if (null == mReader) {
            return;
        }
        try {
            mReader.close();
            mReader = null;
        } catch (Exception e) {
        }
    }

    /**
    99.      * Main for testing - demonstrates common usage.
    100.      * @param args java.lang.String[]
    101.      */
    /**
    119.      * Read one line from input file.  On read past end of file
    120.      * closes the file and returns null.
    121.      * @return java.lang.String
    122.      */
    public String readLine() throws java.io.IOException {
        if (null == mReader) {
            throw new java.io.IOException();
        }
        String lLine = mReader.readLine();
        if (null == lLine) {
            this.close();
        }
        return lLine;
    }
}
