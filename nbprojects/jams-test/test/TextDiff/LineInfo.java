package TextDiff;

/**
 * One line in one file. For UniqueMatch lines, holds a line number 
 * pointer into the other file.
 */
public class LineInfo
{
    public static final int EOF = -1;
    public static final int OLDONLY = 1;
    public static final int NEWONLY = 2;
    public static final int UNIQUEMATCH = 3;
    public static final int OTHER = 4;

    public int lineStatus = 0;
    public int oldLineNum = 0;
    public int newLineNum = 0;
    public int blockNum = 0;

    public LineInfo()
    {
        this( 0, 0, 0, 0 );
    }

    public LineInfo(int aLineStatus, int aOldLineNum, int aNewLineNum, int aBlockNum)
    {
        lineStatus = aLineStatus;
        oldLineNum = aOldLineNum;
        newLineNum = aNewLineNum;
        blockNum = aBlockNum;
    }

    public boolean isEOF()
    {
        return (lineStatus == EOF);
    }

    public boolean isMatch()
    {
        return (lineStatus == UNIQUEMATCH);
    }

    public boolean isOldOnly()
    {
        return (lineStatus == OLDONLY || lineStatus == OTHER);
    }

    public boolean isNewOnly()
    {
        return (lineStatus == NEWONLY || lineStatus == OTHER);
    }

    public void setBlockNumber(int aBlockNum)
    {
        blockNum = aBlockNum;
        lineStatus = UNIQUEMATCH;
    }

    public String toString()
    {
        return "Status=" + lineStatus + " OldNum=" + oldLineNum + " NewNum=" + newLineNum + " Block=" + blockNum;
    }
}