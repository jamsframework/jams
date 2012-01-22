package jams.meta;

import jams.JAMS;
import jams.JAMSException;
import jams.data.JAMSDataFactory;
import jams.tools.StringTools;
import java.util.ArrayList;

public class ComponentField {

    public static final int READ_ACCESS = 0;
    public static final int WRITE_ACCESS = 1;
    public static final int READWRITE_ACCESS = 2;
    private String value = null;
    private String name = "";
    private Class type = null;
    private int accessType;
    private ArrayList<ContextAttribute> contextAttributes = new ArrayList<ContextAttribute>();
    private ComponentDescriptor parent;

    public ComponentField(String name, Class type, int accessType, ComponentDescriptor parent) {
        super();
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.accessType = accessType;
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
        // @TODO: can have multiple attributes, but only one context :(
        if (contextAttributes.size() > 0) {
            return contextAttributes.get(0).getContext();
        } else {
            return null;
        }
    }

    public ArrayList<ContextAttribute> getContextAttributes() {
        return contextAttributes;
    }

    public String getValue() {
        return value;
    }

    public void unlinkFromAttribute() {
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

    public void linkToAttribute(ContextDescriptor context, String attributeName) throws AttributeLinkException {
        Class basicType;
        // if there is more than one attribute bound to this
        if (attributeName.contains(";")) {
            if (this.type.isArray()) {
                String[] attributeNames = StringTools.toArray(attributeName, ";");
                for (String s : attributeNames) {
                    linkToAttribute(context, s);
                }
                return;
            } else {
                throw new AttributeLinkException("Semicolons are not allowed in attribute names!("+attributeName+")", JAMS.i18n("Error"));
            }
        }
        if (this.type.isArray()) {
            basicType = this.type.getComponentType();
        } else {
            basicType = this.type;
        }
        if (!basicType.isInterface()) {
            basicType = JAMSDataFactory.getBelongingInterface(basicType);
        }
        ContextAttribute attribute = context.getDynamicAttributes().get(attributeName);
        // check if already existing
        if ((attribute != null) && (attribute.getType() != basicType)) {
            throw new AttributeLinkException("Attribute " + attributeName + " already exists in context " + context.getInstanceName() + " with different type " + attribute.getType(), JAMS.i18n("Error"));
        }
        if (!this.type.isArray()) {
            // unlink from old ContextAttribute
            unlinkFromAttribute();
        }
        if (attribute == null) {
            attribute = new ContextAttribute(attributeName, basicType, context);
            context.getDynamicAttributes().put(attributeName, attribute);
        }
        attribute.getFields().add(this);
        this.contextAttributes.add(attribute);
    }

    public class AttributeLinkException extends JAMSException {

        public AttributeLinkException(String message, String header) {
            super(message, header);
        }
    }

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
}
