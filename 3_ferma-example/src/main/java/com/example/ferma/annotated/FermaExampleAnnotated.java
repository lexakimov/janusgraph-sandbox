package com.example.ferma.annotated;

import com.syncleus.ferma.ClassInitializer;
import com.syncleus.ferma.DefaultClassInitializer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * @author akimov
 * created at 11.10.2020 12:34
 */
public class FermaExampleAnnotated {

	public static void main(String[] args) throws Exception {
		Graph graph = TinkerGraph.open();
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.example.ferma.annotated");

		Person лёша = fg.addFramedVertex(Programmer.class);
		лёша.setName("Лёша");
		лёша.setAge(26);

		Person саша = fg.addFramedVertex(Programmer.class);
		саша.setName("саша");
		саша.setAge(29);

		Person тима = fg.addFramedVertex(Tester.class);
		тима.setName("тима");
		тима.setAge(31);

		Person лена = fg.addFramedVertex(Person.class);
		лена.setName("лена");
		лена.setAge(25);

		Person путин = fg.addFramedVertex(Person.class);
		путин.setName("путин");
		путин.setAge(71);

//		лёша.worksWith(саша, "Shortest Track");
//		лёша.worksWith(тима, "Shortest Track");
//		саша.worksWith(тима, "Shortest Track");

		лёша.worksWith(саша);
		лёша.worksWith(тима);
		саша.worksWith(тима);

		лёша.knows(лена, 1);
		лена.knows(лёша, 1);

		лёша.knows(путин, 26);
		саша.knows(путин, 26);
		тима.knows(путин, 26);
		лена.knows(путин, 26);


//		public List<? extends Person> getFriendsNamedBill() {
//			return this.traverse(input -> input.out("knows").has("name", "bill")).toList(Person.class);
//		}

		ClassInitializer<Knows> edgeClassInitializer = new DefaultClassInitializer<Knows>(Knows.class) {
			@Override
			public void initalize(Knows frame) {
				frame.setYears(8888);
			}
		};

//		лёша.addKnows(саша, classInitializer);

//		Person julia = fg.traverse((g1) -> g1.V().has("name", juliaName)).next(Person.class);
//		Person jeff = julia.getKnowsPeople().get(0);
//		System.out.println(julia);
//		System.out.println(jeff);


		System.out.println();
		System.out.println(fg.traverse(GraphTraversalSource::V).toList(Person.class));

		System.out.println();
		g.E().toList().forEach(System.out::println);

		graph.close();
	}
}
