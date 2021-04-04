package com.example.ferma.simple;

import com.syncleus.ferma.AbstractVertexFrame;

import java.util.List;
import java.util.UUID;

/**
 * untyped
 *
 * @author akimov
 * created at: 08.10.2020 16:47
 */
public class Person extends AbstractVertexFrame {
	
	public UUID getName() {
		return getProperty("name");
	}
	
	public void setName(UUID name) {
		setProperty("name", name);
	}
	
	public Knows addKnows(Person friend) {
		return addFramedEdge("knows", friend, Knows.class);
	}
	
	public List<? extends Knows> getKnowsList() {
		return traverse((v) -> v.outE("knows")).toList(Knows.class);
	}
}