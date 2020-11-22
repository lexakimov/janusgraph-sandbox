package com.example.tinkerpop;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * @author akimov
 * created at 10.10.2020 14:01
 */
public class TinkerpopLocalExample {

	public static void main(String[] args) throws Exception {
		String configFileName = "configs/local-tinkergraph.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		// Open with GraphFactory
//		Graph graph = GraphFactory.open(conf);
//		Graph graph = GraphFactory.open(configFileName);

//		TinkerGraph graph = TinkerGraph.open();
		TinkerGraph graph = TinkerGraph.open(conf);

		// populate
		//TinkerFactory.generateModern(graph);
		TinkerFactory.generateTheCrew(graph);

		GraphTraversalSource g = graph.traversal();
		System.out.println(g);
		System.out.println(graph);
		System.out.println(graph.features());
		System.out.println(graph.variables().asMap());

		GraphUtils.printAll(g);

		graph.close();
	}
}
