package com.example.ferma.annotated;

import com.syncleus.ferma.AbstractVertexFrame;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.GraphElement;
import com.syncleus.ferma.annotations.Property;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;

/**
 * @author akimov
 * created at 11.10.2020 12:34
 */
@GraphElement
public abstract class Person extends AbstractVertexFrame {

	@Property("name")
	public abstract String getName();

	@Property("name")
	public abstract void setName(String name);

	@Property("age")
	public abstract int getAge();

	@Property("age")
	public abstract void setAge(int age);


	@Adjacency(label = Knows.EDGE_LABEL, direction = Direction.IN, operation = Adjacency.Operation.ADD)
//	@Incidence(label = Knows.EDGE_LABEL, operation = Incidence.Operation.ADD)
	public abstract Person addKnows(Person friend);
//	public abstract Person addKnows(Person friend, ClassInitializer<Knows> classInitializer);

	@Adjacency(label = Knows.EDGE_LABEL, direction = Direction.IN, operation = Adjacency.Operation.GET)
//	@Incidence(label = Knows.EDGE_LABEL, operation = Incidence.Operation.GET)
	public abstract List<Knows> getKnows();

	@Adjacency(label = Knows.EDGE_LABEL, direction = Direction.IN, operation = Adjacency.Operation.GET)
	public abstract List<Person> getKnowsPeople();

	@Adjacency(label = WorksWith.EDGE_LABEL, direction = Direction.BOTH, operation = Adjacency.Operation.SET)
	public abstract void worksWith(Person person);

//	public void worksWith(Person person, String desc) {
//		WorksWith tEdge = person.setLinkBoth(WorksWith.EDGE_LABEL, this, WorksWith.class);
////		WorksWith tEdge = person.addFramedEdge(WorksWith.EDGE_LABEL, this, WorksWith.class);
//		tEdge.setDesc(desc);
//	}

	public void knows(Person person, int years) {
		Knows tEdge = person.addFramedEdge(Knows.EDGE_LABEL, this, Knows.class);
		tEdge.setYears(years);
	}
}