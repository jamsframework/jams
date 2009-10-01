package jams.tspaces;

/*
Aufrufparameter:

1. Möglichkeit:
1. Aufrufparameter: Angabe des xml-File

2. Möglichkeit:
1. Aufrufparameter: Angabe des xml-File
2. Aufrufparameter: Angabe der Datei, in die gespeichert werden soll 

3. Möglichkeit:
ohne Aufrufparameter, xml-File muss im Programm ausgewählt werden
*/

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpace;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import java.net.URL;
import java.util.StringTokenizer;

public class Tools extends JPanel{
	
    protected JTextArea textAreaStatus, textAreaInfo, textAreaSpace;
    protected JTable table;
    protected JComponent scrollStatus,scrollInfo,scrollTable,scrollSpace,scrollTree;
    protected DrawPanel drawPanel;
    protected JTabbedPane tabbedPane;
    protected Action openAction, deleteAction, saveAction, exitAction, infoAction, resultAction, spaceAction, restartAction, treeAction;
    protected JCheckBoxMenuItem[] cbmi;
    protected String jmc;
    protected InitSpace initspace;
    protected Boolean loaded=false;
    protected String saveFile;
    protected Boolean  saveFileloaded=false; 
    
    
    protected ImageIcon openicon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/openicon.png"));
    protected ImageIcon saveicon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/saveicon.png"));
    protected ImageIcon deleteicon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/deleteicon.png"));
    protected ImageIcon exiticon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/exiticon.png"));
    protected ImageIcon infoicon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/infoicon.png"));
    protected ImageIcon spaceicon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/spaceicon.png"));
    protected ImageIcon resulticon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/resulticon.png"));
    protected ImageIcon restarticon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/restarticon.png"));
    protected ImageIcon treeicon=new ImageIcon(ClassLoader.getSystemResource("jams/tspaces/treeicon.png"));
    
    /*
    protected String imageverzeichnis="C:\\Dokumente und Einstellungen\\kerstin\\Eigene Dateien\\arbeit\\eclipse2\\Module\\";
    protected ImageIcon openicon=new ImageIcon(imageverzeichnis+"openicon.png");
    protected ImageIcon deleteicon=new ImageIcon(imageverzeichnis+"deleteicon.png");
    protected ImageIcon saveicon=new ImageIcon(imageverzeichnis+"saveicon.png");
    protected ImageIcon exiticon=new ImageIcon(imageverzeichnis+"exiticon.png");
    protected ImageIcon infoicon=new ImageIcon(imageverzeichnis+"infoicon.png");
    protected ImageIcon resulticon=new ImageIcon(imageverzeichnis+"resulticon.png");
    protected ImageIcon spaceicon=new ImageIcon(imageverzeichnis+"spaceicon.png");
    protected ImageIcon restarticon=new ImageIcon(imageverzeichnis+"restarticon.png");
    protected ImageIcon treeicon=new ImageIcon(imageverzeichnis+"treeicon.png");
    */

    public Tools(String args[]) {
        super(new BorderLayout());        
        
        tabbedPane = new JTabbedPane();
        textAreaStatus=new JTextArea();
        textAreaStatus.setEditable(false);
        JScrollPane scrollPaneStatus = new JScrollPane(textAreaStatus);                               
        scrollStatus = scrollPaneStatus;
        tabbedPane.addTab("Satus",new ImageIcon(), scrollStatus,"zeigt alle Statusinformationen an");
        add(tabbedPane);

        if (args.length>=1){
        	loaded=true;
        	jmc=args[0];
        	initspace=new InitSpace(jmc);
        	if (args.length>=2){
        		saveFileloaded=true;
        		saveFile=args[1];
        	}
        }
        
        openAction = new OpenAction(  "jmc öffnen",openicon,"Öffnen einer jmc.Datei.",new Integer(KeyEvent.VK_O));
        deleteAction = new DeleteAction("Space löschen",deleteicon,"Löschen des Space", new Integer(KeyEvent.VK_D));
        saveAction =  new SaveAction( "Space speichern",saveicon,"Speichern des Space", new Integer(KeyEvent.VK_S));
        exitAction =  new ExitAction( "Beenden",exiticon,"Beenden des Programmes", new Integer(KeyEvent.VK_X));       
        infoAction =  new InfoAction( "Info",infoicon,"Info über Space",new Integer(KeyEvent.VK_I));
        resultAction =  new ResultAction( "Results",resulticon,"Ergebnisse im Space",new Integer(KeyEvent.VK_R));            
        spaceAction =  new SpaceAction( "Spaceinhalt",spaceicon,"Gesamter Space",new Integer(KeyEvent.VK_A));
        restartAction =  new RestartAction( "Spaceneustart",restarticon,"Startet den Space neu",new Integer(KeyEvent.VK_N));
        treeAction =  new TreeAction( "Tree",treeicon,"Malt den Baum",new Integer(KeyEvent.VK_T));
        
        deleteAction.setEnabled(loaded);        
        saveAction.setEnabled(loaded);
        infoAction.setEnabled(loaded);
        resultAction.setEnabled(loaded);
        spaceAction.setEnabled(loaded);
        restartAction.setEnabled(loaded);
        treeAction.setEnabled(loaded);
    }

    public JMenuBar createMenuBar() {
        JMenuItem menuItem = null;
        JMenuBar menuBar;

        menuBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Menu");

        Action[] actions = {openAction, exitAction};
        for (int i = 0; i < actions.length; i++) {
            menuItem = new JMenuItem(actions[i]);
            menuItem.setIcon(null);
            mainMenu.add(menuItem);
        }
        menuBar.add(mainMenu);
        
        JMenu spaceMenu = new JMenu("Space");

        Action[] spaceactions = {deleteAction, saveAction,restartAction};
        for (int i = 0; i < spaceactions.length; i++) {
            menuItem = new JMenuItem(spaceactions[i]);
            menuItem.setIcon(null);
            spaceMenu.add(menuItem);
        }
        menuBar.add(spaceMenu);
        
        JMenu infoMenu = new JMenu("Info");

        Action[] infoactions = {resultAction, infoAction, spaceAction};
        for (int i = 0; i < infoactions.length; i++) {
            menuItem = new JMenuItem(infoactions[i]);
            menuItem.setIcon(null);
            infoMenu.add(menuItem);
        }
        menuBar.add(infoMenu);
        
        
        JMenu treeMenu = new JMenu("Grafik");
        Action[] treeactions = {treeAction};
        for (int i = 0; i < treeactions.length; i++) {
            menuItem = new JMenuItem(treeactions[i]);
            menuItem.setIcon(null);
            treeMenu.add(menuItem);
        }
        menuBar.add(treeMenu);
        
        return menuBar;
    }

    public void createToolBar() {
        JButton button = null;

        //Create the toolbar.
        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.PAGE_START);

        //open button
        button = new JButton(openAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);

        //delete button
        button = new JButton(deleteAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);

        //save button
        button = new JButton(saveAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);
        
        //restart button
        button = new JButton(restartAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);
                
        //space button
        button = new JButton(resultAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);
        
        //info button
        button = new JButton(infoAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);    
        
        button = new JButton(spaceAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);  

        //tree button
        button = new JButton(treeAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);  
        
        //exit button
        button = new JButton(exitAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);       
    }

    public class OpenAction extends AbstractAction {
        public OpenAction(String text, ImageIcon icon,
                          String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
        	JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setFileFilter(new FileFilter() {
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".jmc") || f.isDirectory();
             }
             public String getDescription() {
                 return "JMC-File";
             }
	        });
	        if ( fileChooser.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION ) {
	            jmc=fileChooser.getSelectedFile().toString();
		    	loaded=true;		    	
		    	saveFileloaded=false;
		    	String s=("Datei "+jmc+" geöffnet."+"\n\n");		   
		    	textAreaStatus.append(s);
		    			    	
		    	InitSpace initspace= new InitSpace(jmc);		    
		    	s=("Spacename: "+initspace.name+"\n"+"Host: "+initspace.host+"\n"+"WorkSpace: "+initspace.workspace+"\n"+"hruFileName: "+initspace.hruFile+"\n"+"reachFileName: "+initspace.reachFile+"\n\n");		   		    				 
		    	textAreaStatus.append(s);
		    	tabbedPane.setSelectedComponent(scrollStatus);
		    	tabbedPane.remove(scrollInfo);
		    	tabbedPane.remove(scrollTable);	
		    	tabbedPane.remove(scrollSpace);
		    	tabbedPane.remove(scrollTree);
		    	try{
	        	  if (TupleSpace.exists(initspace.name, initspace.host)){
		       	    textAreaStatus.append("Der Space existiert. \n\n");			       	        
	        	  }
	        	  else{		  		    	
	  	       		textAreaStatus.append("Der Space("+initspace.name+", "+ initspace.host+") existiert nicht. \n\n");		  	   			
	         	  }
	  		    	saveAction.setEnabled(loaded);
			    	deleteAction.setEnabled(loaded);
			    	infoAction.setEnabled(loaded);
			    	resultAction.setEnabled(loaded);
			    	spaceAction.setEnabled(loaded);
			    	restartAction.setEnabled(loaded);
			    	treeAction.setEnabled(loaded);
		       		} catch (Exception exc) {
		       	        textAreaStatus.append("Die Spaceumgebung wurde nicht gestartet. \n\n");		                
		       		}          		                      
	        }
	      } 
        }

    public class DeleteAction extends AbstractAction {
        public DeleteAction(String text, ImageIcon icon,
                            String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
    		InitSpace initspace= new InitSpace(jmc);
    		try{
    			TupleSpace space = new TupleSpace(initspace.name,initspace.host);
    			space.destroy();
    			String s=("Der Space wurde gelöscht."+"\n\n");
		    	textAreaStatus.append(s);
		    	tabbedPane.setSelectedComponent(scrollStatus);
		    	tabbedPane.remove(scrollInfo);
		    	tabbedPane.remove(scrollTable);
		    	tabbedPane.remove(scrollSpace);
		    	tabbedPane.remove(scrollTree);
    		} catch (Exception exc) {
    	        textAreaStatus.append("Der Space existiert nicht. \n\n");
    	        tabbedPane.setSelectedComponent(scrollStatus);
    		}
        }
    }

    public class SaveAction extends AbstractAction {
        public SaveAction(String text, ImageIcon icon,
                           String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
    		InitSpace initspace= new InitSpace(jmc);
    		try{	
    		  if (TupleSpace.exists(initspace.name, initspace.host)){
    			TupleSpace space = new TupleSpace(initspace.name,initspace.host);    			
    			Tuple template=new Tuple(new Field(new JAMSString("fileName")), new Field(JAMSString.class));
    			String outputfile="";
    			if (saveFileloaded){
    				outputfile=saveFile;
    			}
    			else if (space.countN(template)>0){
    				Tuple file = space.read(template);
    				outputfile=((JAMSString) file.getField(1).getValue()).getValue();
    			} else{
    				JFileChooser fileChooser = new JFileChooser();
    	        	fileChooser.setDialogTitle("Speichern");
    	        	fileChooser.setApproveButtonText("Speichern");
    		        fileChooser.setFileFilter(new FileFilter() {
    	             public boolean accept(File f) {
    	                 return f.getName().toLowerCase().endsWith(".par") || f.isDirectory();
    	             }
    	             public String getDescription() {
    	                 return "PAR-File";
    	             }
    		        });				
    		        
    		        if ( fileChooser.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION ) {
    		            outputfile=fileChooser.getSelectedFile().toString();
    		            saveFileloaded=true;
    		        }
    		        
    			}			
    			Writer fw = new FileWriter(outputfile);
    			StringBuffer info = new StringBuffer("");
    			
    			template=new Tuple(new Field(new JAMSString("requestedJVM")), new Field(JAMSInteger.class));
    			info.append("requestedJVM: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue()+"\t");
    			template=new Tuple(new Field(new JAMSString("assignedJVM")), new Field(JAMSInteger.class));
    			info.append("assignedJVM: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue()+"\t");			
    			template=new Tuple(new Field(new JAMSString("sampleCount")), new Field(JAMSInteger.class));
    			info.append("sampleCount: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue()+"\t");			
    			template=new Tuple(new Field(new JAMSString("currentCount")), new Field(JAMSInteger.class));
    			info.append("currentCount: " + ((JAMSInteger)space.read(template).getField(1).getValue()).getValue());
    			fw.write(info.toString()+"\n");
    			
    			template=new Tuple(new Field(new JAMSString("header")), new Field(JAMSString.class));
    			String header=((JAMSString)space.read(template).getField(1).getValue()).getValue();
    			fw.write(header.replaceAll(" ", "\t")+"\n");
    			
    			template = new Tuple(new Field(new JAMSString("result")), new Field(JAMSString.class));
    			Tuple scan = space.scan(template);
    			int size=scan.numberOfFields();
    		    for (int i=0; i<size; i++){
    		    	String result=((JAMSString) (((Tuple) scan.getField(i).getValue()).getField(1).getValue())).toString();
    		    	fw.write(result.replaceAll(" ","\t")+"\n");    	
    		    }			
    			fw.close();    			
    			textAreaStatus.append("Der Space wurde in die Datei "+outputfile+" geschrieben."+"\n\n");
    			tabbedPane.setSelectedComponent(scrollStatus);
    		  }
    		  else{
  	       		textAreaStatus.append("Der Space("+initspace.name+", "+ initspace.host+") existiert nicht. \n\n");
  	   			tabbedPane.setSelectedComponent(scrollStatus);
    		  }
    	    } catch ( IOException exc ) { 
    	    	  textAreaStatus.append( "Konnte Datei nicht erstellen. \n\n" );
    			  tabbedPane.setSelectedComponent(scrollStatus);
    		} catch (Exception exc) {
	       		textAreaStatus.append("Im Space("+initspace.name+", "+ initspace.host+") fehlen Daten. \n\n");
	   			tabbedPane.setSelectedComponent(scrollStatus);
    		}
        }
    }
    
    public class RestartAction extends AbstractAction {
        public RestartAction(String text, ImageIcon icon,
                            String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
    		InitSpace initspace= new InitSpace(jmc);
    		try{
    			JAMSString modelerKey = new JAMSString(initspace.name);
    	        JAMSString tSpaceIP = new JAMSString(initspace.host);
    	        
    	        SpaceTools spaceTools = new SpaceTools(modelerKey,tSpaceIP);
    	        spaceTools.deleteSpace();
    	        // set implizit spaceTools.monitor key
    	        spaceTools.setMonitor();
    	        for (int i=1;i<spaceTools.validMonitors.length;i++){
    	            spaceTools.setMonitor(spaceTools.validMonitors[i]);    	          
    	        }    			
    			String s=("Der Space wurde neu gestartet."+"\n\n");
		    	textAreaStatus.append(s);
		    	tabbedPane.setSelectedComponent(scrollStatus);
		    	tabbedPane.remove(scrollInfo);
		    	tabbedPane.remove(scrollTable);
		    	tabbedPane.remove(scrollSpace);
		    	tabbedPane.remove(scrollTree);
    		} catch (Exception exc) {
    	        textAreaStatus.append(exc+"Die Spaceumgebung wurde nicht gestartet. \n\n");
    	        tabbedPane.setSelectedComponent(scrollStatus);
    		}
        }
    } 
    
    public class ExitAction extends AbstractAction {
        public ExitAction(String text, ImageIcon icon,
                           String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
        	System.exit( 0 ); 
        }
    }
    
    public class ResultAction extends AbstractAction {
        public ResultAction(String text, ImageIcon icon,
                           String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
        	 InitSpace initspace= new InitSpace(jmc); 
        	 try{
        	  if (TupleSpace.exists(initspace.name, initspace.host)){
     			TupleSpace space = new TupleSpace(initspace.name,initspace.host);			
     			Tuple template=new Tuple(new Field(new JAMSString("header")), new Field(JAMSString.class));     		     	
     			String 	header=((JAMSString)space.read(template).getField(1).getValue()).getValue();     			
     			StringTokenizer headertokenizer = new StringTokenizer(header);
     			int anzspalten=headertokenizer.countTokens();
     			String[] columnNames=new String[anzspalten];     			
     			for(int i=0;i<anzspalten;i++){
     				columnNames[i]=headertokenizer.nextToken();
     			}

     			template = new Tuple(new Field(new JAMSString("result")), new Field(JAMSString.class));
     			Tuple scan = space.scan(template);
     			int size=scan.numberOfFields();	
     			Object[][] data=new Object[size][anzspalten];
     			for (int i=0; i<size; i++){
     		    	String result=((JAMSString) (((Tuple) scan.getField(i).getValue()).getField(1).getValue())).toString();
     				StringTokenizer restokenizer = new StringTokenizer(result);
     				for(int j=0;j<anzspalten;j++){
     					data[i][j]=restokenizer.nextToken();
     				}    	
     		    }     			
     			for (int i=0; i< data.length; i++){
     				int min=i;
     				for (int j=i; j<data.length; j++){
     					if (Integer.parseInt((String)data[j][0])<Integer.parseInt((String)data[min][0])){
     						min=j;
     					}
     				}
     				if(i!=min){
     					Object[] h=data[min];
     					data[min]=data[i];
     					data[i]=h;
     				}			
     			}     	    
     			tabbedPane.remove(scrollTable);
     			table = new JTable(data,columnNames);     			
     	        JScrollPane scrollPaneTable = new JScrollPane(table);       
     	        scrollTable=scrollPaneTable;       
     	        tabbedPane.addTab("Ergebnisse",resulticon, scrollTable,"zeigt alle Resultate an");
     	        tabbedPane.setSelectedComponent(scrollTable); 
        	  }
       	      else{
	       		textAreaStatus.append("Der Space("+initspace.name+", "+ initspace.host+") existiert nicht. \n\n");
	   			tabbedPane.setSelectedComponent(scrollStatus);
       	      }
     		} catch (Exception exc) {
     	        textAreaStatus.append("Im Space("+initspace.name+", "+ initspace.host+") befinden sich kein Header oder keine fertigen Ergebnisse. \n\n");
                tabbedPane.setSelectedComponent(scrollStatus);
     		}          		                      
        }
    }
    
    public class InfoAction extends AbstractAction {
    	
        public InfoAction(String text, ImageIcon icon,
                           String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);          
        }
        public void actionPerformed(ActionEvent e) {
        	 tabbedPane.remove(scrollInfo);
             textAreaInfo=new JTextArea();
             textAreaInfo.setEditable(false);
             JScrollPane scrollPaneInfo = new JScrollPane(textAreaInfo);                            
             scrollInfo=scrollPaneInfo;                            
             InitSpace initspace= new InitSpace(jmc);
        	 try{
        	   if (TupleSpace.exists(initspace.name, initspace.host)){
        		   tabbedPane.addTab("Info",infoicon, scrollInfo,"zeigt alle Informationen über den Space an");
                   tabbedPane.setSelectedComponent(scrollInfo);
                   TupleSpace space = new TupleSpace(initspace.name,initspace.host);
	     			
	     			Tuple template=new Tuple(new Field(new JAMSString("requestedJVM")), new Field(JAMSInteger.class));
	     			Integer requestedJVM=null;
	     			if (space.countN(template)>0){
		     			requestedJVM=((JAMSInteger)space.read(template).getField(1).getValue()).getValue();
	     			}
	     			textAreaInfo.append("RequestedJVM: "+requestedJVM+"\n");
           	    
	     			template=new Tuple(new Field(new JAMSString("assignedJVM")), new Field(JAMSInteger.class));
	     			Integer assignedJVM=null;
	     			if (space.countN(template)>0){
	     				assignedJVM=((JAMSInteger)space.read(template).getField(1).getValue()).getValue();
	     			}
	     			textAreaInfo.append("AssignedJVM: "+assignedJVM+"\n");
        	    
	           	    template=new Tuple(new Field(new JAMSString("sampleCount")), new Field(JAMSInteger.class));
	           	    Integer sampleCount=null;
	           	    if (space.countN(template)>0){
	           	    	sampleCount=((JAMSInteger)space.read(template).getField(1).getValue()).getValue();
	           	    }
	           	    textAreaInfo.append("SampleCount: "+sampleCount+"\n");
	        	    
	           	    template=new Tuple(new Field(new JAMSString("currentCount")), new Field(JAMSInteger.class));
	           	    Integer currentCount=null;
	           	    if (space.countN(template)>0){
	           	    	currentCount=((JAMSInteger)space.read(template).getField(1).getValue()).getValue();	
	           	    }           	    
	            	textAreaInfo.append("CurrentCount: "+currentCount+"\n");
	        	    
	           	    template=new Tuple(new Field(new JAMSString("fileName")), new Field(JAMSString.class));
	           	    String fileName=null;
	           	    if (space.countN(template)>0){
	           	    	fileName=((JAMSString)space.read(template).getField(1).getValue()).getValue();
	           	    }
	           	    textAreaInfo.append("fileName: "+fileName+"\n");
    	        
	           	    String monitorPrefix = new String("***MONITOR***");

	           	    String[] validMonitors={ 
      	    		  new String("GENERAL***MONITOR***"),
      	    	        new String("state"),
      	    	        new String("requestedJVM"),
      	    	        new String("assignedJVM"),
      	    	        new String("currentCount"),
      	    	        new String("sampleCount"),
      	    	        new String("resultsWritten"),
      	    	        new String("RandomParaSampler.updateValues"),
      	    	        new String("init"),
      	    	        new String("run"),
      	    	        new String("cleanup"),
      	    	        new String("RandomParaSampler.init"),
      	    	        new String("RandomParaSampler.run"),
      	    	        new String("RandomParaSampler.run.hasNext"),
      	    	        new String("RandomParaSampler.cleanup"),
      	    	        new String("JAMSTSpaces.init"),
      	    	        new String("JAMSTSpaces.run"),
      	    	        new String("JAMSTSpaces.cleanup")};
	      	      
	      	      for (int i=0; i<validMonitors.length; i++){
	      	    	  String vorhanden="nicht vorhanden";
	      	    	  if (space.countN(new Tuple(new Field(new JAMSString(monitorPrefix+validMonitors[i]))))>0){
	      	    		  vorhanden="vorhanden";
	      	    	  }
	      	    	 textAreaInfo.append(validMonitors[i]+": "+vorhanden+"\n");
	      	      }  
        	   }        	           	           
        	   else{
        		textAreaStatus.append("Der Space("+initspace.name+", "+ initspace.host+") existiert nicht. \n\n");
    			tabbedPane.setSelectedComponent(scrollStatus);
        	   }
     		} catch (Exception exc) {     			
     			textAreaStatus.append("Der Space("+initspace.name+", "+ initspace.host+") existiert nicht. \n\n");
     			tabbedPane.setSelectedComponent(scrollStatus);
    		}  
     		//frame.setVisible(true);        
        }
    }
 
    public class SpaceAction extends AbstractAction {
    	
        public SpaceAction(String text, ImageIcon icon,
                           String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);          
        }
        public void actionPerformed(ActionEvent e) {
        	 tabbedPane.remove(scrollSpace);
             textAreaSpace=new JTextArea();
             textAreaSpace.setEditable(false);
             JScrollPane scrollPaneSpace = new JScrollPane(textAreaSpace);                            
             scrollSpace=scrollPaneSpace;                           
             InitSpace initspace= new InitSpace(jmc);            
        	 try{
        	   Tuple[] tuple={new Tuple(new Field(Object.class)),
        			          new Tuple(new Field(Object.class),new Field(Object.class)),
        			          new Tuple(new Field(Object.class),new Field(Object.class),new Field(Object.class)),
        			          new Tuple(new Field(Object.class),new Field(Object.class),new Field(Object.class),new Field(Object.class)),
        			          new Tuple(new Field(Object.class),new Field(Object.class),new Field(Object.class),new Field(Object.class),new Field(Object.class)),
        			          new Tuple(new Field(Object.class),new Field(Object.class),new Field(Object.class),new Field(Object.class),new Field(Object.class),new Field(Object.class))};
        	   if (TupleSpace.exists(initspace.name, initspace.host)){
        		   	tabbedPane.addTab("Space",spaceicon, scrollSpace,"zeigt den gesamten Space an");
                   	tabbedPane.setSelectedComponent(scrollSpace);
	     			TupleSpace space = new TupleSpace(initspace.name,initspace.host);
	     			for(int j=0; j<tuple.length; j++){
		     	     	Tuple template=tuple[j];
		     	     	Tuple scan = space.scan(template);
		     	     	for(int i=0; i<scan.numberOfFields(); i++){
		     	     		textAreaSpace.append(((Tuple) scan.getField(i).getValue()).toString()+"\n");
		     	     	}
     			}	     	     	     	     	     		    
        	   }        	           	           
        	   else{
        		textAreaStatus.append("Der Space("+initspace.name+", "+ initspace.host+") existiert nicht. \n\n");
    			tabbedPane.setSelectedComponent(scrollStatus);
    			}
     		} catch (Exception exc) {     			
     			textAreaStatus.append("Der Space("+initspace.name+", "+ initspace.host+") existiert nicht. \n\n");
     			tabbedPane.setSelectedComponent(scrollStatus);
    		}  
     		//frame.setVisible(true);        
        }
    }
    
 public class TreeAction extends AbstractAction {
    	
        public TreeAction(String text, ImageIcon icon,
                           String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);          
        }
        public void actionPerformed(ActionEvent e) {
        	tabbedPane.remove(scrollTree);
        	
            //String hrudatei="C:\\Dokumente und Einstellungen\\kerstin\\Eigene Dateien\\arbeit\\eclipse\\Baumpartitionierung\\hrus_new.par";
            //String reachdatei="C:\\Dokumente und Einstellungen\\kerstin\\Eigene Dateien\\arbeit\\eclipse\\Baumpartitionierung\\reach.par";
        	InitSpace initspace=new InitSpace(jmc);
        	String hrudatei=initspace.workspace+initspace.hruFile;
        	String reachdatei=initspace.workspace+initspace.reachFile;
        	int maxsize=5;
            
        	EntityReader er=new EntityReader(initspace.workspace,initspace.hruFile,initspace.reachFile);
        	try{
    			er.init();
    		}catch(Exception exc){System.out.println(exc);}
        	BaumpartEntity bp=new BaumpartEntity(er.hrus,er.reaches,5);
        	drawPanel=new DrawPanel(bp.wurzel,bp.partitioningtree);
        	drawPanel.setPreferredSize(new Dimension(drawPanel.maxx+50,drawPanel.maxy+50));
        	drawPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));	    
    	    scrollTree=new JScrollPane(drawPanel);
    	    scrollTree.setPreferredSize(new Dimension(800, 600));
		   	tabbedPane.addTab("Tree",treeicon, scrollTree,"zeigt den Baum an");
           	tabbedPane.setSelectedComponent(scrollTree);
        }
    }
   
    private static void createAndShowGUI(String[] args) {
        //Create and set up the window.
        JFrame frame = new JFrame("Space-Tools");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        //Create/set menu bar and content pane.
        Tools demo = new Tools(args);
        frame.setJMenuBar(demo.createMenuBar());
        demo.createToolBar();
        demo.setOpaque(true); //content panes must be opaque
        frame.setContentPane(demo);

        //Display the window.
        frame.setVisible(true);
    }

    public static void main(String[] args) {      
    	createAndShowGUI(args);
    }
}