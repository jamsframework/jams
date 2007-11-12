/*
 * FindPage.java
 *
 * Created on 12. November 2007, 09:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sourceforge.jwbf.actions.http.mw.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jwbf.actions.http.ProcessException;
import net.sourceforge.jwbf.actions.http.mw.MWAction;
import net.sourceforge.jwbf.bots.MediaWikiBot;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

/**
 *
 * @author admin
 */
public class FindPage extends MWAction implements MultiAction<String>  {    
    private static final Logger LOG = Logger.getLogger(FindPage.class);
    
    private Collection<String> titleCollection = new ArrayList<String>();
    
    //this component finds all pages with given search string, without use of wikimedia api.
    //if wikimedia is installed there far better ways to perform this action
    public FindPage(String searchString) {	
	String uS = "";
        //search for pages, and show first 5000 results 
	try {
		uS = "/index.php?title=Spezial:Suche&ns0=1&redirs=1&searchx=1&search="+URLEncoder.encode(searchString, MediaWikiBot.CHARSET)+"&limit=5000&offset=0";
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	LOG.debug(uS);
	msgs.add(new GetMethod(uS));		
    }
    //parse page for search hits
    public String processAllReturningText(final String s) throws ProcessException {
	String t = encodeUtf8(s);
	parseArticleTitles(t);	
	return "";
	}
            
    /**
    * picks the article name from a MediaWiki api response.
    *	
    * @param s   text for parsing
    */
    public void parseArticleTitles(String s) {
		
	// get the backlink titles and add them all to the titleCollection
        int start = 0,starttitle=0,endtitle=0;
        //each result begings with <li> ... 
        while ( (start = s.indexOf("<li><a href=\"",start)) != -1) {
            start += 13;
            //we are interessted in the title 
            starttitle = s.indexOf("\" title=",start) + 9;
            if (starttitle == - 1) {
                continue;
            }
            //which ends with >
            endtitle = s.indexOf(">",starttitle)-1;
            if (endtitle == - 1) {
                continue;
            }
            String title = s.substring(starttitle,endtitle);
            //save result
            titleCollection.add(title);
        }                        	
    }
    
    /**
    * @return   the collected article names
     */
    public Collection<String> getResults() {
        return titleCollection;	 
    }
	
    /**
     * @return   necessary information for the next action
     *           or null if no next api page exists
     */
    //attention, if there are more than 5.000 results, only the first 5.000 are processed and the rest is skipped
    public GetAllPageTitles getNextAction() {    	
        return null;
    }
}
