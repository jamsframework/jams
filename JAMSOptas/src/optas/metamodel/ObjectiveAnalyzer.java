/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.metamodel;

import jams.data.Attribute.TimeInterval;
import java.util.ArrayList;

import optas.efficiencies.UniversalEfficiencyCalculator;
import optas.metamodel.ModelModifier.WizardException;
import optas.metamodel.ModificationExecutor.InsertElement;
import optas.metamodel.ModificationExecutor.Modification;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author chris
 */
public class ObjectiveAnalyzer {

    private ArrayList<Objective> objectives;
    private String context;
    private int efficiencyComponentCounter = 0;
    private ArrayList<AttributeWrapper> objectiveList = new ArrayList<AttributeWrapper>();
    private ArrayList<Modification> actionList = new ArrayList<Modification>();
    private String effValueString = "";

    public ObjectiveAnalyzer(ArrayList<Objective> objectives, String context){
        this.objectives = objectives;
        this.context = context;
    }

    public ArrayList<AttributeWrapper> getObjectiveList(){
        return objectiveList;

    }

    public ArrayList<Modification> getModifications(){
        return actionList;
    }

    private Element createEfficiencyComponent(Document doc, Objective o, String attributeName) throws WizardException {        
        if (!o.getMeasurement().getContextName().equals(o.getSimulation().getContextName())) {
            throw new WizardException("Error: Mismatch of measurement and simulation context with objective:" + o.toString());
        }

        Element efficiencyComponent = doc.createElement("component");
        efficiencyComponent.setAttribute("class", UniversalEfficiencyCalculator.class.getName());
        efficiencyComponent.setAttribute("name", "objective" + efficiencyComponentCounter++);

        optas.metamodel.Tools.addAttribute(efficiencyComponent, "measurementAttributeName", o.getMeasurement().getChildName(),
                null, true);

        optas.metamodel.Tools.addAttribute(efficiencyComponent, "measurement", o.getMeasurement().getChildName(),
                o.getMeasurement().getContextName(), false);

        optas.metamodel.Tools.addAttribute(efficiencyComponent, "simulationAttributeName", o.getSimulation().getChildName(),
                null, true);

        optas.metamodel.Tools.addAttribute(efficiencyComponent, "simulation", o.getSimulation().getChildName(),
                o.getSimulation().getContextName(), false);

        /*optas.metamodel.Tools.addAttribute(efficiencyComponent, "method", o.getMethod(),
                null, true);*/

        String timeDomain = "";
        for (TimeInterval t : o.getTimeDomain()) {
            timeDomain += t.getValue() + ";";
        }

        optas.metamodel.Tools.addAttribute(efficiencyComponent, "timeInterval", timeDomain,
                null, true);

        ArrayList<Element> temporalNodes = Tools.getNodeByName(doc, o.getSimulation().getContextName());
        if (!temporalNodes.isEmpty()){
            ArrayList<Element> timeNode = Tools.getVariable(temporalNodes.get(0), "current");
            if (!timeNode.isEmpty()){
                String context = timeNode.get(0).getAttribute("context");
                if (context != null){
                    optas.metamodel.Tools.addAttribute(efficiencyComponent, "time", "time",
                        context, false);
                }
            }
        }
        if (o.getMethod().equals("Root Mean Square Error")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "e2_normalized", attributeName ,
                    context, false);
        } else if (o.getMethod().equals("Nash Sutcliffe (e1)")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "e1_normalized", attributeName ,
                    context, false);
        } else if (o.getMethod().equals("Nash Sutcliffe (e2)")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "e2_normalized", attributeName ,
                    context, false);
        } else if (o.getMethod().equals("log Nash Sutcliffe (le1)")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "le1_normalized", attributeName ,
                    context, false);
        } else if (o.getMethod().equals("log Nash Sutcliffe (le2)")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "le2_normalized", attributeName ,
                    context, false);
        } else if (o.getMethod().equals("Average Volume Error")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "ave_normalized", attributeName ,
                    context, false);
        } else if (o.getMethod().equals("r2")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "r2_normalized", attributeName ,
                    context, false);
        } else if (o.getMethod().equals("relative bias")) {
                optas.metamodel.Tools.addAttribute(efficiencyComponent, "bias_normalized", attributeName ,
                    context, false);
        }
        
        AttributeWrapper a = new AttributeWrapper(null, attributeName, null, context);
        this.objectiveList.add(a);

        return efficiencyComponent;
    }

    public String getAttributeList(){
        return effValueString;
    }

    public void analyse(Document doc) throws WizardException{        
        int counter=0;

        for (Objective o : objectives){
            String attributeName = "e"+efficiencyComponentCounter;
            Element e = createEfficiencyComponent(doc, o, attributeName);

            ArrayList<Element> nodes = Tools.getNodeByName(doc, o.getMeasurement().getContextName());
            if (nodes.isEmpty()){
                throw new WizardException("Error objective " + attributeName +
                        " should use data from context: " + o.getMeasurement().getContextName() +
                        "but this context does not exist\n" + o);
            }else if (nodes.size()>1){
                throw new WizardException("Error objective " + attributeName +
                        " should use data from context: " + o.getMeasurement().getContextName() +
                        "but there is more than one context with this name\n" + o);
            }
            this.actionList.add(new InsertElement((Element)nodes.get(0), (Element)e));

            if (counter++ == objectives.size()-1)
                effValueString += attributeName;
            else
                effValueString += attributeName + ";";
        }
    }
}
