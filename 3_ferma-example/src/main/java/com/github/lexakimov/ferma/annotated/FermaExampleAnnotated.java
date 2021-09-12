package com.github.lexakimov.ferma.annotated;

import com.github.lexakimov.ferma.annotated.domain.vertex.Person;
import com.github.lexakimov.ferma.annotated.domain.vertex.Programmer;
import com.github.lexakimov.ferma.annotated.domain.vertex.TeamLead;
import com.github.lexakimov.ferma.annotated.domain.vertex.Tester;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.core.JanusGraphFactory;

/**
 * @author akimov
 * created at 11.10.2020 12:34
 */
public class FermaExampleAnnotated {
	
	public static void main(String[] args) throws Exception {
//		Graph graph = TinkerGraph.open();
		Graph graph = JanusGraphFactory.open("inmemory");
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.github.lexakimov.ferma.annotated");
		
		Person alice = fg.addFramedVertex(Programmer.class);
		alice.setName("Alice");
		alice.setAge(26);
		
		Person bob = fg.addFramedVertex(TeamLead.class);
		bob.setName("Bob");
		bob.setAge(29);
		
		Person john = fg.addFramedVertex(Tester.class);
		john.setName("John");
		john.setAge(31);
		
		Person maggie = fg.addFramedVertex(Person.class);
		maggie.setName("Maggie");
		maggie.setAge(25);
		
		Person robert = fg.addFramedVertex(Person.class);
		robert.setName("Robert");
		robert.setAge(71);
		
		alice.worksWith(bob);
		alice.worksWith(john);
		bob.worksWith(john);
		
		alice.knows(maggie, 1);
		maggie.knows(alice, 1);
		
		alice.knows(robert, 26);
		bob.knows(robert, 26);
		john.knows(robert, 26);
		maggie.knows(robert, 26);
		
		
		System.out.println(fg.traverse(GraphTraversalSource::V).toList(Person.class));
		System.out.println();
		g.V().elementMap().forEachRemaining(System.out::println);
		System.out.println();
		g.E().elementMap().forEachRemaining(System.out::println);
		
		graph.close();
	}
}
