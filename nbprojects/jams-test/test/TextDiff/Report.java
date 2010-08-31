package TextDiff;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Creates and holds a List of edit commands that can be used to tranform Old
 * into New. The constructor does all the work. Use any methods of the ancestor
 * ArrayList to access the commands. getCommand is a get wrapped to do the
 * casting to EditCommand.
 */
public class Report extends ArrayList
{
    public Report()
    {
        super();
    }

    /**
     * Create a Report (list of edit commands) from old and new file info.
     */
    public Report(FileInfo oldFileInfo, FileInfo newFileInfo)
    {
        while (true)
        {
            LineInfo oldLineInfo = oldFileInfo.currentLineInfo();
            LineInfo newLineInfo = newFileInfo.currentLineInfo();
            if (oldLineInfo.isEOF() && newLineInfo.isEOF())
                break;

            else if (oldLineInfo.isEOF() && newLineInfo.isNewOnly())
                this.add( new AppendCommand( oldFileInfo, newFileInfo ) );

            else if (oldLineInfo.isOldOnly() && newLineInfo.isNewOnly())
                this.add( new ChangeCommand( oldFileInfo, newFileInfo ) );

            else if (newLineInfo.isNewOnly())
                this.add( new InsertCommand( oldFileInfo, newFileInfo ) );

            else if (oldLineInfo.isOldOnly())
                this.add( new DeleteCommand( oldFileInfo, newFileInfo ) );

            else if (oldLineInfo.isMatch())
                this.add( new MatchCommand( oldFileInfo, newFileInfo ) );

            else if (newLineInfo.isMatch())
                newFileInfo.nextBlock(); // discard
        }
    }

    /**
     * Write a report to standard out.
     */
    public void print()
    {
        print( System.out );
    }

    /**
     * Write a report to any PrintStream
     */
    public void print(PrintStream stream)
    {
        write( new DefaultReportWriter( stream ) );
    }

    /**
     * Write a report using an IReportWriter. Use a custom report writer to
     * capture the report to something other than a PrintStream.
     */
    public void write(IReportWriter writer)
    {
        int count = this.size();
        for (int i = 0; i < count; i++)
        {
            writer.report( getCommand( i ) );
        }
    }

    public EditCommand getCommand(int lineNum)
    {
        return (EditCommand) super.get( lineNum );
    }
}
