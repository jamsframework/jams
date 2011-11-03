/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.remote.common;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

/**
 *
 * @author chris
 */
public class HostInfo implements Serializable {

    private String host;
    private String port;
    private String user;
    private String pw;

    public HostInfo() {
    }

    public HostInfo(String host, String port, String user, String pw) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pw = pw;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the pw
     */
    public String getPw() {
        return pw;
    }

    /**
     * @param pw the pw to set
     */
    public void setPw(String pw) {
        this.pw = pw;
    }


    public static void main(String[] args) throws Exception{
        HostInfo test[] = new HostInfo[2];
        test[0] = new HostInfo("t1","t2","t3","t4");
        test[1] = new HostInfo("u1","u2","u3","u4");

        File inFile = new File("hosts.xml");
        XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(
                new FileOutputStream(inFile)));

        encoder.writeObject(test);
        encoder.close();
    }
}
