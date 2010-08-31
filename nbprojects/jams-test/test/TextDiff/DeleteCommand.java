package TextDiff;

/**
 * Delete a block from the old file.
 */
public class DeleteCommand extends EditCommand
{
    public DeleteCommand(FileInfo oldFileInfo, FileInfo newFileInfo)
    {
        super( oldFileInfo, newFileInfo );
        command = "Delete";
        oldLines = oldFileInfo.nextBlock();
        oldLines.reportable = true;
    }
}
