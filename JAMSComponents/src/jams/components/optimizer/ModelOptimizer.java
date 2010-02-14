/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.JAMSProperties;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import jams.SystemProperties;
import jams.data.*;
import jams.model.*;
import jams.runtime.StandardRuntime;
import jams.tools.XMLTools;
import org.w3c.dom.*;

/**
 *
 * @author Christian Fischer
 */
public class ModelOptimizer extends JAMSComponent{
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Description"
            )
            public JAMSDocument model;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString propertyFile;
   
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "efficiency methods"
            )
            public JAMSString effMethodName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "efficiency methods"
            )
            public JAMSString effAttributeName;
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSBoolean reduceHRUs;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSString HRUAttribute;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSInteger HRUReductionMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public String optimizationModel;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSString timeContext;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSString timeIntervalName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSTimeInterval initialisationTimeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSTimeInterval calibrationTimeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSBoolean doInitialisation;
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSBoolean optimizeModelStructure;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSInteger optimizationMethod;
                    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
    
   
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble effValue;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSInteger mode;
           
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSString optimizerResultFile;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maxn"
            )
            public JAMSInteger maxn;
    
     @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSString resultingModelFile;
     

    class AttributeDesc{
        String ContextName;
        String AttrName;
        ArrayList<String> compNames;
    }
                        
    boolean OperatesOnAttribute(Node context,String attribute){
        NodeList childs = context.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("component") || node.getNodeName().equals("contextcomponent")){
                if (OperatesOnAttribute(node,attribute))
                    return true;
            }
            if (node.getNodeName().equals("var")){
                String value = ((Element)node).getAttribute("attribute");
                if (value != null){
                    if (value.equals(attribute))
                        return true;
                }
            }
        }
        return false;
    }

    void AddHRUReducerContext(Node root,Document doc){        
        NodeList childs_tmp = root.getChildNodes();
        //copy to array
        Node childs[] = new Node[childs_tmp.getLength()];
        for (int i=0;i<childs_tmp.getLength();i++){
            childs[i] = childs_tmp.item(i);
        }
        //reducing context
        Element HRUReducingContext = doc.createElement("contextcomponent");
        HRUReducingContext.setAttribute("class", "jams.components.optimizer.HRUReducer");
        HRUReducingContext.setAttribute("name", "HRUReducer");
        
        AddAttribute(HRUReducingContext, "active", "true", null, true);
        AddAttribute(HRUReducingContext, "hrus", HRUAttribute.getValue(), null, false);
        AddAttribute(HRUReducingContext, "effValue", effAttributeName.getValue(), null, false);
        AddAttribute(HRUReducingContext, "snapshot", "HRUReducerSnapshot", null, false);
        AddAttribute(HRUReducingContext, "method", Integer.toString(this.HRUReductionMethod.getValue()), null, true);
                        
        //snapshot component
        Element ModelSnapshot = doc.createElement("component");
        ModelSnapshot.setAttribute("class", "jams.components.tools.JAMSModelSnapshot");
        ModelSnapshot.setAttribute("name", "HRUReducerSnapshot");
        
        AddAttribute(ModelSnapshot, "takeSnapshot", "true", null, true);
        AddAttribute(ModelSnapshot, "loadSnapshot", "false", null, true);
        AddAttribute(ModelSnapshot, "data", "HRUReducerSnapshot", null, false);
        AddAttribute(ModelSnapshot, "holdInMemory","true",null,true);      
        AddAttribute(ModelSnapshot, "freezeState","0",null,true);      
        /*Node newRoot = root.cloneNode(true);
        
        //clear newRoot
        ArrayList<Node> oldChilds = new ArrayList();
        for (int index = 0; index < newRoot.getChildNodes().getLength(); index++) {
            Node node = newRoot.getChildNodes().item(index);
            if (node.getNodeName().equals("component") || node.getNodeName().equals("contextcomponent")){
                oldChilds.add(node);
            }
        }
        for (int i=0;i<oldChilds.size();i++)
            newRoot.removeChild(oldChilds.get(i));*/
        
        //search for first contextcomponent which operates on hrus
        Node HRUReducerInsertingPosition = null;
        for (int index = 0; index < childs.length; index++) {
            Node node = childs[index];
            if (node.getNodeName().equals("contextcomponent")){
                if (OperatesOnAttribute(node,this.HRUAttribute.getValue())){
                    HRUReducerInsertingPosition = node;
                    break;
                }
            }
        }
            
        boolean InsertingPositionReached = false;
        for (int index = 0; index < childs.length; index++) {
            Node node = childs[index];
            
            if (!InsertingPositionReached){
                if (node.isSameNode(HRUReducerInsertingPosition)){
                    InsertingPositionReached = true;
                }                
            }
            //do not use "else" here, because InsertingPositionReached is not dynamic
            if (InsertingPositionReached){
                //rename instance names
                Node cpy_node = node.cloneNode(true);
                if (cpy_node.getNodeName().equals("component") || cpy_node.getNodeName().equals("contextcomponent") ){
                    //((Element)cpy_node).setAttribute("name","hru_reduction"+((Element)cpy_node).getAttribute("name"));
                    HRUReducingContext.appendChild(cpy_node);
                }
            }
        }
        
        AddNamePrefix(HRUReducingContext,"hru_reduction",new ArrayList<String>());
        
        root.insertBefore(ModelSnapshot, HRUReducerInsertingPosition);
        root.insertBefore(HRUReducingContext, HRUReducerInsertingPosition);       
    }
    
    Node getNamedNode(Node context, String name){
        NodeList childs = context.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("component") || node.getNodeName().equals("contextcomponent")){
                if (((Element)node).getAttribute("name").equals(name)){
                    return node;
                }
            }
            Node result = getNamedNode(node,name);
            if (result != null)
                return result;
        }
        return null;
    }
        
    void AddNamePrefix(Node owner,String prefix,ArrayList<String> renamed_contexts/*,ArrayList<Pair> renamed_components*/){
        if (owner.getNodeName().equals("contextcomponent")){
            Element elem = (Element)owner;
            renamed_contexts.add(elem.getAttribute("name"));            
        }
        if (owner.getNodeName().equals("contextcomponent") || owner.getNodeName().equals("component")){
            Element elem = (Element)owner;
            //renamed_components.add(new Pair(elem.getAttribute("name"),prefix + elem.getAttribute("name")));
            elem.setAttribute("name", prefix + elem.getAttribute("name"));
        }        
        if (owner.getNodeName().equals("var")){
            Element elem = (Element)owner;
            String context = elem.getAttribute("context");
            if (context != null){
                for (int i=0;i<renamed_contexts.size();i++){
                    if (renamed_contexts.get(i).equals(context)){
                        elem.setAttribute("context", prefix + context);
                    }
                }
                
            }
        }
        NodeList childs = owner.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            AddNamePrefix(node,prefix,renamed_contexts/*,renamed_components*/);
        }
    }
            
    void RenameAttribute(Node owner,String old_attr,String new_attr){        
        if (owner.getNodeName().equals("var")){
            Element elem = (Element)owner;
            String attr = elem.getAttribute("attribute");
            if (attr != null){
                if (attr.equals(old_attr)){
                    elem.setAttribute("attribute", new_attr);
                }
            }
            
        }
        NodeList childs = owner.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            RenameAttribute(node,old_attr,new_attr);
        }
    }
    @SuppressWarnings("unchecked")
    void SplitTimeContext(Node root,String timeContextName, String timeIntervalName, 
            String initialisationTimeInterval, String calibrationTimeInterval, Document doc,
            Element Optimizer,Element snapshot){
        //add timeIntervalAttributes
        Element TimeIntervalAttribute1 = doc.createElement("attribute");
        TimeIntervalAttribute1.setAttribute("class", "jams.data.JAMSTimeInterval");
        TimeIntervalAttribute1.setAttribute("name", "initialisationTimeInterval");
        TimeIntervalAttribute1.setAttribute("value", initialisationTimeInterval.toString());
        
        Element TimeIntervalAttribute2 = doc.createElement("attribute");
        TimeIntervalAttribute2.setAttribute("class", "jams.data.JAMSTimeInterval");
        TimeIntervalAttribute2.setAttribute("name", "calibrationTimeInterval");
        TimeIntervalAttribute2.setAttribute("value", calibrationTimeInterval.toString());
        
        root.appendChild(TimeIntervalAttribute1);
        root.appendChild(TimeIntervalAttribute2);
                
        //search in root node for specified timeContext
        Node timeContext_init = getNamedNode(root,timeContextName);
                        
        //copy timeContext, rename components and contexes
        ArrayList<String> renamedContexts = new ArrayList();        
        Node timeContext_cal = timeContext_init.cloneNode(true);
        RenameAttribute(timeContext_init,timeIntervalName,"initialisationTimeInterval");
        RenameAttribute(timeContext_cal,timeIntervalName,"calibrationTimeInterval");
                        
        AddNamePrefix(timeContext_cal,"cal",renamedContexts);      
        
        Optimizer.appendChild(timeContext_cal);
        if (timeContext_init.getNextSibling() != null){            
            timeContext_init.getNextSibling().getParentNode().insertBefore(Optimizer,timeContext_init.getNextSibling() );
            timeContext_init.getNextSibling().getParentNode().insertBefore(snapshot,timeContext_init.getNextSibling() );
        }else{
            timeContext_init.getParentNode().appendChild(snapshot);
            timeContext_init.getParentNode().appendChild(Optimizer);
        }        
        
    }
    
    public boolean IsInContext(Node root, String componentName, String contextName, boolean rootInContext){
        if (root.getNodeName().equals("component")){
            Element elem =(Element)root;
            if (elem.getAttribute("name").equals(componentName))
                return rootInContext;
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("contextcomponent")){
                Element elem = (Element)node;
                if (elem.getAttribute("name").equals(contextName)){
                    if (IsInContext(node,componentName,contextName,true)){
                        return true;
                    }
                }else if (IsInContext(node,componentName,contextName,rootInContext)){
                    return true;                
                }
            }else if (IsInContext(node,componentName,contextName,rootInContext)){
                return true;                
            }
        }
        return false;
    }
    
    public void AddAttribute(Element parent,String name,String value,String context,boolean isValue){
        Element newElement = parent.getOwnerDocument().createElement("var");
        newElement.setAttribute("name", name);
        if (isValue)
            newElement.setAttribute("value", value);
        else
            newElement.setAttribute("attribute", value);
        if (context != null)
            newElement.setAttribute("context", context);
        
        parent.appendChild(newElement);        
    }
    
    class Pair{
        public String first;
        public String second;
        
        Pair(String a,String b){
            first = a;
            second = b;
        }
    }
    
    public ArrayList<Pair> SplitParameterString(String ParameterString){
        //zerlege parameterstring
        ArrayList<Pair> parameter = new ArrayList<Pair>();
        StringTokenizer tok = new StringTokenizer(ParameterString,";");
        while(tok.hasMoreTokens()){
            String param = tok.nextToken();
            String splitting[] = param.split("\\.");
            if (splitting.length == 2)
                parameter.add(new Pair(splitting[0],splitting[1]));
            else
                parameter.add(new Pair(null,param));
        }
        return parameter;
    }
    
    public String GetFirstAttributeUsingComponent(Node root,String attr){
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            String result = GetFirstAttributeUsingComponent(node,attr);
            if (result != null)
                return result;
            
            if (node.getNodeName().equals("var")){
                Element elem = (Element)node;
                if (elem.getAttribute("name").equals(attr)){
                    Node parent = elem.getParentNode();
                    if (parent.getNodeName().equals("component")){
                        return ((Element)parent).getAttribute("name");
                    }
                }
            }            
        } 
        return null;
    }
    
    public Node GetFirstParameterDependentComponent(Node root,String ParameterString){
        
        ArrayList<Pair> params = SplitParameterString(ParameterString);
            
        for (int i=0;i<params.size();i++){
            Pair p = params.get(i);
            if (p.first == null){
                String first= GetFirstAttributeUsingComponent(root,p.second);
                if (first != null) 
                    p.first = first;
                else{
                    //param is never used!!
                }
            }
        }
            
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("contextcomponent")){
                Node recursiveResult = GetFirstParameterDependentComponent(node,ParameterString);
                if (recursiveResult != null){
                    //is root model context
                    return node;
                }
            }
            if (node.getNodeName().equals("component")){
                Element elem = (Element)node;
                String name = elem.getAttribute("name");
                //sehr in param list wheter or not component "name" is parameter dependent
                for (int j=0;j<params.size();j++){
                    Pair p = params.get(j);
                    if (p.first.equals(name))
                        return node;
                }
            }
        }            
        return null;
    }
    @SuppressWarnings("unchecked")
    public void init() {
        Document doc = this.model.getValue();
	//1. schritt
        //parameter relevante componenten verschieben
        StringTokenizer tok = new StringTokenizer(this.parameterIDs.getValue(),";");
        int n = tok.countTokens();
        String params[] = new String[n];
        for (int i=0;i<n;i++){
            params[i] = tok.nextToken();
        }
        SystemProperties properties = JAMSProperties.createProperties();

        //try to load property values from file
        if (propertyFile.getValue() != null) {
            //check for file provided at command line
            try{
                properties.load(propertyFile.getValue());
            }catch(IOException e){
                this.getModel().getRuntime().sendHalt("Cant find property file, because:" + e.toString());
                }
        } else {
            //check for default file
            String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + ".test";
            File file = new File(defaultFile);
            if (file.exists()) {
                try{
                    properties.load(defaultFile);                    
                }catch(IOException e){
                    this.getModel().getRuntime().sendHalt("Cant find property file, because:" + e.toString());
                }
            }
        }
        //baue abh^ngigkeitsgraph
        StandardRuntime rt = new StandardRuntime();
        rt.loadModel(doc,properties );
        JAMSModel model = rt.getModel();
        
        Hashtable<String,HashSet<String>> dependencyGraph = jams.model.metaoptimizer.metaModelOptimizer.getDependencyGraph(doc,model);
        Hashtable<String,HashSet<String>> transitiveClosureOfDependencyGraph = jams.model.metaoptimizer.metaModelOptimizer.TransitiveClosure(dependencyGraph);
        
        Node root = doc.getDocumentElement();
                
        jams.model.metaoptimizer.metaModelOptimizer.RemoveGUIComponents(root);
        /*jams.model.metaoptimizer.metaModelOptimizer.RemoveNotListedComponents(root,
                jams.model.metaoptimizer.metaModelOptimizer.GetRelevantComponentsList(transitiveClosureOfDependencyGraph,model.CollectAttributeWritingComponents(effAttributeName.getValue())));*/
                                                        
        ArrayList<String> innerTimeContextParameter = new ArrayList();
        ArrayList<String> outerTimeContextParameter = new ArrayList();
        
        ArrayList<String> innerTimeContextBoundaries = new ArrayList();
        ArrayList<String> outerTimeContextBoundaries = new ArrayList();
        
        String innerTimeContextParameterString = "";
        String outerTimeContextParameterString = "";
        String innerTimeContextBoundariesString = "";
        String outerTimeContextBoundariesString = "";

        //get inner and outer time - context parameters
        if (initialisationTimeInterval != null && calibrationTimeInterval != null){
            StringTokenizer param_tok = new StringTokenizer(parameterIDs.getValue(),";");
            StringTokenizer boundaries_tok = new StringTokenizer(boundaries.getValue(),";");
            
            if (param_tok.countTokens() != boundaries_tok.countTokens()){
                this.getModel().getRuntime().sendHalt("number of parameters and boundaries is different");
            }
            
            String newParamString = "";
            while(param_tok.hasMoreTokens()){
                String param = param_tok.nextToken();
                StringTokenizer component_tok = new StringTokenizer(param,".");
                int numOfTokens = component_tok.countTokens();                
                //look if component is child of context
                String componentName = component_tok.nextToken();
                if (IsInContext(root,componentName,timeContext.getValue(),false)){                    
                    if (numOfTokens == 2){
                        //because of time-context splitting, inner time context parameters 
                        //have to be renamed
                        String newParam = ";cal" + param;
                        newParamString += newParam;                        
                        innerTimeContextParameter.add(newParam);
                        innerTimeContextBoundaries.add(boundaries_tok.nextToken());
                    }else{
                        newParamString = newParamString + ";" + param;
                        outerTimeContextParameter.add(param);
                        outerTimeContextBoundaries.add(boundaries_tok.nextToken());
                        
                    }
                }else{
                    while (component_tok.hasMoreTokens())
                        componentName += component_tok.nextToken();
                    outerTimeContextParameter.add(param);
                    outerTimeContextBoundaries.add(boundaries_tok.nextToken());
                }
            }
            //build inner/outer-time context parameter strings
            for (int i=0;i<innerTimeContextParameter.size();i++){
                innerTimeContextParameterString += innerTimeContextParameter.get(i) + ";";
                innerTimeContextBoundariesString += innerTimeContextBoundaries.get(i) + ";";
            }
            for (int i=0;i<outerTimeContextParameter.size();i++){
                outerTimeContextParameterString += outerTimeContextParameter.get(i) + ";";
                outerTimeContextBoundariesString += outerTimeContextBoundaries.get(i) + ";";
            }
        }else{            
            outerTimeContextParameterString = this.parameterIDs.getValue();
            outerTimeContextBoundariesString = this.boundaries.getValue();
        }
                                        
        //optimierer bauen
        Element innerTimeContextOptimizer = doc.createElement("contextcomponent");
        Element outerTimeContextOptimizer = doc.createElement("contextcomponent");
        if (this.optimizationMethod.getValue() == 1){
            innerTimeContextOptimizer.setAttribute("class", "jams.components.optimizer.BranchAndBound");
            outerTimeContextOptimizer.setAttribute("class", "jams.components.optimizer.BranchAndBound");
        }
                
        innerTimeContextOptimizer.setAttribute("name", "innerTimeContextOptimizer");
        outerTimeContextOptimizer.setAttribute("name", "outerTimeContextOptimizer");
        
        AddAttribute(innerTimeContextOptimizer,"parameterIDs",innerTimeContextParameterString,null,true);
        AddAttribute(outerTimeContextOptimizer,"parameterIDs",outerTimeContextParameterString,null,true);
        
        AddAttribute(innerTimeContextOptimizer,"boundaries",innerTimeContextBoundariesString,null,true);
        AddAttribute(outerTimeContextOptimizer,"boundaries",outerTimeContextBoundariesString,null,true);
        
        AddAttribute(innerTimeContextOptimizer,"effMethodName",effMethodName.getValue(),null,true);
        AddAttribute(outerTimeContextOptimizer,"effMethodName",effMethodName.getValue(),null,true);
        
        AddAttribute(innerTimeContextOptimizer,"effValue",effAttributeName.getValue(),null,false);
        AddAttribute(outerTimeContextOptimizer,"effValue",effAttributeName.getValue(),null,false);
        
        AddAttribute(innerTimeContextOptimizer,"mode",Integer.toString(mode.getValue()),null,true);
        AddAttribute(outerTimeContextOptimizer,"mode",Integer.toString(mode.getValue()),null,true);
        
        AddAttribute(innerTimeContextOptimizer,"maxn",Integer.toString(maxn.getValue()),null,true);
        AddAttribute(outerTimeContextOptimizer,"maxn",Integer.toString(maxn.getValue()),null,true);
                
        AddAttribute(innerTimeContextOptimizer,"SampleDumpFileName","samples_innerTimeContext.dat",null,true);
        AddAttribute(outerTimeContextOptimizer,"SampleDumpFileName","samples_outerTimeContext.dat",null,true);
        
        AddAttribute(innerTimeContextOptimizer,"snapshot","innerTimeOptimizerSnapshot",null,false);
        AddAttribute(outerTimeContextOptimizer,"snapshot","outerTimeOptimizerSnapshot",null,false);
        
        AddAttribute(innerTimeContextOptimizer,"outputFileName","optimizer_innerTimeContext.out",null,true);
        AddAttribute(outerTimeContextOptimizer,"outputFileName","optimizer_outerTimeContext.out",null,true);
               
                                            
        //snapshot component
        Element innerTimeModelSnapshot = doc.createElement("component");
        innerTimeModelSnapshot.setAttribute("class", "jams.components.tools.JAMSModelSnapshot");
        innerTimeModelSnapshot.setAttribute("name", "innerTimeOptimizerSnapshot");
        
        Element outerTimeModelSnapshot = doc.createElement("component");
        outerTimeModelSnapshot.setAttribute("class", "jams.components.tools.JAMSModelSnapshot");
        outerTimeModelSnapshot.setAttribute("name", "outerTimeOptimizerSnapshot");
        
        AddAttribute(innerTimeModelSnapshot,"takeSnapshot","true",null,true);
        AddAttribute(outerTimeModelSnapshot,"takeSnapshot","true",null,true);
        
        AddAttribute(innerTimeModelSnapshot,"loadSnapshot","false",null,true);
        AddAttribute(outerTimeModelSnapshot,"loadSnapshot","false",null,true);

        AddAttribute(innerTimeModelSnapshot,"data","innerTimeOptimizerSnapshot",null,false);
        AddAttribute(outerTimeModelSnapshot,"data","outerTimeOptimizerSnapshot",null,false);
            
        AddAttribute(innerTimeModelSnapshot,"holdInMemory","true",null,true);
        AddAttribute(outerTimeModelSnapshot,"holdInMemory","true",null,true);
        
        AddAttribute(innerTimeModelSnapshot, "freezeState","1",null,true);      
        AddAttribute(outerTimeModelSnapshot, "freezeState","1",null,true);      
        
        Node outerTimeOptimizerPosition = null;
        if (!outerTimeContextParameterString.equals("")){
            outerTimeOptimizerPosition = GetFirstParameterDependentComponent(root,outerTimeContextParameterString);
        }
        //hru reducer hinzuf^gen
        if (reduceHRUs.getValue()){
            AddHRUReducerContext(root,doc);
        }
        
        if(initialisationTimeInterval != null && calibrationTimeInterval != null && !innerTimeContextParameterString.equals("")){
            SplitTimeContext(root,timeContext.getValue(),timeIntervalName.getValue(),
                initialisationTimeInterval.toString(),calibrationTimeInterval.toString(),doc,innerTimeContextOptimizer,innerTimeModelSnapshot);
        }
        
        //TODO: sicherstellen, dass outer time optimizer nach hru reducer ausgef^hrt wird?!
        
        //place outer time optimizer        
        if (outerTimeOptimizerPosition != null){
            root.insertBefore(outerTimeModelSnapshot, outerTimeOptimizerPosition);
            root.insertBefore(outerTimeContextOptimizer, outerTimeOptimizerPosition);
            
            //take all following components and add them to optimizer context
            NodeList childs = root.getChildNodes();
            ArrayList<Node> removeNodes = new ArrayList<Node>();
            boolean optimizerReached = false;
            for (int i=0;i<childs.getLength();i++){
                Node node = childs.item(i);
                if (optimizerReached){                    
                    if (node.getNodeName().equals("component") || node.getNodeName().equals("contextcomponent")){
                        removeNodes.add(node);
                        outerTimeContextOptimizer.appendChild(node.cloneNode(true));
                    }
                }
                if (node.isSameNode(outerTimeContextOptimizer))
                    optimizerReached = true;                
            }
            for (int i=0;i<removeNodes.size();i++){
                root.removeChild(removeNodes.get(i));
            }
        }
        
               
        try{
            XMLTools.writeXmlFile(doc, getModel().getWorkspaceDirectory().getPath() + this.resultingModelFile.getValue());
        }catch(Exception e){
            System.out.println(e.toString());
        }                                     
    }
} 