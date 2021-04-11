package com.example.janusgraph;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.graphdb.database.StandardJanusGraph;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author akimov
 * created at 17.10.2020 12:52
 */
@Slf4j
public class JanusLocalExampleThreadedTransactions {
	
	/**
	 * https://tinkerpop.apache.org/docs/current/reference/#transactions
	 * https://docs.janusgraph.org/basics/transactions/
	 */
	public static void main(String[] args) throws Exception {
		String config = "2_janusgraph-example/src/main/java/com/example/janusgraph/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(config);
		JanusGraph janusGraph = JanusGraphFactory.open(conf);
		GraphTraversalSource g = janusGraph.traversal();
		
		// threaded transaction
		Graph threadedTx = g.tx().createThreadedTx(); // Threaded transactions are open when created and in manual mode
		GraphTraversalSource threadedTraversal = threadedTx.traversal();
		
		int threads = 10;
		val latch = new CountDownLatch(threads);
		
		// add vertex in main thread
		threadedTraversal.addV().property("thread", "main").next();
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		for (int i = 0; i < threads; i++) {
			executorService.submit(() -> {
				String threadName = Thread.currentThread().getName();
				var v = threadedTraversal.addV().property("thread", threadName).next();
				log.info("added: {}", v);
				latch.countDown();
			});
		}
		latch.await();
		executorService.shutdown();
		
		log.info("\ncheck count before commit: {}\n", g.V().count().next());
		threadedTx.tx().commit(); // this cannot be re-opened
		log.info("\ncheck count after commit: {}\n", g.V().count().next());
		
		log.info("is threaded transaction closed: " + !threadedTx.tx().isOpen());
		log.info("currently opened transactions: " + ((StandardJanusGraph) janusGraph).getOpenTransactions());
		
		JanusGraphFactory.drop(janusGraph);
		janusGraph.close();
	}
}
