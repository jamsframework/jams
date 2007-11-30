/*
 * UploadFile.java
 *
 * Created on 16. November 2007, 10:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sourceforge.jwbf.actions.http.mw.api;

import java.io.File;
import java.io.IOException;
import net.sourceforge.jwbf.actions.http.mw.MWAction;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;

/**
 *
 * @author Christian Fischer
 */
public class UploadFile extends MWAction{
    private static final Logger LOG = Logger.getLogger(UploadFile.class);
    /** Creates a new instance of UploadFile */
    public UploadFile(String fileName,HttpClient client) {
        /*String uS = "";
	//try {
		uS = "/index.php?title=Spezial:Upload&action=submit" /*+ URLEncoder.encode(a.getLabel(), MediaWikiBot.CHARSET)
					+ "&action=submit"*/;
	/*} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}*/

	/*NameValuePair wpUploadFile = new NameValuePair("wpUploadFile", fileName);
        //new FilePart("wpUploadFile", f.getName(), f)
        NameValuePair wpDestFile = new NameValuePair("wpDestFile", "test.ps");
	NameValuePair wpUploadDescription = new NameValuePair("wpUploadDescription","no description1");
	NameValuePair wpWatchthis = new NameValuePair("wpWatchthis","false");
	NameValuePair wpIgnoreWarning = new NameValuePair("wpIgnoreWarning", "true");
										
		
	LOG.info("UPLOAD: " + fileName);
	PostMethod pm = new PostMethod(uS);
	pm.getParams().setContentCharset(MediaWikiBot.CHARSET);

	pm.setRequestBody(new NameValuePair[] { wpUploadFile,wpDestFile, wpUploadDescription,
			wpWatchthis, wpIgnoreWarning});
	msgs.add(pm);*/

        String baseURL = "http://jams.uni-jena.de/jamswiki/index.php";
        String URL = baseURL + "?title=Special:Upload&action=submit";
        PostMethod upload = new PostMethod(URL);
        HttpMethodBase currentMethod = upload;
        File f = new File(fileName);
	
        try {
            Part[] parts = {
                new StringPart("wpUploadDescription", "no description"),
                new StringPart("wpUploadAffirm", "1"),
                new StringPart("wpIgnoreWarning", "1"),
                new StringPart("wpUpload", "Upload file"),
                new FilePart("wpUploadFile", f.getName(), f)
            };
            upload.setRequestEntity( new MultipartRequestEntity(parts, upload.getParams()) );
        }catch(Exception e){
            System.out.println("unknown error: " + e.toString());
            e.printStackTrace();
        }
	int status = 1;
	try {
	status = client.executeMethod(upload);
	} catch (HttpException e) {
            e.printStackTrace();
            //disposeClient();
            } catch (IOException e) {
            	e.printStackTrace();
		//disposeClient();
            } finally {
		upload.releaseConnection();
		currentMethod = null;
	}
	
		//return (status == 302);                                
    }    
}
