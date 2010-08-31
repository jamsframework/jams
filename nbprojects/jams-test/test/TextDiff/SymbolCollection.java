package TextDiff;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages a set of Symbols. Current implementation is a HashMap.
 */
public class SymbolCollection
{
    private Map symbols = new HashMap();

    public Symbol getSymbolFor(String line)
    {
        Symbol symbol = (Symbol) symbols.get( line );
        if (null == symbol)
        {
            symbol = new Symbol();
            symbols.put( line, symbol );
        }
        return symbol;
    }

    public int registerSymbol(int fileIx, String line, int lineNum)
    {
        return getSymbolFor( line ).countLine( fileIx, lineNum );
    }

    public int getStateOf(String line)
    {
        return getSymbolFor( line ).getState();
    }
}
