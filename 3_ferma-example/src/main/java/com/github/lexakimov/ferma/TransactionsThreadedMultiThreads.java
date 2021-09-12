package com.github.lexakimov.ferma;

import com.github.lexakimov.ferma.annotated.domain.vertex.Programmer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.graphdb.database.StandardJanusGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author akimov
 * created at 17.10.2020 8:39
 */
@Slf4j
public class TransactionsThreadedMultiThreads {
	
	static ExecutorService executor = Executors.newFixedThreadPool(10);
	
	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);
		
		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(conf);
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.github.lexakimov.ferma.annotated");

//		exec(fg);
		exec(fg.tx().createThreadedTx()); // org.janusgraph.core.SchemaViolationException: Adding this property for
		// key [~T$SchemaName] and value [rtferma_type] violates a uniqueness constraint [SystemIndex#~T$SchemaName]
		
		GraphUtils.printAll(fg.getRawTraversal());
		
		executor.shutdown();
		JanusGraphFactory.drop(graph);
		graph.close();
	}
	
	@SneakyThrows
	private static void exec(FramedGraph graph) {
		Runnable task = () -> {
			Programmer programmer = graph.addFramedVertex(Programmer.class);
			programmer.setName("thread " + Thread.currentThread().getName());
			GraphUtils.printAll(graph.getRawTraversal());
			graph.tx().commit();
		};
		
		Set<Future<?>> executions = new HashSet<>();
		
		for (int i = 0; i < 10; i++) {
			executions.add(executor.submit(task));
		}
		
		for (Future<?> future : executions) {
			future.get();
		}
		
		graph.tx().commit();
	}
	
}
