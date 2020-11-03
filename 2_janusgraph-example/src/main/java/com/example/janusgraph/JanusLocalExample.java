package com.example.janusgraph;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.util.Gremlin;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.attribute.Geo;
import org.janusgraph.core.attribute.Geoshape;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.example.GraphOfTheGodsFactory;

import java.util.List;
import java.util.Map;

public class JanusLocalExample {

	public static void main(String[] args) throws ConfigurationException, BackendException {
		System.out.println("JanusGraph " + JanusGraph.version() + ", Apache TinkerPop " + Gremlin.version());

		String configFileName = "2_janusgraph-example/src/main/java/com/example/janusgraph/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		// Graph graph = GraphFactory.open(configFileName);
		// Graph graph = GraphFactory.open(conf);
		// JanusGraph graph = JanusGraphFactory.open(configFileName);
		// graph = JanusGraphFactory.open('berkeleyje:/tmp/graph')

		JanusGraph janusGraph = JanusGraphFactory.open(conf);
		GraphTraversalSource g = janusGraph.traversal();

		GraphOfTheGodsFactory.load(janusGraph);

		Map<Object, Object> saturnProps = g.V().has("name", "saturn").valueMap(true).next();
		System.out.println(saturnProps.toString());

		List<Edge> places = g.E().has("place", Geo.geoWithin(Geoshape.circle(37.97, 23.72, 50))).toList();
		System.out.println(places.toString());

		JanusGraphFactory.drop(janusGraph);
		janusGraph.close();
	}
}
