package TextDiff;

/**
 * Append a block of lines to the end of the old file.
 */
public class AppendCommand extends EditCommand
{
    public AppendCommand(FileInfo oldFileInfo, FileInfo newFileInfo)
    {
        super( oldFileInfo, newFileInfo );
        command = "Append";
        newLines = newFileInfo.nextBlock();
        newLines.reportable = true;
    }
}
