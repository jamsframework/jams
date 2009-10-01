package jams.tspaces;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;

public class InitSpace {
	public String name;
	public String host;
	public String workspace;
	public String reachFile;
	public String hruFile;
	public InitSpace(String jmc){
		try{
			Reader jmcreader = new FileReader(jmc);
			BufferedReader jmcbreader = new BufferedReader(jmcreader);			
			String jmczeile=jmcbreader.readLine();
			jmczeile=jmcbreader.readLine();
			StringTokenizer jmctokenizer = new StringTokenizer(jmczeile);
			Boolean found=false;
			String xml="";
			while(jmctokenizer.hasMoreElements() && !found){
				String token=jmctokenizer.nextToken();
				if (token.length()>=16 && token.substring(0,16).equals("modeldefinition=")){
					xml=token.substring(17, token.length()-1);
					found=true;
				}
			}	
			//System.out.println(jmc.substring(0,jmc.lastIndexOf("\\")+1)+xml);
			Reader reader = new FileReader(jmc.substring(0,jmc.lastIndexOf("\\")+1)+xml);
			BufferedReader breader = new BufferedReader(reader);
			Boolean namefound=false;
			Boolean hostfound=false;
			while(!namefound||!hostfound){
				String zeile=breader.readLine();
				StringTokenizer tokenizer = new StringTokenizer(zeile);
				if (tokenizer.countTokens()>1){
					String var=tokenizer.nextToken();
					var=tokenizer.nextToken();
					if (var.equals("name=\"modelerKey\"")){
						String modelerkey=tokenizer.nextToken();
						name=modelerkey.substring(7, modelerkey.length()-3);
						namefound=true;										
					}
					if (var.equals("name=\"tSpaceIP\"")){
						String tSpaceIP=tokenizer.nextToken();
						host=tSpaceIP.substring(7, tSpaceIP.length()-3);
						hostfound=true;										
					}
					if (var.equals("name=\"workspaceDir\"")){
						int z=zeile.indexOf("value=");
						workspace=zeile.substring(z+7,zeile.length()-3)+"/";														
					}
					if (var.equals("name=\"hruFileName\"")){
						int z=zeile.indexOf("value=");
						hruFile=zeile.substring(z+7,zeile.length()-3);	
						
					}
					if (var.equals("name=\"reachFileName\"")){
						int z=zeile.indexOf("value=");
						reachFile=zeile.substring(z+7,zeile.length()-3);
					}
				}
			}
		} catch(IOException e) {
			System.out.println( "Fehler beim Lesen der Datei" );
		}
		
	}
}
