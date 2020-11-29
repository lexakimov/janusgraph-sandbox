package com.example.ferma;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author akimov
 * created at 11.10.2020 12:41
 */
public class GraphUtils {
	public static void printAll(GraphTraversalSource g) {
		StringBuilder sb = new StringBuilder();
		sb.append(Thread.currentThread().getName());
		sb.append(" +++ \n");
		
		List<? extends Element> vertices = g.V().toList().stream().sorted().collect(Collectors.toList());
		_iter("vertices:", vertices, sb);
		
		sb.append("\n");
		List<? extends Element> edges = g.E().toList();
		_iter("edges:", edges, sb);
		
		System.out.println(sb);
		
	}
	
	private static void _iter(String label, List<? extends Element> vertices, StringBuilder sb) {
		if (vertices.isEmpty()) {
			return;
		}
		sb.append(label);
		sb.append("[").append(vertices.size()).append("]\n");
		for (Element e : vertices) {
			sb.append(String.format("%12s | %8s |", e.id(), e.label()));
			e.properties().forEachRemaining(p -> {
				if (p.key().equals("ferma_type")) {
					sb.append(String.format(" %10s : %-40s |", p.key(), p.value()));
				} else {
					sb.append(String.format(" %8s : %10s |", p.key(), p.value()));
				}
			});
			sb.append(String.format("%30s", e));
			sb.append("\n");
		}
	}
}
