/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import jams.model.Model;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import optas.metamodel.ModificationExecutor.Modification;
import optas.metamodel.ModificationExecutor.RemoveElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author chris
 */
public class ModelOptimizer {

    Document doc;
    Model model;
    ArrayList<Objective> objectives;
    Hashtable<String, Set<String>> dependencyGraph;
    Hashtable<String, Set<String>> transitiveClosureOfDependencyGraph;
    ArrayList<Modification> actionList = new ArrayList<Modification>();

    public ModelOptimizer(Document doc, Model model, ArrayList<Objective> obj) {
        this.doc = doc;
        this.model = model;
        this.objectives = obj;
    }

    public void writeGDLFile(String path) {
        Set<String> removedComponents = new TreeSet<String>();
        for (Modification m : actionList) {
            if (m instanceof ModificationExecutor.RemoveElement) {
                ModificationExecutor.RemoveElement e = (ModificationExecutor.RemoveElement) m;
                removedComponents.add(e.node.getAttribute("name"));
            }
        }
        //show model graph
        metaModelOptimizer.ExportGDLFile(dependencyGraph, removedComponents, path);
    }

    public void optimize(boolean removeGUI, boolean removeRedundantComponents) {
        dependencyGraph = metaModelOptimizer.getDependencyGraph(this.doc.getDocumentElement(), model);
        transitiveClosureOfDependencyGraph = metaModelOptimizer.TransitiveClosure(dependencyGraph);

        ArrayList<Element> list = new ArrayList<Element>();
        if (removeGUI) {
            list.addAll(Tools.getNodeByAttributeContent(doc, "class", "jams.components.gui"));
            list.addAll(Tools.getNodeByName(doc, "group"));

            for (Element e : list) {
                actionList.add(new RemoveElement( (Element)e.getParentNode(), e));
            }

        }
        if (removeRedundantComponents) {
            Set<String> effWritingComponents = new TreeSet<String>();
            for (Objective o : objectives) {
                effWritingComponents.addAll(
                        metaModelOptimizer.CollectAttributeWritingComponents(
                        (Node) doc.getDocumentElement(),
                        model, o.getMeasurement().getAttributeName(), o.getMeasurement().getContextName()));

                effWritingComponents.addAll(
                        metaModelOptimizer.CollectAttributeWritingComponents(
                        (Node) doc.getDocumentElement(),
                        model, o.getSimulation().getAttributeName(), o.getSimulation().getContextName()));
            }
            Set<String> relevantComponents = metaModelOptimizer.GetRelevantComponentsList(transitiveClosureOfDependencyGraph,
                    effWritingComponents);


            ArrayList<Element> componentList = Tools.getNodeByType(doc, "component");
            for (Element e : componentList) {
                if (!relevantComponents.contains(e.getAttribute("name")))
                    actionList.add(new RemoveElement((Element)e.getParentNode(), e ));
            }
        }
    }

    public ArrayList<Modification> getModifications() {
        return this.actionList;
    }
}
