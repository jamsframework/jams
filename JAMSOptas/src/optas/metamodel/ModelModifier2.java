/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import jams.tools.XMLTools;
import java.util.ArrayList;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.SimpleOptimizationController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author christian
 */
public class ModelModifier2 {

    static final String OPTIMIZER_CONTEXT_NAME = "optimizer";
    static final String OBJECTIVE_COMPONENT_NAME = "objective";

    private static Element createOptimizerComponent(Document doc, Optimization opt, String relaxationAttribute) {
        //optimierer bauen
        Element optimizerContext = doc.createElement("contextcomponent");
        optimizerContext.setAttribute("class", SimpleOptimizationController.class.getName());
        optimizerContext.setAttribute("name", OPTIMIZER_CONTEXT_NAME);

        optas.metamodel.Tools.addAttribute(optimizerContext, "maxn", Integer.toString(Integer.MAX_VALUE),
                null, true);
        
        optas.metamodel.Tools.addAttribute(optimizerContext, "optimizationClassName", opt.getOptimizerDescription().getOptimizerClassName(),
                null, true);

        optas.metamodel.Tools.addAttribute(optimizerContext, "parameterization", getOptimizerParameterString(opt),
                null, true);
        if (relaxationAttribute != null) {
            optas.metamodel.Tools.addAttribute(optimizerContext, "relaxationParameter", relaxationAttribute,
                    OPTIMIZER_CONTEXT_NAME, false);
        }
        return optimizerContext;
    }

    static private String getOptimizerParameterString(Optimization opt) {
        String parameter = "";
        for (OptimizerParameter p : opt.getOptimizerDescription().getPropertyMap().values()) {
            parameter += p.getString() + ";";
        }
        return parameter;
    }

    static private Element getOptimizer(Document doc) {
        ArrayList<Element> elem = Tools.getNodeByAttribute(Tools.getModelNode(doc), "name", OPTIMIZER_CONTEXT_NAME);
        if (elem.size() > 0) {
            return elem.get(0);
        }
        return null;
    }

    static private Document wrapOptimizerAroundModel(Document doc, Element optimizerContext){       
       Node root = Tools.getModelNode(doc);
       Element rootElement = (Element)root;
       String modelName = rootElement.getAttribute("name");
       rootElement.setAttribute("name", modelName + "_1");
       doc.adoptNode(optimizerContext);
       NodeList childs = root.getChildNodes();
       ArrayList<Node> moveList = new ArrayList<Node>();
       for (int i=0;i<childs.getLength();i++){
           if ( childs.item(i).getNodeName().equals("contextcomponent") ||
                childs.item(i).getNodeName().equals("component")        ||
                childs.item(i).getNodeName().equals("attribute") ){
               moveList.add(childs.item(i));
               }
       }
       Element newContext = doc.createElement("contextcomponent");
       newContext.setAttribute("name", modelName);
       newContext.setAttribute("class", "jams.model.JAMSContext");
       for (Node node : moveList){
           root.removeChild(node);
           newContext.appendChild(node);
       }
       optimizerContext.appendChild(newContext);
       //doc.replaceChild(optimizerContext, root);
       //((Element)root).setNodeValue("contextcompoent");
       root.appendChild(optimizerContext);       
       
//       ArrayList<Element> launcherNodes = Tools.getNodeByType(root, "launcher");
//       ArrayList<Element> datastoreNodes = Tools.getNodeByType(root, "datastore");
//       launcherNodes.addAll(datastoreNodes);
//       for (int i=0;i<launcherNodes.size();i++){
//            Element e = launcherNodes.get(i);
//            e.getParentNode().removeChild(e);
//            optimizerContext.appendChild(e);
//       }
       try{
       XMLTools.writeXmlFile(doc, "E:/JAMS/test.xml");
       }catch(Exception e){
           e.printStackTrace();
       }
       return doc;
    }
    
    static public Document addOptimizationContext(Document doc, Optimization opt) {
        Element optimizerContext = getOptimizer(doc);
        if (optimizerContext == null) {
            optimizerContext = createOptimizerComponent(doc, opt, null);
            doc = wrapOptimizerAroundModel(doc, optimizerContext);
        } else {
            NodeList list = optimizerContext.getChildNodes();
            optimizerContext.setAttribute("class", SimpleOptimizationController.class.getName());
            optimizerContext.setAttribute("name", OPTIMIZER_CONTEXT_NAME);
            for (int i = 0; i < list.getLength(); i++) {
                Node child = list.item(i);       
                Element e  = (Element)child;
                if (child.getNodeName().equals("var")) {
                    if (e.hasAttribute("name")) {
                        String name = e.getAttribute("name");
                   
                        if(name.equals("parameterization")) {
                            e.setAttribute("value", getOptimizerParameterString(opt));
                        }if(name.equals("optimizationClassName")) {
                            e.setAttribute("value", opt.getOptimizerDescription().getOptimizerClassName());
                        }
                    } 
                }
            }
        }
        return doc ;
    }    
}
