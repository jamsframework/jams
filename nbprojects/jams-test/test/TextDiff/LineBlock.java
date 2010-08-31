package TextDiff;

/**
 * A contiguous block of one or more lines with the same state, eg match or other.
 */
public class LineBlock
{
    public int fromLineNum;
    public int thruLineNum;
    public String[] lines;
    public boolean reportable = false;

    public LineBlock(String[] sourceLines, int fromLineNum, int thruLineNum)
    {
        this.fromLineNum = fromLineNum;
        this.thruLineNum = thruLineNum;
        lines = new String[thruLineNum - fromLineNum + 1];
        System.arraycopy( sourceLines, fromLineNum, lines, 0, lines.length );
    }
}
