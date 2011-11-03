/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;

import java.io.Serializable;

/**
 *
 * @author chris
 */
public class Commands {
    static public class ConnectCommand extends JAMSCommand{
        String account;
        String password;

        public final static String GRANTED = "GRANTED";
        public final static String REFUSED = "REFUSED";
        public ConnectCommand(String account, String password){
            this.account = account;
            this.password = password;
        }

        public String getAccount(){
            return account;
        }

        public String getPassword(){
            return password;
        }
    }
   
    static public class GetJobListCommand extends JAMSCommand{
        public GetJobListCommand(){            
        }

    }

    static public class GetFileListCommand extends JAMSCommand{
        int jobId;
        public GetFileListCommand(int jobId){
            this.jobId = jobId;
        }

        public int getJobId(){
            return jobId;
        }
    }

    static public class GetFileCommand extends JAMSCommand{
        int jobId;
        String subPath;

        public GetFileCommand(int jobId, String subPath){
            this.jobId = jobId;
            this.subPath = subPath;
        }

        public int getJobId(){
            return jobId;
        }

        public String getSubPath(){
            return subPath;
        }
    }

    public static class CommandAnswer extends JAMSCommand{
        long id;
        Serializable data;

        public CommandAnswer(long id, Serializable s){
            this.id = id;
            this.data = s;
        }
    }
}
