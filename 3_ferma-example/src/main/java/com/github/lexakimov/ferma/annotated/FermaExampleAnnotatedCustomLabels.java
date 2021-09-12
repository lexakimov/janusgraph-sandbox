package com.github.lexakimov.ferma.annotated;

import com.github.lexakimov.ferma.annotated.domain.vertex.Person;
import com.github.lexakimov.ferma.annotated.domain.vertex.Programmer;
import com.github.lexakimov.ferma.annotated.domain.vertex.TeamLead;
import com.github.lexakimov.ferma.annotated.domain.vertex.Tester;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.janusgraph.core.JanusGraphFactory;

/**
 * By default, Syncleus Ferma sets the label of vertex to "vertex".
 * This example shows how you can change this behaviour.
 *
 * @author akimov
 * created at 11.10.2020 12:34
 */
public class FermaExampleAnnotatedCustomLabels {
	
	public static void main(String[] args) throws Exception {
//		Graph graph = TinkerGraph.open();
		Graph graph = JanusGraphFactory.open("inmemory");
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.github.lexakimov.ferma.annotated");
		
		Person alice = fg.addFramedVertex(Programmer.class, T.label, "programmer");
		alice.setName("Alice");
		alice.setAge(26);
		
		Person bob = fg.addFramedVertex(TeamLead.class, T.label, "team_lead");
		bob.setName("Bob");
		bob.setAge(29);
		
		Person john = fg.addFramedVertex(Tester.class, T.label, "tester");
		john.setName("John");
		john.setAge(31);
		
		Person maggie = fg.addFramedVertex(Person.class, T.label, "person");
		maggie.setName("Maggie");
		maggie.setAge(25);
		
		Person robert = fg.addFramedVertex(Person.class, T.label, "person");
		robert.setName("Robert");
		robert.setAge(71);
		
		g.V().elementMap().forEachRemaining(System.out::println);
		/*
		output is like:
		{id=4168, label=team_lead, ferma_type=com.github.lexakimov.ferma.annotated.domain.vertex.TeamLead, name=Bob, age=29}
		{id=4304, label=person, ferma_type=com.github.lexakimov.ferma.annotated.domain.vertex.Person, name=Robert, age=71}
		{id=4152, label=programmer, ferma_type=com.github.lexakimov.ferma.annotated.domain.vertex.Programmer, name=Alice, age=26}
		{id=4248, label=tester, ferma_type=com.github.lexakimov.ferma.annotated.domain.vertex.Tester, name=John, age=31}
		{id=4216, label=person, ferma_type=com.github.lexakimov.ferma.annotated.domain.vertex.Person, name=Maggie, age=25}
		 */
		
		graph.close();
	}
}
