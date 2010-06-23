/*
 * Context.java
 * Created on 22.06.2010, 21:30:39
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.gui.graph;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DependencyGraph {

    static int edgeCount = 0;

    public static void main(String[] args) {
        // Graph<V, E> where V is the type of the vertices
// and E is the type of the edges
        Graph<Integer, String> g = new SparseMultigraph<Integer, String>();
// Add some vertices. From above we defined these to be type Integer.
        g.addVertex((Integer) 1);
        g.addVertex((Integer) 2);
        g.addVertex((Integer) 3);
// Add some edges. From above we defined these to be of type String
// Note that the default is for undirected edges.
        g.addEdge("Edge-A", 1, 2); // Note that Java 1.5 auto-boxes primitives
        g.addEdge("Edge-B", 2, 3);
// Let's see what we have. Note the nice output from the
// SparseMultigraph<V,E> toString() method
        System.out.println("The graph g = " + g.toString());
// Note that we can use the same nodes and edges in two different graphs.
        Graph<Integer, String> g2 = new SparseMultigraph<Integer, String>();
        g2.addVertex((Integer) 1);
        g2.addVertex((Integer) 2);
        g2.addVertex((Integer) 3);
        g2.addEdge("Edge-A", 1, 3);
        g2.addEdge("Edge-B", 2, 3, EdgeType.DIRECTED);
        g2.addEdge("Edge-C", 3, 2, EdgeType.DIRECTED);
        g2.addEdge("Edge-P", 2, 3); // A parallel edge
        System.out.println("The graph g2 = " + g2.toString());
        
    }

    class MyNode {

        int id; // good coding practice would have this as private
        public MyNode(int id) {
            this.id = id;
        }

        public String toString() { // Always a good idea for debuging
            return "V" + id; // JUNG2 makes good use of these.
        }
    }

    class MyLink {

        double capacity; // should be private
        double weight; // should be private for good practice
        int id;

        public MyLink(double weight, double capacity) {
            this.id = edgeCount++; // This is defined in the outer class.
            this.weight = weight;
            this.capacity = capacity;
        }

        public String toString() { // Always good for debugging
            return "E" + id;
        }
    }
}
