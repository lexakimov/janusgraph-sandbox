package com.example.janusgraph;

import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.util.Gremlin;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.attribute.Geo;
import org.janusgraph.core.attribute.Geoshape;
import org.janusgraph.example.GraphOfTheGodsFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class JanusLocalExample {

	public static void main(String[] args) {
		log.info("JanusGraph " + JanusGraph.version() + ", Apache TinkerPop " + Gremlin.version());

		JanusGraph graph = JanusGraphFactory.open("inmemory");
		GraphTraversalSource g = graph.traversal();

		GraphOfTheGodsFactory.load(graph);

		Map<Object, Object> saturnProps = g.V().has("name", "saturn").valueMap(true).next();
		log.info(saturnProps.toString());

		List<Edge> places = g.E().has("place", Geo.geoWithin(Geoshape.circle(37.97, 23.72, 50))).toList();
		log.info(places.toString());

		graph.close();
	}
}