/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import java.util.ArrayList;
import optas.metamodel.ModelModifier.WizardException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author chris
 */
public class ModificationExecutor {

    static public class Modification {
    }

    static public class ReplaceAttribute extends Modification {
        AttributeWrapper attribute;
        String newAttributeName;
        String newAttributeContext;

        public ReplaceAttribute(AttributeWrapper attribute, String newAttributeName, String newAttributeContext){
            this.attribute = attribute;
            this.newAttributeContext = newAttributeContext;
            this.newAttributeName = newAttributeName;
        }

        @Override
        public String toString(){
            return "replace attribute " + attribute.toString() + " with " + newAttributeContext + "." + newAttributeName;
        }
    }

    static public class InsertElement extends Modification {

        Element previousNode;
        Element newNode;

        public InsertElement(Element prev, Element add) {
            this.previousNode = prev;
            this.newNode = add;
        }
        @Override
        public String toString(){
            return "insert node  " + newNode.getAttribute("name") + " after " + previousNode.getAttribute("name");
        }
    }

    static public class InsertBefore extends Modification {

        Element beforeNode;
        Element newNode;

        public InsertBefore(Element newNode, Element before) {
            this.beforeNode = before;
            this.newNode = newNode;
        }
        @Override
        public String toString(){
            return "insert node  " + newNode.getAttribute("name") + " before " + beforeNode.getAttribute("name");
        }
    }

    static public class RemoveElement extends Modification {

        Element node;
        Element parent;

        public RemoveElement(Element parent, Element node) {
            this.parent = parent;
            this.node = node;
        }

        @Override
        public String toString(){
            return "remove node  " + node.getAttribute("name");
        }
    }

    static public class WrapElement extends Modification {

        Element outer;
        Element inner;

        public WrapElement(Element outer, Element inner) {
            this.outer = outer;
            this.inner = inner;
        }
        @Override
        public String toString(){
            return "wrap " + outer.getAttribute("name") + " around " + inner.getAttribute("name");
        }
    }

    static public class ChangeWorkspace extends Modification {
        String workspace;

        public ChangeWorkspace(String ws) {
            this.workspace = ws;
        }

        @Override
        public String toString(){
            return "changing workspace to:" + workspace;
        }
    }
    
    ArrayList<Modification> list = new ArrayList<Modification>();
    Document doc;

    public ModificationExecutor(Document doc, ArrayList<Modification> list ) {
        this.list = list;
        this.doc = doc;
    }

    public void add(Modification list) {
        this.list.add(list);
    }

    public void addAll(ArrayList<Modification> list) {
        this.list.addAll(list);
    }

    public String getLog(){
        String log="";
        for (Modification m : list) {
            log += m.toString() + "\n";
        }
        return log;
    }

    public Document execute() throws ModelModifier.WizardException {
        Node root = (Node)doc;

        for (Modification m : list) {            
            if (m instanceof RemoveElement) {
                RemoveElement re = (RemoveElement)m;
                try{
                    re.parent.removeChild(re.node);
                }catch(DOMException domE){
                    domE.printStackTrace();
                }
            } else if (m instanceof InsertElement) {
                ((InsertElement) m).previousNode.appendChild(((InsertElement) m).newNode);
            } else if (m instanceof WrapElement) {
                WrapElement w = (WrapElement) m;

                Node currentNode = w.inner;
                ArrayList<Node> followingNodes = new ArrayList<Node>();
                do {
                    followingNodes.add(currentNode);
                    currentNode = currentNode.getNextSibling();
                }while(currentNode != null);

                if (w.inner.getParentNode() == null) {
                    throw new WizardException(("Error_model_file_does_not_contain_a_model_context"));
                }

                Node modelContext = w.inner.getParentNode();
                for (int i = 0; i < followingNodes.size(); i++) {
                    modelContext.removeChild(followingNodes.get(i));
                    w.outer.appendChild(followingNodes.get(i));
                }

                modelContext.appendChild(w.outer);
            } else if (m instanceof ChangeWorkspace){
                Tools.changeWorkspace(root, ((ChangeWorkspace)m).workspace);
            } else if (m instanceof InsertBefore){
                InsertBefore ib = (InsertBefore)m;
                Node parent = ib.beforeNode.getParentNode();
                parent.insertBefore( ib.newNode, ib.beforeNode);
            } else if (m instanceof ReplaceAttribute){
                ReplaceAttribute r = (ReplaceAttribute)m;
                Tools.replaceAttribute(root,r.attribute , r.newAttributeName, r.newAttributeContext);
            }
        }
        Tools.doAdjustments(root);
        metaModelOptimizer.removeUnlinkedProperties(root);
        metaModelOptimizer.RemoveEmptyContextes(root);
        return doc;
    }
}
