package TextDiff;

/**
 * A block of matching lines is either a "match" for no edit required,
 * or "move" for blocks that changed order.
 */
public class MatchCommand extends EditCommand
{
    public MatchCommand(FileInfo oldFileInfo, FileInfo newFileInfo)
    {
        super( oldFileInfo, newFileInfo );
        LineInfo oldLineInfo = oldFileInfo.currentLineInfo();
        LineInfo newLineInfo = newFileInfo.currentLineInfo();
        if (oldLineInfo.newLineNum == newFileInfo.lineNum)
        {
            command = "Match";
            newLines = newFileInfo.nextBlock();
        }
        else
        {
            command = "Move";
            newLines = newFileInfo.getBlockAt( oldLineInfo.newLineNum );
        }
        oldLines = oldFileInfo.nextBlock();
        oldLines.reportable = true;
        newLines.reportable = false;
    }
}
