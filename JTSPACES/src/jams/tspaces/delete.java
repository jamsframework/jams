package jams.tspaces;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.StringTokenizer;

import com.ibm.tspaces.TupleSpace;

//parameter:
//1. jmc-file

public class delete {

	public static void main(String[] args) {
		String jmc=args[0];
		InitSpace initspace= new InitSpace(jmc);
		try{
			TupleSpace space = new TupleSpace(initspace.name,initspace.host);
			space.destroy();
		} catch (Exception e) {
	        System.out.println(e);
	    }
		

	}

}
