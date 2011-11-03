/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;

import optas.remote.server.ServerOPTAS;
import jams.runtime.JAMSLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import optas.remote.common.Commands.CommandAnswer;

/**
 *
 * @author chris
 */
public class JAMSConnection {
    int BUFFER_SIZE = ServerOPTAS.BUFFER_SIZE;

    public enum Operation{Read, Write};

    private Socket socket;    
    byte buffer[] = null;

    boolean isConnected = false;

    private OutputStream out;
    private InputStream in;

    private JAMSLog errorLog = new JAMSLog();
    private JAMSLog infoLog = new JAMSLog();

    LinkedList<JAMSCommand> commandBuffer = new LinkedList<JAMSCommand>();
    ArrayList<ReceivePacketListener> listenerList = new ArrayList<ReceivePacketListener>();
    ArrayList<ConnectionStateListener> connectionStateListenerList = new ArrayList<ConnectionStateListener>();

    HashMap<Long, Object> resultMap = new HashMap<Long, Object>();

    public abstract static class ReceivePacketListener{
        public abstract void packetReceived(JAMSConnection connection, Operation operation, int bytes, int bytesTotal, Object o);
    }
    public abstract static class ConnectionStateListener{
        public abstract void stateChanged(JAMSConnection connection, int state);
    }

    public abstract static class CommandHandler{
        public abstract boolean handle(JAMSConnection connection, JAMSCommand command);
    }
    
    CommandHandler handler;

    boolean block = false;
    boolean isAlive = false;
    boolean isBusy = false;

    final int CONNECTED = 0;
    final int DISCONNECTED = 1;
    
    public JAMSConnection(Socket socket, CommandHandler handler, JAMSLog infoLog, JAMSLog errorLog) throws IOException{
        this.socket = socket;

        if (this.socket.isConnected())
            this.setConnectionState(CONNECTED);
        
        if (handler == null){
            this.handler = new CommandHandler() {

                @Override
                public boolean handle(JAMSConnection connection, JAMSCommand command) {
                    connection.answer(command, new CommunicationException("unknown command" + command.getName()));
                    return true;
                }
            };
        }else
            this.handler = handler;
        if (infoLog != null)
            this.infoLog = infoLog;
        if (errorLog != null)
            this.errorLog = errorLog;

        out = socket.getOutputStream();
        in = socket.getInputStream();

        buffer = new byte[ServerOPTAS.BUFFER_SIZE];

        Thread tAliveMonitor = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!JAMSConnection.this.socket.isClosed()) {
                    if (isBusy==true)
                        isAlive = true;
                    else
                        isAlive = false;
                    isBusy = false;

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }

                }
            }
        });
        Thread tWorker = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!JAMSConnection.this.socket.isClosed()) {
                    if (!commandBuffer.isEmpty()) {
                        block = true;
                        JAMSCommand cmd = commandBuffer.pop();
                        block = false;
                        CommandAnswer result = performCommand(cmd);
                        if (result!=null){
                            block = true;
                            resultMap.put(result.id, result.data);
                            block = false;
                        }
                    }
                    int bytesAvail = 0;
                    try{
                        bytesAvail = in.available();
                    }catch(IOException ioe){
                        bytesAvail = 0;
                        ioe.printStackTrace();
                    }

                    if (bytesAvail>0) {
                        Object cmd = null;
                        try{
                            cmd = receiveObject();
                        }catch(IOException ioe){
                            close();
                            return;
                        }
                        if (cmd instanceof JAMSCommand) {
                            if (cmd != null) {                                
                                JAMSConnection.this.handler.handle(JAMSConnection.this, (JAMSCommand)cmd);
                            }
                        }
                    }
                    try{
                        Thread.sleep(100);
                    }catch(Exception e){

                    }
                }
            }
        });
        tAliveMonitor.start();
        tWorker.start();
    }

    private void setConnectionState(int state){
        if (state == CONNECTED)
            this.isConnected = true;
        else
            this.isConnected = false;

        for (ConnectionStateListener listener : connectionStateListenerList)
            listener.stateChanged(this, state);
    }

    public void setHandler(CommandHandler handler){
        this.handler = handler;
    }

    public Socket getSocket(){
        return this.socket;
    }

    public void addConnectionStateListener(ReceivePacketListener rpl){
        listenerList.add(rpl);
    }

    public void removeConnectionStateListener(ReceivePacketListener rpl){
        listenerList.remove(rpl);
    }

    public void addReceivePacketListener(ReceivePacketListener rpl){
        listenerList.add(rpl);
    }

    public void removeReceivePacketListener(ReceivePacketListener rpl){
        listenerList.remove(rpl);
    }

    private void notify(Operation operation, int bytes, int bytesTotal, Object o){
        isBusy = true;
        for (ReceivePacketListener listener : this.listenerList){
            listener.packetReceived(this, operation, bytes, bytesTotal, o);
        }
    }

    private void sendCommand(JAMSCommand cmd) throws IOException{
        sendData(cmd);
    }

    private Object receiveObject() throws IOException{
        Object o = null;
        try {
            int length = readInt();
            if (length == -1){
                close();
                return null;
            }
            byte[] data = new byte[length];

            int offset = 0;
            int numRead = 0;
            int outstanding = length;

            while ((offset < length) && ((numRead = in.read(data, offset, outstanding)) > 0)) {
                offset += numRead;
                outstanding = length - offset;
                notify(Operation.Read, offset, length, null);
            }
            if (offset < length) {
                close();
                throw new IOException("Could not completely read from stream, numRead=" + numRead + ", ret.length=" + length); // ???
            }

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (ClassNotFoundException e) {
            return e;
        }
        return o;

    }

    private void sendData(Object o) throws IOException{
        //convert cmd into bytearray
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.flush();
        oos.close();
        baos.flush();
        byte data[] = baos.toByteArray();
        baos.close();

        long length = data.length;
        if (length>Integer.MAX_VALUE){
            close();
            throw new IOException("Objectsize exceeded 2gb");
        }

        sendInt( data.length);

        int offset = 0;
        while(offset < data.length){
            int numWrite = Math.min(BUFFER_SIZE, data.length-offset);
            out.write(data, offset, numWrite);
            offset += numWrite;
            notify(Operation.Write, offset, data.length, o);
        }
        out.flush();
    }
    
    private CommandAnswer performCommand(JAMSCommand cmd){
        Object result = null;
        try{
            this.sendCommand(cmd);
            if (!(cmd instanceof CommandAnswer))
                result = this.receiveObject();
            else
                return null;
        }catch(IOException ioe){
            close();
            ioe.printStackTrace();
            return new CommandAnswer(cmd.getId(),ioe);
        }
        if (result == null){
            return new CommandAnswer(cmd.getId(),new CommunicationException("received no answer for command: " + cmd));
        }
        if (!(result instanceof CommandAnswer))
            return new CommandAnswer(cmd.getId(),new CommunicationException("excepeted to receive container command, but received" + cmd.toString()));

        return ((CommandAnswer)result);
    }
    
    public void answer(JAMSCommand question, Serializable answer){
        while (block){
            try{
                Thread.sleep(10);
            }catch(Exception e){}
        }
        this.commandBuffer.push(new CommandAnswer(question.getId(), answer));
    }

    public Object perform(JAMSCommand cmd, long timeout) throws CommunicationException{
        while (block){
            try{
                Thread.sleep(10);
            }catch(Exception e){}
        }
        this.commandBuffer.push(cmd);
        long startTime = System.currentTimeMillis();
        long waitTime = 0;

        while(block || !this.resultMap.containsKey(cmd.getId())){
            try{
                Thread.sleep(10);
            }catch(Exception e){}

            if (!this.isAlive){
                waitTime = System.currentTimeMillis() - startTime;
                if (waitTime>timeout){
                    close();
                    throw new CommunicationException("Timeout");
                }
            }
            if (this.socket.isClosed()){
                close();
                throw new CommunicationException("Socket closed");
            }

        }
        Object o = this.resultMap.get(cmd.getId());
        this.resultMap.put(cmd.getId(), null);
        return o;
    }

    public void close(){
        try{
            this.setConnectionState(DISCONNECTED);
            this.socket.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            errorLog.print(ioe.toString());
        }
    }

    private int readInt() throws java.io.IOException {
        try {
            byte[] byte_array_4 = new byte[4];

            byte_array_4[0] = (byte) in.read();
            byte_array_4[1] = (byte) in.read();
            byte_array_4[2] = (byte) in.read();
            byte_array_4[3] = (byte) in.read();

            return toInt(byte_array_4);
        } catch (SocketException se) {
            se.printStackTrace();
            close();
            return -1;
        }
    }

    private void sendInt(int i) throws java.io.IOException {
        byte[] byte_array_4 = toByteArray(i);
        out.write(byte_array_4);
    }

    private static int toInt(byte[] byte_array_4) {
        int ret = 0;
        for (int i = 0; i < 4; i++) {
            int b = (int) byte_array_4[i];
            if (i < 3 && b < 0) {
                b = 256 + b;
            }
            ret += b << (i * 8);
        }
        return ret;
    }

    private static byte[] toByteArray(int in_int) {
        byte a[] = new byte[4];
        for (int i = 0; i < 4; i++) {

            int b_int = (in_int >> (i * 8)) & 255;
            byte b = (byte) (b_int);

            a[i] = b;
        }
        return a;
    }
}
