package TextDiff;

/**
 * One of the two files being compared. lineNum supports nextBlock something like
 * an iterator.
 */
public class FileInfo
{
    public String[] lines;
    public LineInfo[] lineInfo;
    public int length;
    public int lineNum;
    private static final LineInfo EOF = new LineInfo( -1, -1, -1, -1 );

    public FileInfo(String[] lines)
    {
        this.lines = lines;
        length = lines.length;
        lineInfo = new LineInfo[length];
    }

    public LineInfo currentLineInfo()
    {
        return lineInfoAt( lineNum );
    }

    public LineInfo lineInfoAt(int lineNum)
    {
        if (lineNum >= lines.length)
            return EOF;
        else
            return lineInfo[lineNum];
    }

    public LineBlock nextBlock()
    {
        LineBlock lineBlock = getBlockAt( lineNum );
        if (null != lineBlock)
            lineNum += lineBlock.lines.length; 
        return lineBlock;
    }

    public LineBlock getBlockAt(int lineNum)
    {
        if (lineNum >= lines.length)
            return null;
        int fromLineNum = lineNum;
        int blockNum = lineInfo[lineNum].blockNum;
        while (blockNum == lineInfoAt( lineNum ).blockNum)
        {
            lineNum++;
        }
        int thruLineNum = lineNum - 1;
        LineBlock lBlock = new LineBlock( lines, fromLineNum, thruLineNum );
        return lBlock;
    }

    public void setBlockNumber(int lineNum, int blockNum)
    {
        lineInfo[lineNum].setBlockNumber( blockNum );
    }

    public boolean isValidLineNum(int lineNum)
    {
        return ((lineNum >= 0) && (lineNum < lines.length));
    }
}