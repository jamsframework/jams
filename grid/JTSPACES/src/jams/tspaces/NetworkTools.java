/*
 * NetworkTools.java
 *
 * Created on 5. Juni 2007, 17:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.tspaces;
 import java.net.*;
import org.unijena.jams.data.JAMSString;
/**
 *
 * @author ncb
 */
public class NetworkTools {
   
    /** Creates a new instance of NetworkTools */
    public NetworkTools() {
    }
    


  public static JAMSString getLocalHost()
  {
    JAMSString localHost=null;  
    InetAddress iaHost = null;  

    
    try
    {
     
         localHost =  new JAMSString(iaHost.getLocalHost()+"");
      
    } catch( Exception ex ){}
    
      
    
    return localHost;
  }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       
       // NetworkTools networkTools = new NetworkTools();
        
        System.out.println(NetworkTools.getLocalHost().getValue());
    }
    
}
