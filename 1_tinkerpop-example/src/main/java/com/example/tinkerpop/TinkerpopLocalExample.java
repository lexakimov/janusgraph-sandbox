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

		String configFileName = "1_tinkerpop-example/src/main/java/com/example/tinkerpop/local-tinkergraph.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		// Open with GraphFactory
//		Graph graph = GraphFactory.open(conf);
//		Graph graph = GraphFactory.open(configFileName);
//		System.out.println(graph);
//		System.out.println(graph.features());
//		System.out.println(graph.variables());
//		graph.close();

//		TinkerGraph tinkerGraph = TinkerGraph.open();
		TinkerGraph tinkerGraph = TinkerGraph.open(conf);
//		System.out.println(tinkerGraph.features());

		//TinkerFactory.generateModern(tinkerGraph);
		TinkerFactory.generateTheCrew(tinkerGraph);
		System.out.println(tinkerGraph);
//		System.out.println(tinkerGraph.variables().asMap());

		GraphTraversalSource g = tinkerGraph.traversal();
		System.out.println(g);

		System.out.println(g.V().toList());
		System.out.println(g.V().has("name").values("name").toList());
		/*List<Map<Object, Object>> vertices = g.V().valueMap().toList();
		vertices.forEach(m -> {
			MapUtils.debugPrint(System.out, null, m);
			System.out.println("━━━━━━━━━━━━━━━━━━━");
		});*/
		
		/*
		System.out.println("━━━━━━━━━ tx ━━━━━━━━━");
		System.out.println(g.V().toList());
		g.addV("label_1").property("key1", 233);
		System.out.println(g.V().toList());

		Transaction tx = tinkerGraph.tx();
		// graph.tx().createThreadedTx();
		tx.open();

		g.addV("label_2").property("key1", 233);
		System.out.println(g.V().toList());

		tx.rollback();
		System.out.println(g.V().toList());
		*/
		tinkerGraph.close();
	}
}
