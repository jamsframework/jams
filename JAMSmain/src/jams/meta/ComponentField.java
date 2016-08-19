package jams.meta;

import jams.JAMS;
import jams.JAMSException;
import jams.data.DefaultDataFactory;
import jams.tools.StringTools;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class ComponentField implements Comparable {

    public static final int READ_ACCESS = 0;
    public static final int WRITE_ACCESS = 1;
    public static final int READWRITE_ACCESS = 2;
    private String value = null;
    private String name = "";
    private String description = "";
    private String defaultValue = "";
    private String unit = "";
    private Class type = null;
    private int accessType;
    private ArrayList<ContextAttribute> contextAttributes = new ArrayList<ContextAttribute>();
    private AttributeList attributeList = null;
    private ComponentDescriptor parent;
    private ContextDescriptor context;

    public ComponentField(String name, Class type, int accessType, String description, String defaultValue, String unit, ComponentDescriptor parent) {
        super();
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.accessType = accessType;
        this.description = description;
        this.defaultValue = defaultValue;
        this.unit = unit;
    }

    public String getAttribute() {
        String aName = "";
        if (contextAttributes.isEmpty()) {
            return aName;
        }
        for (ContextAttribute ca : contextAttributes) {
            aName += ca.getName() + ";";
        }
        aName = aName.substring(0, aName.length() - 1);
        return aName;
    }

    public ContextDescriptor getContext() {
//        // @TODO: can have multiple attributes, but only one context :(
//        if (contextAttributes.size() > 0) {
//            return contextAttributes.get(0).getContext();
//        } else {
//            return null;
//        }
        return context;
    }

    public ArrayList<ContextAttribute> getContextAttributes() {
        return contextAttributes;
    }

    public String getValue() {
        return value;
    }

    public void linkToAttributeList(ContextDescriptor context, AttributeList list) {
        this.attributeList = list;
        this.context = context;
        
        for (String attributeName : attributeList.getElements()) {
            linkToAttribute(getContext(), attributeName, false);
        }

        this.attributeList.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object o1) {
                String attributeName = (String) o1;
                if (getAttributeList().getElements().contains(o1)) {
                    linkToAttribute(getContext(), attributeName, false);
                } else {
                    unlinkFromAttribute(attributeName);
                }
            }
        });
    }

    public void unlinkFromAttribute() {
        // we need an array since contextAttributes is modified in unlinkFromAttribute(ca)
        for (ContextAttribute ca : this.contextAttributes.toArray(new ContextAttribute[this.contextAttributes.size()])) {
            unlinkFromAttribute(ca);
        }
    }

    public void unlinkFromAttribute(String caName) {
        for (ContextAttribute ca : this.contextAttributes) {
            if (ca.getName().equals(caName)) {
                unlinkFromAttribute(ca);
                return;
            }
        }
    }

    public void unlinkFromAttribute(ContextAttribute ca) {
        // remove from ContextAttribute
        ca.getFields().remove(this);
        // if ContextAttribute has no connected fields anymore, remove it from its context
        if (ca.getFields().isEmpty()) {
            ContextDescriptor context = ca.getContext();
            context.getDynamicAttributes().remove(ca.getName());
        }
        this.contextAttributes.remove(ca);
    }

    public void linkToAttribute(ContextDescriptor context, String attributeName) {
        linkToAttribute(context, attributeName, true);
    }

    public void linkToAttribute(ContextDescriptor context, String attributeName, boolean removeOldLink) {

        Class basicType;

        if (removeOldLink) {
            unlinkFromAttribute();
        }

        // if there is more than one attribute bound to this
        if (attributeName.contains(";")) {
            if (this.type.isArray()) {

                String[] attributeNames = StringTools.toArray(attributeName, ";");
                for (String s : attributeNames) {
                    linkToAttribute(context, s, false);
                }
                return;
            } else {
                throw new JAMSException(MessageFormat.format(JAMS.i18n("Semicolons_not_allowed_in_attribute_names"), parent.getInstanceName(), attributeName));
            }
        }
        if (this.type.isArray()) {
            basicType = this.type.getComponentType();
        } else {
            basicType = this.type;
        }
        if (!basicType.isInterface()) {
            basicType = DefaultDataFactory.getDataFactory().getBelongingInterface(basicType);
        }
        ContextAttribute attribute = context.getDynamicAttributes().get(attributeName);
        // check if already existing
        if ((attribute != null) && (!basicType.isAssignableFrom(attribute.getType()))) {
            throw new JAMSException(MessageFormat.format(JAMS.i18n("Attribute_already_exists_in_context_with_different_type"), attributeName, context.getInstanceName(), attribute.getType()));
        }

        if (attribute == null) {
            attribute = new ContextAttribute(attributeName, basicType, context);
            context.getDynamicAttributes().put(attributeName, attribute);

//            if (this.accessType == READ_ACCESS) {
//                // add this attribute to the list of undeclared attributes
//                System.out.println("undeclared in " + context.getInstanceName() + ": " + attributeName);
//            }
        }
        attribute.getFields().add(this);
        this.contextAttributes.add(attribute);
        this.context = context;
    }

//    public class AttributeLinkException extends JAMSException {
//
//        public AttributeLinkException(String message, String header) {
//            super(message, header);
//        }
//    }
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the parent
     */
    public ComponentDescriptor getParent() {
        return parent;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }        
    
    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }    
    
    /**
     * @return the description
     */
    public String getUnit() {
        return unit;
    }      

    /**
     * @return the type
     */
    public Class getType() {
        return type;
    }

    /**
     * @return the accessType
     */
    public int getAccessType() {
        return accessType;
    }

    public String toString() {
        return this.parent.getInstanceName() + "." + this.name;
    }

    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

    /**
     * @return the attributeList
     */
    public AttributeList getAttributeList() {
        return attributeList;
    }
}
