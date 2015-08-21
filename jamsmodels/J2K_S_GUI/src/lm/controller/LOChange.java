package lm.controller;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class LOChange {

    private MainController mainController;



    public LOChange(MainController p){
        mainController=p;
        addMainContentChangeListener();
    }

    public void addListeners(){
        mainController.getMainView().getMainMenuBar().setChange(new change());
//        mainController.getMainView().getMainChange().getMaincontentchange().addlmArableIDStepListListener(new selectIDStep());
        }

    public void addMainContentChangeListener(){
      //ID List Listener
      mainController.getMainView().getMainChange().getMaincontentchange().addLmArableIDListListener(new ListSelectionListener(){
          public void valueChanged(ListSelectionEvent listSelectionEvent){
                int index= mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition();
                if(index!=-1){
                    mainController.getMainView().getMainChange().getMaincontentchange().setlmArablePanel(mainController.getMainModel().getSaveModel().getArrayLMArable(index+1),mainController.getMainModel().getSaveModel().getTIDBoxModel(),mainController.getMainModel().getSaveModel().getFIDBoxModel());
                    mainController.getMainView().getMainChange().getMaincontentchange().setlmArableHead();
                    addLMArablePanelListener();
                    addLMArableHeadListener();
                }

           }
      });
      //Add ID Listener
      mainController.getMainView().getMainChange().getMaincontentchange().addAddIDListener(new ActionListener(){
          public void actionPerformed(ActionEvent event){
              mainController.getMainModel().getSaveModel().AddLMArableID(mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition()+1);
          }
      });
      //Delete ID Listener
      mainController.getMainView().getMainChange().getMaincontentchange().addDeleteIDListener(new ActionListener(){
          public void actionPerformed(ActionEvent event){
              mainController.getMainModel().getSaveModel().DeleteAndReorderLMArableID(mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition()+1);
//              mainController.getMainView().getMainChange().getMaincontentchange().setlmArableIDList(mainController.getMainModel().getSaveModel().getLmArableIDModel());
          }
      });
      //Save Listener

      //Cancel Listener

       

    }
    public void addLMArableHeadListener(){
        //Hinzufügen des CIDEdit Listeners
        mainController.getMainView().getMainChange().getMaincontentchange().getLmArableHead().addChangeIDActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                int SearchRow=mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition()+1;
                mainController.getMainView().getMainChange().showcropPanel();
            }
        });


    }

    public void addLMArablePanelListener(){
        //Hinzufügen der TIDEDit Listeners
        for(int i=0;i<mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getViewList().size();i++){
            mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getViewList().get(i).addTIDEditListener(new ActionListener(){
                         public void actionPerformed(ActionEvent event){
                        int SearchRow=mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition()+1;
                        int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                        mainController.getMainView().getMainChange().showtillPanel();



                        if(!mainController.getMainModel().getSaveModel().getLMArableVector(SearchRow, SearchStep).getTID().isEmpty()){
                            Object[]arr=mainController.getMainModel().getSaveModel().getTIDListModel().toArray();
                             String sA[] = Arrays.copyOf(arr, arr.length, String[].class);;
                            for( int i=0;i<sA.length;i++){
                                if(sA[i].startsWith(mainController.getMainModel().getSaveModel().getLMArableVector(SearchRow, SearchStep).getTID().getTID()+ "")){
                                    mainController.getMainView().getMainChange().getTillPanel().getTillJList().setSelectedIndex(i);
                                    break;
                                }
                            }
                        }else{
                            mainController.getMainView().getMainChange().getTillPanel().getTillJList().setSelectedIndex(0);
                        }
                }
            });

        }
        //Hinzufügen der FIDEDIT Listener
         for(int i=0;i<mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getViewList().size();i++){
            mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getViewList().get(i).addFIDEditListener(new ActionListener(){
                public void actionPerformed(ActionEvent event ){
                        int SearchRow=mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition()+1;
                        int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                        mainController.getMainView().getMainChange().showfertPanel();

                         if(!mainController.getMainModel().getSaveModel().getLMArableVector(SearchRow, SearchStep).getFID().isEmpty()){
                            Object[]arr=mainController.getMainModel().getSaveModel().getFIDListModel().toArray();
                             String sA[] = Arrays.copyOf(arr, arr.length, String[].class);
                            for( int i=0;i<sA.length;i++){
                                if(sA[i].startsWith(mainController.getMainModel().getSaveModel().getLMArableVector(SearchRow, SearchStep).getFID().getID()+ "")){
                                    mainController.getMainView().getMainChange().getFertPanel().getFertJList().setSelectedIndex(i);
                                    break;
                                }
                            }
                        }else{
                            mainController.getMainView().getMainChange().getFertPanel().getFertJList().setSelectedIndex(0);
                        }
                }
            });
        }
        //Hinzufügen der delete Listeners
        for(int i=0;i<mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getViewList().size();i++){
                mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getViewList().get(i).addDeleteListener(new ActionListener(){
                    public void actionPerformed(ActionEvent event){
                        System.out.println("Im Listener");
                         int SearchRow=mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition()+1;
                        int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                          mainController.getMainModel().getSaveModel().DeleteAndReorderLMArableStep(SearchRow,SearchStep);
                          mainController.getMainView().getMainChange().getMaincontentchange().setlmArablePanel(mainController.getMainModel().getSaveModel().getArrayLMArable(SearchRow),mainController.getMainModel().getSaveModel().getTIDBoxModel(),mainController.getMainModel().getSaveModel().getFIDBoxModel());
                            mainController.getMainView().getMainChange().getMaincontentchange().setlmArableHead();

//                        
//                        mainController.getMainView().getMainChange().getMaincontentchange().setSelectedListElement(SearchRow-1);
                        addLMArablePanelListener();
                        addLMArableHeadListener();
                    }
                });
     }
        //Hinzufügen der Add Listeners
        for(int i=0;i<mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getAddSteps().size();i++){
            mainController.getMainView().getMainChange().getMaincontentchange().getLmArablePanel().getAddSteps().get(i).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    int SearchRow=mainController.getMainView().getMainChange().getMaincontentchange().getlmArableIDListPosition()+1;
                    int SearchStep=Integer.parseInt(event.getActionCommand())+1;
                    mainController.getMainModel().getSaveModel().AddAndReorderLMArableStep(SearchRow, SearchStep);
                    mainController.getMainView().getMainChange().getMaincontentchange().setlmArablePanel(mainController.getMainModel().getSaveModel().getArrayLMArable(SearchRow),mainController.getMainModel().getSaveModel().getTIDBoxModel(),mainController.getMainModel().getSaveModel().getFIDBoxModel());
                    mainController.getMainView().getMainChange().getMaincontentchange().setlmArableHead();
                    addLMArablePanelListener();
                    addLMArableHeadListener();
                }
            });
        }

    }







    public void addTillPanelListener(){
        //Add TillPanel the DefaultListModel
        mainController.getMainView().getMainChange().getTillPanel().getTillJList().setModel(mainController.getMainModel().getSaveModel().getTIDListModel());
        //Save Button Listener
         mainController.getMainView().getMainChange().getTillPanel().getSaveButton().addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                        String s =(String) mainController.getMainView().getMainChange().getTillPanel().getTillJList().getSelectedValue();
                            if(!s.equals("-")){
                                Integer i =Integer.parseInt(s.split("\n")[0]);
                                ArrayList a= mainController.getMainView().getMainChange().getTillPanel().getInput();
                               // a.set(0,String.valueOf(i));
                                System.out.println("Index nr.--------->" + i + "   --Input" + a);
                                mainController.getMainModel().getSaveModel().setTillVector(i, mainController.getMainView().getMainChange().getTillPanel().getInput());
                                mainController.getMainModel().getSaveModel().createTIDListModel();
                                mainController.getMainModel().getSaveModel().getTillVector(i).print();
                            }

                    }
                });
        //Cancel Button Listener
        mainController.getMainView().getMainChange().getTillPanel().getCancelButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

            }
        });
        //Add Button Listener
        mainController.getMainView().getMainChange().getTillPanel().getAddButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                mainController.getMainModel().getSaveModel().AddTillVector();
                mainController.getMainView().getMainChange().getTillPanel().getTillJList().setModel(mainController.getMainModel().getSaveModel().getTIDListModel());
                int i = mainController.getMainView().getMainChange().getTillPanel().getTillJList().getModel().getSize()-1;
                mainController.getMainView().getMainChange().getTillPanel().getTillJList().setSelectedIndex(i);
            }
        });
        //Delete Button Listener
        mainController.getMainView().getMainChange().getTillPanel().getDeleteButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String s =(String) mainController.getMainView().getMainChange().getTillPanel().getTillJList().getSelectedValue();
                if(!s.equals("-")){
                    Integer Key =Integer.parseInt(s.split("\n")[0]);
                    int index=mainController.getMainView().getMainChange().getTillPanel().getTillJList().getSelectedIndex();
                    mainController.getMainModel().getSaveModel().DeleteTillVector(Key, index);
                    mainController.getMainView().getMainChange().getTillPanel().getTillJList().setModel(mainController.getMainModel().getSaveModel().getTIDListModel());
                }
                mainController.getMainView().getMainChange().getTillPanel().getTillJList().setSelectedIndex(0);
            }
        });
        //JList Listener
        mainController.getMainView().getMainChange().getTillPanel().getTillJList().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event){
                String s =(String) mainController.getMainView().getMainChange().getTillPanel().getTillJList().getSelectedValue();
                if(s!=null){
                    if(s.equals("-")){
                        mainController.getMainView().getMainChange().getTillPanel().setInput(null);
                    }else{
                        int i= 0;
                        String number="";
                        while(s.charAt(i)!=('\n')){
                            number=number + s.charAt(i);
                            i++;
                        }
                    int element = Integer.parseInt(number);
                    mainController.getMainView().getMainChange().getTillPanel().setInput(mainController.getMainModel().getSaveModel().getTillVector(element));
                    }
                }
            }
        });

    }
    public void addFertPanelListener(){
        //Add TillPanel the DefaultListModel
        mainController.getMainView().getMainChange().getFertPanel().getFertJList().setModel(mainController.getMainModel().getSaveModel().getFIDListModel());
        //Save Button Listener
         mainController.getMainView().getMainChange().getFertPanel().getSaveButton().addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                        String s =(String) mainController.getMainView().getMainChange().getFertPanel().getFertJList().getSelectedValue();
                            if(!s.equals("-")){
                                Integer i =Integer.parseInt(s.split("\n")[0]);
                                ArrayList a= mainController.getMainView().getMainChange().getFertPanel().getInput();
                               // a.set(0,String.valueOf(i));
                                System.out.println("Index nr.--------->" + i + "   --Input" + a);
                                mainController.getMainModel().getSaveModel().setFertVector(i, mainController.getMainView().getMainChange().getFertPanel().getInput());
                                mainController.getMainModel().getSaveModel().createFIDListModel();
                            }

                    }
                });
        //Cancel Button Listener
        mainController.getMainView().getMainChange().getFertPanel().getCancelButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

            }
        });
        //Add Button Listener
        mainController.getMainView().getMainChange().getFertPanel().getAddButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                mainController.getMainModel().getSaveModel().AddFertVector();
                mainController.getMainView().getMainChange().getFertPanel().getFertJList().setModel(mainController.getMainModel().getSaveModel().getFIDListModel());
                int i = mainController.getMainView().getMainChange().getFertPanel().getFertJList().getModel().getSize()-1;
                mainController.getMainView().getMainChange().getFertPanel().getFertJList().setSelectedIndex(i);
            }
        });
        //Delete Button Listener
        mainController.getMainView().getMainChange().getFertPanel().getDeleteButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String s =(String) mainController.getMainView().getMainChange().getFertPanel().getFertJList().getSelectedValue();
                if(!s.equals("-")){
                    Integer Key =Integer.parseInt(s.split("\t")[0]);
                    int index=mainController.getMainView().getMainChange().getFertPanel().getFertJList().getSelectedIndex();
                    mainController.getMainModel().getSaveModel().DeleteFertVector(Key, index);
                    mainController.getMainView().getMainChange().getFertPanel().getFertJList().setModel(mainController.getMainModel().getSaveModel().getFIDListModel());
                }
                mainController.getMainView().getMainChange().getFertPanel().getFertJList().setSelectedIndex(0);
            }
        });
        //JList Listener
        mainController.getMainView().getMainChange().getFertPanel().getFertJList().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event){
                String s =(String) mainController.getMainView().getMainChange().getFertPanel().getFertJList().getSelectedValue();
                if(s!=null){
                    if(s.equals("-")){
                        mainController.getMainView().getMainChange().getFertPanel().setInput(null);
                    }else{
                        int i= 0;
                        String number="";
                        while(s.charAt(i)!=('\t')){
                            number=number + s.charAt(i);
                            i++;
                        }
                    int element = Integer.parseInt(number);
                    mainController.getMainView().getMainChange().getFertPanel().setInput(mainController.getMainModel().getSaveModel().getFertVector(element));
                    }
                }
            }
        });

    }
    public void addCropPanelListener(){
        mainController.getMainView().getMainChange().getCropPanel().getCropJList().setModel(mainController.getMainModel().getSaveModel().getCIDListModel());


        //Save Button Listener
         mainController.getMainView().getMainChange().getCropPanel().getSaveButton().addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event){
                        String s =(String) mainController.getMainView().getMainChange().getCropPanel().getCropJList().getSelectedValue();
                            if(!s.equals("-")){
                                Integer i =Integer.parseInt(s.split("\n")[0]);
                                ArrayList a= mainController.getMainView().getMainChange().getCropPanel().getInput();
                               // a.set(0,String.valueOf(i));
                                System.out.println("Index nr.--------->" + i + "   --Input" + a);
                                mainController.getMainModel().getSaveModel().setCropVector(i, mainController.getMainView().getMainChange().getCropPanel().getInput());
                                mainController.getMainModel().getSaveModel().createCIDListModel();
                            }

                    }
                });
        //Cancel Button Listener
        mainController.getMainView().getMainChange().getCropPanel().getCancelButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

            }
        });
        //Add Button Listener
        mainController.getMainView().getMainChange().getCropPanel().getAddButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                mainController.getMainModel().getSaveModel().AddCropVector();
                mainController.getMainView().getMainChange().getCropPanel().getCropJList().setModel(mainController.getMainModel().getSaveModel().getCIDListModel());
                int i = mainController.getMainView().getMainChange().getCropPanel().getCropJList().getModel().getSize()-1;
                mainController.getMainView().getMainChange().getCropPanel().getCropJList().setSelectedIndex(i);
            }
        });
        //Delete Button Listener
        mainController.getMainView().getMainChange().getCropPanel().getDeleteButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String s =(String) mainController.getMainView().getMainChange().getCropPanel().getCropJList().getSelectedValue();
                if(!s.equals("-")){
                    Integer Key =Integer.parseInt(s.split("\t")[0]);
                    int index=mainController.getMainView().getMainChange().getCropPanel().getCropJList().getSelectedIndex();
                    mainController.getMainModel().getSaveModel().DeleteCropVector(Key, index);
                    mainController.getMainView().getMainChange().getCropPanel().getCropJList().setModel(mainController.getMainModel().getSaveModel().getCIDListModel());
                }
                mainController.getMainView().getMainChange().getCropPanel().getCropJList().setSelectedIndex(0);
            }
        });
        //JList Listener
        mainController.getMainView().getMainChange().getCropPanel().getCropJList().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event){
                String s =(String) mainController.getMainView().getMainChange().getCropPanel().getCropJList().getSelectedValue();
                if(s!=null){
                    if(s.equals("-")){
                        mainController.getMainView().getMainChange().getCropPanel().setInput(null);
                    }else{
                        int i= 0;
                        String number="";
                        while(s.charAt(i)!=('\t')){
                            number=number + s.charAt(i);
                            i++;
                        }
                    int element = Integer.parseInt(number);
                    mainController.getMainView().getMainChange().getCropPanel().setInput(mainController.getMainModel().getSaveModel().getCropVector(element));
                    }
                }
            }
        });
    }

   












































    class change implements ActionListener{
        public void actionPerformed(ActionEvent e){
            System.out.println("im Listener");
           if(!mainController.getMainModel().getMultiReader().isEmpty()){
               mainController.getMainView().MainChange();
               mainController.getMainModel().createSaveModel();
               mainController.getMainModel().getSaveModel().setLists();
               //setzen des ersten ListModel
               mainController.getMainView().getMainChange().getMaincontentchange().setlmArableIDList(mainController.getMainModel().getSaveModel().getLmArableIDModel());
               addTillPanelListener();
               addFertPanelListener();
               addCropPanelListener();

           }

            
        }
    }

}
