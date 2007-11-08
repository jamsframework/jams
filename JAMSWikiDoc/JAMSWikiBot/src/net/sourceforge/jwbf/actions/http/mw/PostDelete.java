/*
 * Copyright 2007 Thomas Stock.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors:
 * 
 */
package net.sourceforge.jwbf.actions.http.mw;

import java.util.Hashtable;
import net.sourceforge.jwbf.actions.http.ProcessException;

import net.sourceforge.jwbf.bots.MediaWikiBot;
import org.apache.commons.httpclient.HttpMethod;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;


/**
 * @author Thomas Stock
 * @deprecated
 */
public class PostDelete extends MWAction {
        String label;
        Hashtable<String, String> tab;
        boolean firstcall = true;
	/**
	 * 
	 * @param label of the article
	 * @param tab with contains environment variable "wpEditToken"
	 * @deprecated
	 */
	public PostDelete(final String label, Hashtable<String, String> tab,boolean firstCall) {
                this.firstcall = firstcall;
		this.label = label;
                this.tab = tab;
                tab.put("firstCall","true");
		NameValuePair action = new NameValuePair("wpConfirmB", "Delete Page");
		// this value is preseted
		NameValuePair wpReason = new NameValuePair("wpReason", "hier der Grund");
		wpReason.setName("backdraft");
		
		NameValuePair wpEditToken = new NameValuePair("wpEditToken", tab
				.get("wpEditToken"));

		PostMethod pm = new PostMethod(
				"/index.php?title=" + label + "&action=delete");
                                //"/api.php?action=delete&title=" + label + "&token=" + tab.get("wpEditToken") );
                
		pm.setRequestBody(new NameValuePair[] { action, wpReason, wpEditToken });
		pm.getParams().setContentCharset(MediaWikiBot.CHARSET);
		msgs.add(pm);
                		
	}
	
        public String processReturningText(final String s, HttpMethod hm) throws ProcessException {            
            if (!firstcall)
                return s;
            firstcall = false;
            String t = encodeUtf8(s);

            int index = t.indexOf("name='wpEditToken' value=") + 26;
            int i=0;
            String token = "";
        
            while(t.charAt(index+i) != '"' ) {
                i++;
            }
            token = t.substring(index,index+i);
            /*Pattern p = Pattern.compile("lgtoken=.*?",Pattern.DOTALL | Pattern.MULTILINE);
			
            Matcher m = p.matcher(s);
                
            if (m.find()) {			
                tokenCollection.add(m.group(1));
                return m.group(1);
            }
            	       	        
            return "";*/
            /*tokenCollection.add(loginID);
            return loginID;*/
            tab.put("wpEditToken",token);
        
            NameValuePair action = new NameValuePair("wpConfirmB", "Delete Page");
		// this value is preseted
		NameValuePair wpReason = new NameValuePair("wpReason", "hier der Grund");
		wpReason.setName("backdraft");
		
		NameValuePair wpEditToken = new NameValuePair("wpEditToken", tab
				.get("wpEditToken"));
                
            PostMethod pm = new PostMethod(
				"/index.php?title=" + label + "&action=delete");
                                //"/api.php?action=delete&title=" + label + "&token=" + tab.get("wpEditToken") );
                
            pm.setRequestBody(new NameValuePair[] { action, wpReason, wpEditToken });
            pm.getParams().setContentCharset(MediaWikiBot.CHARSET);
            
            //msgs.add(pm);
            
            return s;
        }
}
