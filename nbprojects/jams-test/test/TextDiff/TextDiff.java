package TextDiff;

import java.io.File;

/**
 * Compares two text files or arrays of strings and generates a report of edit
 * commands that would transform Old to New.
 */
public class TextDiff
{
    public static final int OLD = 0;
    public static final int NEW = 1;

    private SymbolCollection symbols;
    private FileInfo oldFileInfo;
    private FileInfo newFileInfo;

    public TextDiff()
    {
    }

    /** Compare two named Files */
    public Report compare(String oldFileName, String newFileName) throws Exception
    {
        return compare( new File( oldFileName ), new File( newFileName ) );
    }

    /** Compare two Files */
    public Report compare(File oldFile, File newFile) throws Exception
    {
        String[] lOld = new TextFileIn( oldFile ).asArray();
        String[] lNew = new TextFileIn( newFile ).asArray();
        return compare( lOld, lNew );
    }

    /** Compare two string arrays */
    public Report compare(String[] oldLines, String[] newLines)
    {
        createFileInfo( oldLines, newLines );
        createSymbols();
        createLineInfo();
        stretchMatches( oldFileInfo );
        return new Report( oldFileInfo, newFileInfo );
    }

    private void createFileInfo(String[] oldLines, String[] newLines)
    {
        oldFileInfo = new FileInfo( oldLines );
        newFileInfo = new FileInfo( newLines );
    }

    /** Create a symbol for each unique string */
    private void createSymbols()
    {
        symbols = new SymbolCollection();
        createSymbols( oldFileInfo, OLD );
        createSymbols( newFileInfo, NEW );
    }

    private void createSymbols(FileInfo fileInfo, int fileIx)
    {
        for (int line = 0; line < fileInfo.length; line++)
            symbols.registerSymbol( fileIx, fileInfo.lines[line], line );
    }

    /** Initial line status is symbol status. Cross link any UniqueMatch lines */
    private void createLineInfo()
    {
        createLineInfo( oldFileInfo );
        createLineInfo( newFileInfo );
    }

    private void createLineInfo(FileInfo fileInfo)
    {
        for (int line = 0; line < fileInfo.length; line++)
        {
            LineInfo lineInfo = new LineInfo();
            fileInfo.lineInfo[line] = lineInfo;
            Symbol symbol = symbols.getSymbolFor( fileInfo.lines[line] );
            lineInfo.lineStatus = symbol.getState();
            if (lineInfo.isMatch())
            {
                lineInfo.oldLineNum = symbol.getLineNum( OLD );
                lineInfo.newLineNum = symbol.getLineNum( NEW );
            }
        }
    }

    /** Stretch each unique-match in the FileInfo. */
    private void stretchMatches(FileInfo fileInfo)
    {
        int FORWARD = 1;
        int BACKWARD = -1;
        int lBlockNum = 0;
        for (int line = 0; fileInfo.isValidLineNum( line ); line++)
        {
            LineInfo lineInfo = fileInfo.lineInfo[line];
            if ((lineInfo.isMatch()) && (lineInfo.blockNum == 0))
            {
                lBlockNum++;
                stretchOneMatch( lBlockNum, lineInfo.oldLineNum, lineInfo.newLineNum, FORWARD );
                stretchOneMatch( lBlockNum, lineInfo.oldLineNum, lineInfo.newLineNum, BACKWARD );
            }
        }
    }

    /**
     * Find more matching lines before or after a unique match and mark them as
     * unique match, too. If unique match lines are separated by matching but
     * non-unique lines this will merge them all into one block.
     */
    private void stretchOneMatch(int blockNum, int oldLineNum, int newLineNum, int whichWay)
    {
        int lOldLineNum = oldLineNum;
        int lNewLineNum = newLineNum;
        while (true)
        {
            oldFileInfo.setBlockNumber( lOldLineNum, blockNum );
            newFileInfo.setBlockNumber( lNewLineNum, blockNum );
            oldFileInfo.lineInfo[lOldLineNum].newLineNum = lNewLineNum;
            newFileInfo.lineInfo[lNewLineNum].oldLineNum = lOldLineNum;

            lOldLineNum += whichWay;
            lNewLineNum += whichWay;
            if (!(oldFileInfo.isValidLineNum( lOldLineNum ) && newFileInfo.isValidLineNum( lNewLineNum ) && oldFileInfo.lines[lOldLineNum]
                    .equals( newFileInfo.lines[lNewLineNum] )))
                break;
        }
    }
}
