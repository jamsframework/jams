package TextDiff;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FileInfoTester extends TestCase
{
    public void testBlock()
    {
        FileInfo lInfo = new FileInfo( new String[] { "one", "two", "three" } );
        lInfo.lineInfo = new LineInfo[3];
        lInfo.lineInfo[0] = new LineInfo( LineInfo.UNIQUEMATCH, 0, 0, 0 );
        lInfo.lineInfo[1] = new LineInfo( LineInfo.OLDONLY, 1, 1, 1 );
        lInfo.lineInfo[2] = new LineInfo( LineInfo.OLDONLY, 2, 2, 1 );
        LineBlock lBlock = lInfo.nextBlock();
        assertEquals( 1, lBlock.lines.length );
        lBlock = lInfo.nextBlock();
        assertEquals( 2, lBlock.lines.length );
        lBlock = lInfo.nextBlock();
        assertNull( lBlock );
    }

    // -------------- Housekeeping -----------
    public FileInfoTester(java.lang.String testName)
    {
        super( testName );
    }

    public void setUp()
    {
    }

    public static void main(java.lang.String[] args)
    {
        junit.textui.TestRunner.run( suite() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( FileInfoTester.class );
        return suite;
    }

}
