package TextDiff;

import java.io.PrintStream;

/**
 * Implements IREportWriter to generate a human-friendly report to any
 * PrintStream.
 */
public class DefaultReportWriter implements IReportWriter
{
    private PrintStream printStream;
    private String lineNumPad = "    "; // Line numbers will be padded to this

    // length.

    public DefaultReportWriter(PrintStream aStream)
    {
        printStream = aStream;
    }

    public void report(EditCommand command)
    {
        printStream.print( command.command + " " );
        print( command.oldLines, "Old" );
        print( command.newLines, "New" );
        printStream.println( " " );
    }

    private void print(LineBlock lineBlock, String fileDescription)
    {
        if (null != lineBlock)
        {
            printStream.println( fileDescription + " line(s) " + (lineBlock.fromLineNum + 1) + "-"
                    + (lineBlock.thruLineNum + 1) + " " );
            if (lineBlock.reportable)
            {
                int lineNum = lineBlock.fromLineNum + 1;
                for (int i = 0; i < lineBlock.lines.length; i++)
                {
                    printStream.println( pad( lineNum++ ) + ": " + lineBlock.lines[i] );
                }
            }
        }
    }

    private String pad(int lineNum)
    {
        String paddedNum = "" + lineNum;
        if (paddedNum.length() < lineNumPad.length())
            return (lineNumPad + paddedNum).substring( paddedNum.length() );
        else
            return paddedNum;
    }
}