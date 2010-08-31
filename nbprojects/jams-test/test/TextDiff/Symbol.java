package TextDiff;

/**
 * A Symbol is a unique line of text. Symbol state tells if the symbol occurs in
 * OldOnly, NewOnly, UniqueMatch (Old & New exactly once each) or Other. Saves
 * the last line number from each file.
 */
public class Symbol
{
    public static final int UNINITIALIZED = -1;
    public static final int OLDONLY = 1;
    public static final int NEWONLY = 2;
    public static final int UNIQUEMATCH = 3;
    public static final int OTHER = 4;

    /** mState is a bitmap */
    private int state = 0;
    private int[] lineNum = new int[2];

    public Symbol()
    {
    }

    /** Returns state after counting the line */
    public int countLine(int aFile, int aLineNum)
    {
        lineNum[aFile] = aLineNum;
        return adjustState( aFile + 1 );
    }

    /** Adusts and returns state */
    private int adjustState(int newState)
    {
        if ((state & newState) == newState)
            state = OTHER;
        else
            state = Math.min( OTHER, state | newState );
        return state;
    }

    public int getState()
    {
        return state;
    }

    public int getLineNum(int fileIx)
    {
        return lineNum[fileIx];
    }
}