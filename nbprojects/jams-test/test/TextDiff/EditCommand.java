package TextDiff;

/**
 * Can be used to transform Old into New.
 * Concrete classes fill in the command name and old and new lines affected as
 * appropriate.
 */
public abstract class EditCommand
{
    public String command = "undefined";
    public LineBlock oldLines = null;
    public LineBlock newLines = null;

    public EditCommand(FileInfo oldFileInfo, FileInfo newFileInfo)
    {
    }
}