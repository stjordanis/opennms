/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.graph.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.context.DefaultGraphContext;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.info.GraphInfo;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class AbstractGraph<V extends Vertex, E extends Edge> implements Graph<V, E> {

    private final DirectedSparseGraph<VertexRef, E> jungGraph = new DirectedSparseGraph<>();
    private final Map<String, V> vertexToIdMap = new HashMap<>();
    private final Map<String, E> edgeToIdMap = new HashMap<>();
//    private final Map<NodeRef, V> nodeRefToVertexMap = new HashMap<>();

    // A calculation of the focus
    private Focus focusStrategy;
    protected GraphInfo<V> graphInfo;

    public AbstractGraph() {

    }

    public AbstractGraph(GraphInfo<V> graphInfo) {
        this.graphInfo = Objects.requireNonNull(graphInfo);
    }

    @Override
    public List<V> getVertices() {
        // TODO MVR use junggraph.getVetices instead. However addEdge is adding the edges if not in same namespace
        // We have to figure out a workaround for that somehow
        return new ArrayList<>(vertexToIdMap.values());
    }

    @Override
    public List<E> getEdges() {
        // TODO MVR use junggraph.getEdges instead. However addEdge is adding the edges if not in same namespace
        // We have to figure out a workaround for that somehow
        return new ArrayList<>(edgeToIdMap.values());
    }

    @Override
    public String getNamespace() {
        return graphInfo.getNamespace();
    }

    @Override
    public String getDescription() {
        return graphInfo.getDescription();
    }

    @Override
    public String getLabel() {
        return graphInfo.getLabel();
    }

    @Override
    public Class<V> getVertexType() {
        return graphInfo.getVertexType();
    }

    @Override
    public List<Vertex> getDefaultFocus() {
        if (focusStrategy != null) {
            return focusStrategy.getFocus(new DefaultGraphContext(this)).stream().map(vr -> vertexToIdMap.get(vr.getId())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

//    @Override
//    public Vertex getVertex(NodeRef nodeRef) {
//        return nodeRefToVertexMap.get(nodeRef);
//    }

    public void setFocusStrategy(Focus focusStrategy) {
        this.focusStrategy = focusStrategy; // TODO MVR verify persistence of this
    }

    @Override
    public void addEdges(Collection<E> edges) {
        for (E eachEdge : edges) {
            addEdge(eachEdge);
        }
    }

    @Override
    public void addVertices(Collection<V> vertices) {
        for (V eachVertex : vertices) {
            addVertex(eachVertex);
        }
    }

    @Override
    public void addVertex(V vertex) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(vertex.getId());
        if (jungGraph.containsVertex(vertex)) return; // already added
        jungGraph.addVertex(vertex);
        vertexToIdMap.put(vertex.getId(), vertex);

//        if (vertex.getNodeRef() != null) {
//            // TODO MVR implement me
////            nodeRefToVertexMap.put(vertex.getNodeRef(), vertex);
//        }
    }

    @Override
    public void addEdge(E edge) {
        Objects.requireNonNull(edge);
        Objects.requireNonNull(edge.getId());
        if (jungGraph.containsEdge(edge)) return; // already added
        if (edge.getSource().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getSource().getId()) == null) {
            addVertex((V) edge.getSource());
        }
        if (edge.getTarget().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getTarget().getId()) == null) {
            addVertex((V) edge.getTarget());
        }
        jungGraph.addEdge(edge, edge.getSource(), edge.getTarget());
        edgeToIdMap.put(edge.getId(), edge);
    }

    @Override
    public void removeEdge(E edge) {
        Objects.requireNonNull(edge);
        jungGraph.removeEdge(edge);
        edgeToIdMap.remove(edge.getId());
    }

    @Override
    public void removeVertex(V vertex) {
        Objects.requireNonNull(vertex);
        jungGraph.removeVertex(vertex);
        vertexToIdMap.remove(vertex.getId());
    }

    @Override
    public V getVertex(String id) {
        return vertexToIdMap.get(id);
    }

    @Override
    public E getEdge(String id) {
        return edgeToIdMap.get(id);
    }

    @Override
    public List<String> getVertexIds() {
        return vertexToIdMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<String> getEdgeIds() {
        return edgeToIdMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<V> resolveVertices(Collection<String> vertexIds) {
        final List<V> collect = vertexIds.stream().map(vid -> vertexToIdMap.get(vid)).filter(v -> v != null).collect(Collectors.toList());
        return collect;
    }

    public List<V> resolveVertexRefs(Collection<VertexRef> vertexRefs) {
        // Determine all vertexId for all vertices with the same namespace as the current graph
        List<String> vertexIds = vertexRefs.stream()
                .filter(ref -> getNamespace().equalsIgnoreCase(ref.getNamespace()))
                .map(ref -> ref.getId())
                .collect(Collectors.toList());
        return resolveVertices(vertexIds);
    }

    @Override
    public List<E> resolveEdges(Collection<String> vertexIds) {
        final List<E> collect = vertexIds.stream().map(eid -> edgeToIdMap.get(eid)).collect(Collectors.toList());
        return collect;
    }

    @Override
    public Collection<V> getNeighbors(V eachVertex) {
        return resolveVertexRefs(jungGraph.getNeighbors(eachVertex));
    }

    @Override
    public Collection<E> getConnectingEdges(V eachVertex) {
        final Set<E> edges = new HashSet<>();
        edges.addAll(jungGraph.getInEdges(eachVertex));
        edges.addAll(jungGraph.getOutEdges(eachVertex));
        return edges;
    }

    @Override
    public Graph<V, E> getSnapshot(Collection<V> verticesInFocus, int szl) {
        // TODO MVR implement me
//        return new SemanticZoomLevelTransformer(verticesInFocus, szl).transform(this, () -> {
//            final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> snapshotGraph = new SimpleGraph<>(getNamespace());
//            applyInfo(SimpleGraph.this);
//            return snapshotGraph;
//        });
        return null;
    }

    @Override
    public GenericGraph asGenericGraph() {
        final GenericGraph graph = new GenericGraph();
        graph.setProperty(GenericProperties.NAMESPACE, getNamespace());
        graph.setProperty(GenericProperties.LABEL, getLabel());
        graph.setProperty(GenericProperties.DESCRIPTION, getDescription());
        getVertices().stream().map(Vertex::asGenericVertex).forEach(graph::addVertex);
        getEdges().stream().map(Edge::asGenericEdge).forEach(graph::addEdge);
        return graph;
    }

    // TODO MVR
//    @Override
//    public String toString() {
//        return asGenericGraph().toString();
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGraph<?, ?> that = (AbstractGraph<?, ?>) o;
        return Objects.equals(getLabel(), that.getLabel()) // TODO MVR this needs a bit more fine-tuning
                && Objects.equals(getNamespace(), that.getNamespace())
                && Objects.equals(getDescription(), that.getDescription())
                && Objects.equals(getDefaultFocus(), that.getDefaultFocus())
                && Objects.equals(getVertices(), that.getVertices())
                && Objects.equals(getEdges(), that.getEdges());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabel(), getNamespace(), getDescription(), getDefaultFocus(), getVertices(), getEdges());
    }
}

