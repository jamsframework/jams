/*
 * DataflowGraph.java
 * Created on 19.10.2026
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.dataflow;

import jams.JAMS;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelNode;
import jams.meta.OutputDSDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Data flow graph of a JAMS model, derived from its model descriptor. Nodes are
 * the enabled components and contexts, the input datastores, the entity
 * parameters and the output datastores. Edges connect the component that
 * writes an attribute with each component that reads it.
 *
 * @author Sven Kralisch
 */
public class DataflowGraph {

    public static class Options {

        public boolean showConstants = false;
        public boolean showParameters = true;
        public boolean showUnused = true;
        public boolean showStates = true;
        public String hidePattern = "";
    }

    public enum NodeKind {
        COMPONENT, CONTEXT, DATASTORE, PARAMETERS, OUTPUT
    }

    public enum PortKind {
        DATA, PARAMETER, CONSTANT, UNUSED
    }

    public static class Port {

        public final Node node;
        public final String label;
        public final boolean input;
        public final PortKind kind;
        public final String key;
        public String tooltip;
        public boolean time;
        public int index;

        Port(Node node, String label, boolean input, PortKind kind, String key) {
            this.node = node;
            this.label = label;
            this.input = input;
            this.kind = kind;
            this.key = key;
        }
    }

    public static class Node {

        public final NodeKind kind;
        public final String title;
        public final String subtitle;
        public final ComponentDescriptor descriptor;
        public final int order;
        public final List<Port> inputs = new ArrayList<>();
        public final List<Port> outputs = new ArrayList<>();
        public final List<String> states = new ArrayList<>();
        public int colorIndex;

        Node(NodeKind kind, String title, String subtitle, ComponentDescriptor descriptor, int order) {
            this.kind = kind;
            this.title = title;
            this.subtitle = subtitle;
            this.descriptor = descriptor;
            this.order = order;
        }

        Port input(String label, PortKind kind, String key) {
            for (Port p : inputs) {
                if (p.label.equals(label)) {
                    return p;
                }
            }
            Port p = new Port(this, label, true, kind, key);
            inputs.add(p);
            return p;
        }

        Port output(String label, PortKind kind, String key) {
            for (Port p : outputs) {
                if (p.label.equals(label)) {
                    return p;
                }
            }
            Port p = new Port(this, label, false, kind, key);
            outputs.add(p);
            return p;
        }

        public boolean isEmpty() {
            return inputs.isEmpty() && outputs.isEmpty() && states.isEmpty();
        }
    }

    public static class Edge {

        public final Port from;
        public final Port to;
        /**
         * value written later in the execution order, i.e. read from the
         * previous iteration
         */
        public final boolean feedback;
        public final boolean parameter;
        public final boolean time;

        Edge(Port from, Port to, boolean feedback, boolean parameter, boolean time) {
            this.from = from;
            this.to = to;
            this.feedback = feedback;
            this.parameter = parameter;
            this.time = time;
        }
    }

    /**
     * One access of a component field to a context attribute
     */
    private static class Access {

        Node node;
        int order;
        String name;
        String key;
        boolean read, write;
        Class<?> type;
        ComponentField field;
    }

    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final String modelName;

    private DataflowGraph(String modelName) {
        this.modelName = modelName;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public String getModelName() {
        return modelName;
    }

    public static DataflowGraph build(ModelDescriptor md, Options options) {

        DataflowGraph graph = new DataflowGraph(md.getModelName());

        Pattern hide = null;
        if (options.hidePattern != null && !options.hidePattern.trim().isEmpty()) {
            try {
                hide = Pattern.compile(options.hidePattern.trim());
            } catch (PatternSyntaxException ex) {
                hide = null;
            }
        }

        // collect all enabled components and contexts in execution order
        List<ComponentDescriptor> items = new ArrayList<>();
        collect(md.getRootNode(), items, true);

        Map<ComponentDescriptor, Node> nodeMap = new HashMap<>();
        List<Access> accesses = new ArrayList<>();
        List<Node> datastores = new ArrayList<>();
        // collection key -> components that create the entities or add
        // attributes to them (readers), creator first
        Map<String, List<Node>> entitySources = new LinkedHashMap<>();

        int order = 0;
        for (ComponentDescriptor cd : items) {
            order++;
            boolean isContext = cd instanceof ContextDescriptor;
            Node node = new Node(isContext ? NodeKind.CONTEXT : NodeKind.COMPONENT,
                    cd.getInstanceName(), cd.getClazz().getSimpleName(), cd, order);
            nodeMap.put(cd, node);

            for (String fieldName : cd.getComponentFieldList()) {
                ComponentField field = cd.getComponentFields().get(fieldName);
                List<ContextAttribute> cas = field.getContextAttributes();

                if (cas.isEmpty()) {
                    String value = field.getValue();
                    if (value == null || value.isEmpty()) {
                        continue;
                    }
                    if (fieldName.equals("id") && cd.getClazz().getSimpleName().contains("Reader")) {
                        // input datastore
                        Node ds = new Node(NodeKind.DATASTORE, value, JAMS.i18n("DF_Datastore"), null, order);
                        Port out = ds.output(value, PortKind.DATA, null);
                        Port in = node.input(value, PortKind.DATA, null);
                        graph.edges.add(new Edge(out, in, false, false, false));
                        datastores.add(ds);
                    } else if (options.showConstants) {
                        Port p = node.input(fieldName + " = " + shorten(value), PortKind.CONSTANT, null);
                        p.tooltip = fieldName + " = " + value;
                    }
                    continue;
                }

                for (ContextAttribute ca : cas) {
                    String typeName = ca.getType() == null ? "" : ca.getType().getName();
                    if (typeName.contains("EntityCollection")) {
                        // structural link to the entity collection, remember
                        // who creates the entities or reads attributes into them
                        boolean creates = field.getAccessType() != ComponentField.READ_ACCESS;
                        if (!isContext && (creates || cd.getClazz().getSimpleName().contains("Reader"))) {
                            List<Node> sources = entitySources.computeIfAbsent(collectionKey(ca), k -> new ArrayList<>());
                            if (!sources.contains(node)) {
                                if (creates) {
                                    sources.add(0, node);
                                } else {
                                    sources.add(node);
                                }
                            }
                        }
                        continue;
                    }

                    ContextDescriptor ctx = ca.getContext();
                    if (ctx != null && ctx.getStaticAttributes().containsKey(ca.getName())) {
                        // model attribute with a fixed value
                        if (options.showConstants) {
                            String value = ctx.getStaticAttributes().get(ca.getName()).getValue();
                            Port p = node.input(ca.getName() + " = " + shorten(value), PortKind.CONSTANT, null);
                            p.tooltip = ca.getName() + " = " + value;
                        }
                        continue;
                    }
                    if (hide != null && hide.matcher(ca.getName()).matches()) {
                        continue;
                    }

                    Access a = new Access();
                    a.node = node;
                    a.order = order;
                    a.name = ca.getName();
                    a.key = attributeKey(ca);
                    a.type = ca.getType();
                    a.field = field;
                    a.read = field.getAccessType() != ComponentField.WRITE_ACCESS;
                    a.write = field.getAccessType() != ComponentField.READ_ACCESS;
                    accesses.add(a);
                }
            }
        }

        // entity sources always show the files they read
        if (options.showParameters && !options.showConstants) {
            Map<Node, Boolean> done = new HashMap<>();
            for (List<Node> sources : entitySources.values()) {
                for (Node n : sources) {
                    if (done.put(n, true) == null) {
                        addFileConstants(n);
                    }
                }
            }
        }

        // writers and readers per attribute key
        Map<String, List<Access>> writers = new HashMap<>();
        Map<String, List<Access>> readers = new HashMap<>();
        for (Access a : accesses) {
            if (a.write) {
                writers.computeIfAbsent(a.key, k -> new ArrayList<>()).add(a);
            }
            if (a.read) {
                readers.computeIfAbsent(a.key, k -> new ArrayList<>()).add(a);
            }
        }

        // attributes written to output datastores
        Map<String, Boolean> traced = new HashMap<>();
        for (OutputDSDescriptor ods : md.getDatastores().values()) {
            if (ods.isEnabled()) {
                for (ContextAttribute ca : ods.getContextAttributes()) {
                    traced.put(attributeKey(ca), true);
                }
            }
        }

        Map<String, Node> parameterNodes = new LinkedHashMap<>();
        Map<String, Boolean> consumed = new HashMap<>();

        for (Access r : accesses) {
            if (!r.read) {
                continue;
            }
            List<Access> ws = writers.getOrDefault(r.key, new ArrayList<>());

            Access prev = null, later = null;
            boolean selfWrite = false;
            for (Access w : ws) {
                if (w.node == r.node) {
                    selfWrite = true;
                    continue;
                }
                if (w.order < r.order) {
                    prev = w;
                } else if (later == null || w.order > later.order) {
                    later = w;
                }
            }

            if (prev == null && later == null) {
                if (selfWrite) {
                    boolean readByOthers = traced.containsKey(r.key);
                    for (Access o : readers.get(r.key)) {
                        readByOthers |= o.node != r.node;
                    }
                    if (readByOthers) {
                        // read-write output (e.g. result of a calculation), handled by its readers
                        continue;
                    }
                    // value carried over from step to step by the component itself
                    if (options.showStates && !r.node.states.contains(r.name)) {
                        r.node.states.add(r.name);
                    }
                    consumed.put(r.node.order + ":" + r.key, true);
                    continue;
                }
                if (!options.showParameters) {
                    continue;
                }
                // no producer within the model, e.g. entity parameters read from file
                String groupKey = r.key.startsWith("E:") ? r.key.substring(0, r.key.indexOf('/')) : "X";
                Node pn = parameterNodes.get(groupKey);
                if (pn == null) {
                    String title, sub;
                    List<Node> sources = new ArrayList<>();
                    if (groupKey.startsWith("E:")) {
                        title = groupKey.substring(groupKey.lastIndexOf('.') + 1);
                        sub = JAMS.i18n("DF_Entity_Collection");
                        sources = entitySources.getOrDefault(groupKey, sources);
                    } else {
                        title = JAMS.i18n("DF_No_Producer");
                        sub = JAMS.i18n("DF_No_Producer_Sub");
                    }
                    pn = new Node(NodeKind.PARAMETERS, title, sub, null, 0);
                    parameterNodes.put(groupKey, pn);
                    // components that fill the collection
                    for (Node src : sources) {
                        Port so = src.output(title, PortKind.PARAMETER, groupKey);
                        so.tooltip = "<html>" + JAMS.i18n("DF_Entity_Collection") + " <b>" + title + "</b></html>";
                        Port si = pn.input(src.title, PortKind.PARAMETER, groupKey);
                        graph.edges.add(new Edge(so, si, false, true, false));
                    }
                }
                Port out = pn.output("params", PortKind.PARAMETER, groupKey);
                Port in = r.node.input(r.name, PortKind.PARAMETER, r.key);
                in.tooltip = tooltip(r);
                graph.edges.add(new Edge(out, in, false, true, false));
                continue;
            }

            Access w = prev != null ? prev : later;
            Port out = w.node.output(w.name, PortKind.DATA, w.key);
            out.tooltip = tooltip(w);
            Port in = r.node.input(r.name, PortKind.DATA, r.key);
            in.tooltip = tooltip(r);
            out.time = in.time = isTime(r.type);
            graph.edges.add(new Edge(out, in, prev == null, false, out.time));
            consumed.put(w.node.order + ":" + w.key, true);
        }

        // output datastores
        List<Node> outputs = new ArrayList<>();
        for (OutputDSDescriptor ods : md.getDatastores().values()) {
            if (!ods.isEnabled()) {
                continue;
            }
            Node on = new Node(NodeKind.OUTPUT, ods.getName(), JAMS.i18n("DF_Output") + " (" + ods.getContext().getInstanceName() + ")", null, Integer.MAX_VALUE);
            for (ContextAttribute ca : ods.getContextAttributes()) {
                String key = attributeKey(ca);
                List<Access> ws = writers.get(key);
                Port in = on.input(ca.getName(), PortKind.DATA, key);
                if (ws == null || ws.isEmpty()) {
                    continue;
                }
                Access w = ws.get(ws.size() - 1);
                Port out = w.node.output(w.name, PortKind.DATA, w.key);
                out.tooltip = tooltip(w);
                out.time = in.time = isTime(w.type);
                graph.edges.add(new Edge(out, in, false, false, out.time));
                consumed.put(w.node.order + ":" + w.key, true);
            }
            outputs.add(on);
        }

        // outputs that nobody reads
        if (options.showUnused) {
            for (Access w : accesses) {
                if (w.write && !consumed.containsKey(w.node.order + ":" + w.key) && !w.node.states.contains(w.name)) {
                    boolean exists = false;
                    for (Port p : w.node.outputs) {
                        exists |= p.label.equals(w.name);
                    }
                    if (!exists) {
                        Port p = w.node.output(w.name, PortKind.UNUSED, w.key);
                        p.tooltip = tooltip(w);
                    }
                }
            }
        }

        // assemble node list: datastores, components/contexts, parameters, outputs
        graph.nodes.addAll(datastores);
        for (ComponentDescriptor cd : items) {
            Node n = nodeMap.get(cd);
            if (!n.isEmpty()) {
                graph.nodes.add(n);
            }
        }
        graph.nodes.addAll(parameterNodes.values());
        graph.nodes.addAll(outputs);

        // ports: inputs ordered as declared, data before parameters before constants
        for (Node n : graph.nodes) {
            n.inputs.sort((a, b) -> Integer.compare(rank(a.kind), rank(b.kind)));
            n.outputs.sort((a, b) -> Integer.compare(rank(a.kind), rank(b.kind)));
            for (int i = 0; i < n.inputs.size(); i++) {
                n.inputs.get(i).index = i;
            }
            for (int i = 0; i < n.outputs.size(); i++) {
                n.outputs.get(i).index = i;
            }
        }

        // colour index per producing node
        int color = 0;
        for (Node n : graph.nodes) {
            if (!n.outputs.isEmpty()) {
                n.colorIndex = color++;
            }
        }

        return graph;
    }

    private static int rank(PortKind kind) {
        switch (kind) {
            case DATA:
                return 0;
            case PARAMETER:
                return 1;
            case UNUSED:
                return 2;
            default:
                return 3;
        }
    }

    private static void collect(ModelNode node, List<ComponentDescriptor> items, boolean isRoot) {
        ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
        if (!isRoot) {
            if (!cd.isEnabled()) {
                return;
            }
            items.add(cd);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collect((ModelNode) node.getChildAt(i), items, false);
        }
    }

    /**
     * Key that identifies an attribute independent of the context it is
     * accessed from. Attributes of spatial contexts belong to the entities of
     * the context, i.e. two spatial contexts iterating over the same entity
     * collection share their attributes.
     */
    private static String attributeKey(ContextAttribute ca) {
        ContextDescriptor ctx = ca.getContext();
        String collection = entityCollection(ctx);
        if (collection != null) {
            return collection + "/" + ca.getName();
        }
        return "C:" + (ctx == null ? "" : ctx.getInstanceName()) + "/" + ca.getName();
    }

    private static String entityCollection(ContextDescriptor ctx) {
        if (ctx == null) {
            return null;
        }
        ComponentField f = ctx.getComponentFields().get("entities");
        if (f == null || f.getContextAttributes().isEmpty()) {
            return null;
        }
        return collectionKey(f.getContextAttributes().get(0));
    }

    private static String collectionKey(ContextAttribute ca) {
        return "E:" + (ca.getContext() == null ? "" : ca.getContext().getInstanceName()) + "." + ca.getName();
    }

    /**
     * Lists the file names a component reads (fields named "...file..." with
     * a fixed value) as constants.
     */
    private static void addFileConstants(Node n) {
        ComponentDescriptor cd = n.descriptor;
        for (String fieldName : cd.getComponentFieldList()) {
            ComponentField f = cd.getComponentFields().get(fieldName);
            String v = f.getValue();
            if (f.getContextAttributes().isEmpty() && v != null && !v.isEmpty()
                    && fieldName.toLowerCase().contains("file")) {
                Port p = n.input(fieldName + " = " + shorten(v), PortKind.CONSTANT, null);
                p.tooltip = fieldName + " = " + v;
            }
        }
    }

    private static boolean isTime(Class<?> type) {
        return type != null && type.getName().endsWith("Calendar");
    }

    private static String tooltip(Access a) {
        String type = a.type == null ? "" : a.type.getSimpleName();
        String unit = a.field.getUnit();
        String desc = a.field.getDescription();
        StringBuilder sb = new StringBuilder("<html><b>").append(a.name).append("</b> (").append(type);
        if (unit != null && !unit.isEmpty()) {
            sb.append(", ").append(unit);
        }
        sb.append(")<br>").append(a.node.title).append(".").append(a.field.getName());
        if (desc != null && !desc.isEmpty()) {
            sb.append("<br><i>").append(desc).append("</i>");
        }
        return sb.append("</html>").toString();
    }

    private static String shorten(String value) {
        return value.length() > 24 ? value.substring(0, 22) + "…" : value;
    }
}
