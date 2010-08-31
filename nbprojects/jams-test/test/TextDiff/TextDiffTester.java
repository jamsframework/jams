package TextDiff;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;



public class TextDiffTester extends TestCase
{

    public void testBothEmpty()
    {
        Report lReport = runCompare( new String[0], new String[0] );
        lReport.print();
        assertEquals( 0, lReport.size() );
    }

    public void testNewEmpty()
    {
        Report lReport = runCompare( new String[] { "one" }, new String[0] );
        assertEquals( 1, lReport.size() );
        assertEquals( "Delete", lReport.getCommand( 0 ).command );
    }

    public void testOldEmpty()
    {
        Report lReport = runCompare( new String[0], new String[] { "one" } );
        assertEquals( 1, lReport.size() );
        assertEquals( "Append", lReport.getCommand( 0 ).command );
    }

    public void testMatch()
    {
        Report lReport = runCompare( new String[] { "one", "two", "three", "four" }, new String[] { "one", "two",
                "three", "four" } );
        lReport.print();
        assertEquals( 1, lReport.size() );
        EditCommand lCmd = lReport.getCommand( 0 );
        assertEquals( "Match", lCmd.command );
        assertEquals( 4, lCmd.oldLines.lines.length );
    }

    public void testChange()
    {
        Report lReport = runCompare( new String[] { "one", }, new String[] { "two", } );
        lReport.print();
        assertEquals( 1, lReport.size() );
        EditCommand lCmd = lReport.getCommand( 0 );
        assertEquals( "Change", lCmd.command );

        lReport = runCompare( new String[] { "one", "two" }, new String[] { "one", "three" } );
        lReport.print();
        assertEquals( 2, lReport.size() );
        lCmd = lReport.getCommand( 0 );
        assertEquals( "Match", lCmd.command );
        lCmd = lReport.getCommand( 1 );
        assertEquals( "Change", lCmd.command );

        lReport = runCompare( new String[] { "one", "two", "three" }, new String[] { "one", "four", "three" } );
        lReport.print();
        assertEquals( 3, lReport.size() );
        lCmd = lReport.getCommand( 0 );
        assertEquals( "Match", lCmd.command );
        lCmd = lReport.getCommand( 1 );
        assertEquals( "Change", lCmd.command );
        lCmd = lReport.getCommand( 2 );
        assertEquals( "Match", lCmd.command );
    }

    public void testDelete()
    {
        Report lReport = runCompare( new String[] { "one", "two", "three", "four" }, new String[] { "one", "four" } );
        lReport.print();
        assertEquals( 3, lReport.size() );
        assertEquals( "Match", lReport.getCommand( 0 ).command );
        assertEquals( "Delete", lReport.getCommand( 1 ).command );
        assertEquals( "Match", lReport.getCommand( 2 ).command );
    }

    public void testInsert()
    {
        Report lReport = runCompare( new String[] { "one", "four" }, new String[] { "one", "two", "three", "four" } );
        lReport.print();
        assertEquals( 3, lReport.size() );
        assertEquals( "Match", lReport.getCommand( 0 ).command );
        assertEquals( "Insert before", lReport.getCommand( 1 ).command );
        assertEquals( "Match", lReport.getCommand( 2 ).command );
    }

    public void testMove()
    {
        Report lReport = runCompare( new String[] { "a", "b", "c", "d" }, new String[] { "a", "c", "d", "b" } );
        lReport.print();
        assertEquals( 3, lReport.size() );
        assertEquals( "Match", lReport.getCommand( 0 ).command );
        assertEquals( "Move", lReport.getCommand( 1 ).command );
        assertEquals( "Match", lReport.getCommand( 2 ).command );
    }

    public void testMoveAndAppend()
    {
        Report lReport = runCompare( new String[] { "a", "b", "c", "d" }, new String[] { "a", "c", "d", "x", "y", "z",
                "b" } );
        lReport.print();
        assertEquals( 4, lReport.size() );
        assertEquals( "Match", lReport.getCommand( 0 ).command );
        assertEquals( "Move", lReport.getCommand( 1 ).command );
        assertEquals( "Match", lReport.getCommand( 2 ).command );
        assertEquals( "Append", lReport.getCommand( 3 ).command );
    }

    public void testAmbiguous()
    {
        Report lReport = runCompare( new String[] { "a", "b", "a", "b" }, new String[] { "b", "a", "b", "a" } );
        lReport.print();
        assertEquals( 1, lReport.size() );
        assertEquals( "Change", lReport.getCommand( 0 ).command );
    }

    // Reproduced SSWiki defect D2.
    public void testAmbiguousAfterChange()
    {
        Report lReport = runCompare( new String[] { "a", " ", "b", " ", "c", " ", "d" }, new String[] { "a", " ", "b",
                " ", "x", " ", "d" } );
        lReport.print();
        assertEquals( 3, lReport.size() );
        assertEquals( "Match", lReport.getCommand( 0 ).command );
        assertEquals( "Change", lReport.getCommand( 1 ).command );
        assertEquals( "Match", lReport.getCommand( 2 ).command );
    }

    private Report runCompare(String[] aOld, String[] aNew)
    {
        
        return new TextDiff().compare( aOld, aNew );
    }

    // -------------- Housekeeping -----------
    public TextDiffTester(java.lang.String testName)
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
        TestSuite suite = new TestSuite( TextDiffTester.class );
        return suite;
    }

}
