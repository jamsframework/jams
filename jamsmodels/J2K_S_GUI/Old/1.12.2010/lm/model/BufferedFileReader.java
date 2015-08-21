/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Observable;



/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class BufferedFileReader extends Observable {

    private FileReader reader;
    private BufferedReader BuffRead;
    private String lmarabledir="";
    private ArrayList<String> CID = new ArrayList();

    public void init(String dir){

        lmarabledir=dir;
        load_lmarable();

    }
    public void load_lmarable(){

        
      
        try{
            reader = new FileReader(lmarabledir);
            BuffRead = new BufferedReader(reader);
            System.out.println(BuffRead.readLine());
            String t="#";
            while(t.startsWith("#")){
                t = BuffRead.readLine();
                System.out.println(t);

                }
            System.out.println("START DER AUFZEICHNUNG");
            String r ="";
            while(!r.startsWith("#")){
                r=BuffRead.readLine();
                System.out.println(r);
                    int i = 0;
                    String s="";
                    while(r.charAt(i)!='\t'){
                        s=s+r.charAt(i);
                        i++;
                    }
                    CID.add(s);
                    System.out.println(s);
                }
        }
        catch(Exception e){

        }
        setChanged();
        notifyObservers(CID);



    }
    public ArrayList getArrayList(){
        return CID;
    }



}
