package com.github.lexakimov.janusgraph;

import com.github.lexakimov.utils.GraphUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.graphdb.database.StandardJanusGraph;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author akimov
 * created at: 15.10.2020 16:30
 */
@Slf4j
public class JanusLocalExampleTransactionsThreaded {

	static ExecutorService executorService = Executors.newCachedThreadPool();

	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
//		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open("inmemory");
		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(configFileName);
		GraphTraversalSource g = graph.traversal();

		GraphUtils.printAll(g);

		// этот граф можно юзать одновременно несколькими потоками
		Graph threadedGraph = graph.tx().createThreadedTx();
		GraphTraversalSource threadedG = threadedGraph.traversal();

		Callable<String> task = () -> {
			Thread cThread = Thread.currentThread();
			String threadName = cThread.getName();
			long id = cThread.getId();
			log.info("task {} started", threadName);
			Thread.sleep(1500);

			for (int i = 0; i < 5; i++) {
				threadedGraph.addVertex("name" + id, "josh " + i + " from thread " + id);
			}

			Thread.sleep(1500);

			return threadName + " executed! opened tx: " + graph.getOpenTransactions().size();
		};

		List<Callable<String>> objects = IntStream.range(0, 10).mapToObj(i -> task).collect(Collectors.toList());

		List<Future<String>> futures = executorService.invokeAll(objects);
		for (Future<String> future : futures) {
			log.info("{}", future.get());
		}

		GraphUtils.printAll(g);
		System.out.println();

		// изменения в основном графе будут только после коммита
		threadedG.tx().commit();
		GraphUtils.printAll(g);

		log.info(String.valueOf(graph.getOpenTransactions().size()));

		executorService.shutdown();
		graph.close();
	}

}
