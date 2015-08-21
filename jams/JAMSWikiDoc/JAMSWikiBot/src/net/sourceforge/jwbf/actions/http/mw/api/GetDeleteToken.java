/*
 * RemovePage.java
 *
 * Created on 6. November 2007, 12:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package actions.http.mw.api;

import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.jwbf.actions.http.ProcessException;
import net.sourceforge.jwbf.actions.http.mw.MWAction;
import net.sourceforge.jwbf.actions.http.mw.api.MultiAction;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * @author admin
 */
public class GetDeleteToken extends MWAction implements MultiAction<String>{
    
    String token = null;
    String title;
    private Collection<String> tokenCollection = new ArrayList<String>();
    
    /** Creates a new instance of RemovePage */
    public GetDeleteToken(String title) {
        this.title = title;
        try {
        GetMethod getMethod1 = new GetMethod("/api.php?action=login&lgname=WikiSysop&lgpassword=borlandc&format=xml");
                
                //"/api.php?action=query&prop=info&intoken=edit&titles=" + URLEncoder.encode(title, MediaWikiBot.CHARSET) + "&format=xml");
                
        /*GetMethod getMethod2 = new GetMethod(
				"/api.php?action=delete&title=" + title + "&token=");*/
	System.out.println(getMethod1.toString());
        //System.out.println(getMethod2.toString());
	msgs.add(getMethod1);
        //msgs.add(getMethod2);
        }catch(Exception e) {
            System.out.println("Fehler!");
        }
    }    

    public Collection<String> getResults() {
	return tokenCollection;	 
    }
    
    public GetDeleteToken getNextAction() {
	//return new GetDeleteToken(title);
        return null;
    }
    
    public String processAllReturningText(final String s) throws ProcessException {
        String t = encodeUtf8(s);

        int index = t.indexOf("lgtoken=") + 9;
        int i=0;
        String loginID = "";
        
        while(t.charAt(index+i) != '"' ) {
            i++;
        }
        loginID = t.substring(index,index+i);
        /*Pattern p = Pattern.compile("lgtoken=.*?",Pattern.DOTALL | Pattern.MULTILINE);
			
        Matcher m = p.matcher(s);
                
        if (m.find()) {			
            tokenCollection.add(m.group(1));
            return m.group(1);
        }
            	       	        
        return "";*/
        tokenCollection.add(loginID);
        return loginID;
    }
}
