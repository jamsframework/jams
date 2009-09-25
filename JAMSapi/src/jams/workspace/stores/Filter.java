/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.workspace.stores;

import jams.model.Context;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Filter extends Serializable {

    Context getContext();

    String getContextName();

    String getExpression();

    Pattern getPattern();

    void setContext(Context context);

    void setPattern(Pattern pattern);

}
