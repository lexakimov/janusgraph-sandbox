package com.example.tinkerpop;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.List;

/**
 * @author akimov
 * created at 11.10.2020 12:41
 */
public class GraphUtils {
	public static void printAll(GraphTraversalSource g) {
		List<? extends Element> vertices = g.V().toList();
		_iter("vertices:", vertices);

		System.out.println();
		List<? extends Element> edges = g.E().toList();
		_iter("edges:", edges);
	}

	private static void _iter(String label, List<? extends Element> vertices) {
		System.out.println(label);
		for (Element e : vertices) {
			System.out.printf("%4s | %8s |", e.id(), e.label());
			e.properties().forEachRemaining(p -> {
				if (p.key().equals("ferma_type")) {
					System.out.printf(" %10s : %-40s |", p.key(), p.value());
				} else {
					System.out.printf(" %8s : %10s |", p.key(), p.value());
				}
			});
			System.out.printf("%30s", e);
			System.out.println();
		}
	}
}
