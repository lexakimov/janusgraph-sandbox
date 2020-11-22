package com.example.ferma.simple;

import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.core.JanusGraphFactory;

import java.util.UUID;

/**
 * @author akimov
 * created at: 08.10.2020 16:46
 */
public class FermaExample {

	public static void main(String[] args) throws Exception {
//		Graph graph = TinkerGraph.open();
		Graph graph = JanusGraphFactory.open("inmemory");

		// Simple mode
//		FramedGraph fg = new DelegatingFramedGraph<>(graph);

		// Typed mode
		FramedGraph fg = new DelegatingFramedGraph<>(graph, true, false);

		UUID n1 = UUID.randomUUID();
		UUID n2 = UUID.randomUUID();

		Person p1 = fg.addFramedVertex(Person.class);
		p1.setName(n1);

		Programmer p2 = fg.addFramedVertex(Programmer.class);
		p2.setName(n2);
		p2.setLanguage(Programmer.Language.Python);

		Knows knows = p1.addKnows(p2);
		knows.setYears(15);

		p1 = fg.traverse((dd) -> dd.V().has("name", n1)).next(Person.class);
		p2 = fg.traverse((dd) -> dd.V().has("name", n2)).next(Programmer.class);
		System.out.println(p1);
		System.out.println(p2);

		System.out.println();
		System.out.println(fg.traverse(GraphTraversalSource::V).toList(Person.class));

		System.out.println();
		System.out.println("p1 knows:");
		System.out.println(p1.getKnowsList());

		graph.close();
	}

}
