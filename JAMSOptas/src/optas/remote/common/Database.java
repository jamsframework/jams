/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author chris
 */
public class Database {

    public class UserInfo{
        public String userName;
        public String password;
        public String eMail;
        public int nextJobID;
        public String userClass;
        public int userID;
        public int maxJobs;
    }

    private String hostname,  database,  username,  passwd, driver;
    private Connection con;

    public Database(String host, String db, String user, String pw) throws SQLException{
        hostname = host;
        database = db;
        username = user;
        passwd = pw;
        driver = "jdbc:mysql";

        this.con = DriverManager.getConnection(driver + "://" + hostname + "/" + database + "?autoReconnect=true", username, passwd);
    }

    public UserInfo getUserInfo(String userName) throws SQLException{
        String sqlQuery = "SELECT * FROM user WHERE Username=\"" + userName + "\";";
        Statement stmt = con.createStatement();
        ResultSet s = stmt.executeQuery(sqlQuery);
        if (!s.first())
            return null;
        UserInfo ui = new UserInfo();
        ui.userName = s.getString("Username");
        ui.eMail = s.getString("Email");
        ui.maxJobs = s.getInt("MAXJOBS");
        ui.nextJobID = s.getInt("nextJobID");
        ui.password = s.getString("Password");
        ui.userClass = s.getString("UserClass");
        ui.userID = s.getInt("ID");


        return ui;
    }

    public boolean checkAutorization(int userID, String password) throws SQLException{
        String sqlQuery = "SELECT * FROM user WHERE ID =" + userID + ";";
        Statement stmt = con.createStatement();
        ResultSet s = stmt.executeQuery(sqlQuery);
        s.next();
        String pwReference = s.getString("Password");
        if (pwReference.equals(password))
            return true;
        return false;
    }

    public ArrayList<JobState> getJobList(UserInfo userInfo) throws SQLException{
        ArrayList result = new ArrayList<JobState>();
        String sqlStmt = "SELECT id, ord(job_finished), UNIX_TIMESTAMP(start_time), host, description, workspace FROM jobs WHERE user_id = " + userInfo.userID + " ORDER BY id ASC";
        Statement stmt = con.createStatement();
        ResultSet set = stmt.executeQuery(sqlStmt);
        set.beforeFirst();
        while(set.next()){
            JobState state = new JobState();
            state.setId(set.getLong(1));
            if (set.getInt(2)==1)
                state.setIsRunning(true);
            else
                state.setIsRunning(false);
            state.setStartTime(set.getLong(3));
            state.setHost(set.getString(4));
            state.setDescription(set.getString(5));
            state.setWorkspace(set.getString(6));
            result.add(state);
        }
        return result;
    }

    public int getNextID(int userID) throws SQLException{
        String sqlQuery = "SELECT * FROM user WHERE ID =" + userID + ";";

        Statement stmt = con.createStatement();
        ResultSet s = stmt.executeQuery(sqlQuery);
        s.first();
        long jobId = s.getLong("nextJobID");

        String sqlExec = "UPDATE user SET nextJobID="+jobId+" WHERE ID =" + userID + ";";
        stmt.execute(sqlExec);

        return (int)jobId;
    }


}
