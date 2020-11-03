package com.example.janusgraph;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Transaction.CLOSE_BEHAVIOR;
import org.apache.tinkerpop.gremlin.structure.Transaction.READ_WRITE_BEHAVIOR;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.TransactionBuilder;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.graphdb.database.StandardJanusGraph;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * @author akimov
 * created at 17.10.2020 12:52
 */
public class JanusLocalExampleTransactions {

	/**
	 * https://tinkerpop.apache.org/docs/current/reference/#transactions
	 * https://docs.janusgraph.org/basics/transactions/
	 */
	public static void main(String[] args) throws InterruptedException, ConfigurationException, BackendException, ExecutionException {
		String configFileName = "2_janusgraph-example/src/main/java/com/example/janusgraph/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);
		JanusGraph janusGraph = JanusGraphFactory.open(conf);
		StandardJanusGraph sjg = (StandardJanusGraph) janusGraph;
		GraphTraversalSource g = janusGraph.traversal();

		Transaction tx = g.tx()
				.onReadWrite(READ_WRITE_BEHAVIOR.MANUAL)
				.onClose(CLOSE_BEHAVIOR.MANUAL);

		tx.open();
		g.V().drop().toList();
		tx.commit();
//		tx.close(); // not necessary

		tx.open();
		g.addV("person").property("name", "stephen").next();
		tx.commit();

		tx.open();
		g.addV("person").property("name", "bill").next();
		tx.commit();

		tx.open();
		System.out.println(g.V().values("name").toList());    // stephen, bill
		g.addV("person").property("name", "john").next();
		System.out.println(g.V().values("name").toList());    // stephen, bill, john
		tx.rollback();

		tx.open();
		System.out.println(g.V().values("name").toList()); // stephen, bill
		tx.commit();


		tx.open();
		JanusGraphTransaction jgTx1 = sjg.getCurrentThreadTx();
		g.addV("person").property("name", "peter").next();

//		JanusGraphTransaction jgTx2New = sjg.newTransaction();
//		JanusGraphTransaction jgTx3Tb = sjg.newThreadBoundTransaction();

		JanusGraphTransaction jgTx2 = sjg.newTransaction();
		jgTx2.addVertex("person").property("name", "thomas").element();

		JanusGraphTransaction jgTx3 = sjg.newThreadBoundTransaction();
		jgTx3.addVertex("person").property("name", "james").element();

//		jgTx1.getVertices().forEach(v -> System.out.println((String) v.value("name")));
		System.out.println(g.V().values("name").toList());
		jgTx1.commit(); // it affects to jgTx2 and jgTx3

		System.out.println(stream(jgTx2.getVertices().spliterator(), false).map(v -> v.value("name")).collect(toList()));

		System.out.println(stream(jgTx3.getVertices().spliterator(), false).map(v -> v.value("name")).collect(toList()));

		jgTx2.commit();
		jgTx3.commit();

		tx.open();
		System.out.println(g.V().values("name").toList());
		tx.commit();

		// each management creates it's own tx
		JanusGraphManagement mgmt1 = janusGraph.openManagement();
		JanusGraphManagement mgmt2 = janusGraph.openManagement();
		mgmt1.commit();
		mgmt2.rollback();

		TransactionBuilder transactionBuilder = janusGraph
				.buildTransaction()
				.consistencyChecks(true)
				.checkExternalVertexExistence(true)
				.checkInternalVertexExistence(true);

		JanusGraphTransaction buildedTx1 = transactionBuilder.start();
		Transaction tx1 = buildedTx1.tx();

		JanusGraphTransaction buildedTx2 = transactionBuilder.start();
		Transaction tx2 = buildedTx2.tx();

		JanusGraphTransaction buildedTx3 = transactionBuilder.start();
		Transaction tx3 = buildedTx3.tx();

		tx1.commit();
		tx2.commit();
		tx3.commit();

		//mgmt.changeName(property, "new name");

		Graph threadedTx = tx.createThreadedTx();
		GraphTraversalSource threadedTraversal = threadedTx.traversal();

		threadedTraversal.addV("person").property("name", "stephen2").next();

		Callable<String> task = () -> {
			for (int i = 0; i < 5; i++) {
				threadedTraversal.addV("person").property("name", "josh " + i + " from thread " + Thread.currentThread().getId()).next();
			}
			return Thread.currentThread().getName() + " executed!";
		};

		ExecutorService executorService = Executors.newCachedThreadPool();
		List<Future<String>> futures = executorService.invokeAll(Arrays.asList(task, task, task, task, task));
		for (Future<String> future : futures) {
			System.out.println(future.get());
		}

		threadedTx.tx().commit();

		tx.open();
		System.out.println(g.V().<String>values("name").toList().stream().sorted().collect(Collectors.toList()));
		tx.commit();


		tx.open();
		g.V().drop().toList();
		tx.commit();
		
		JanusGraphFactory.drop(janusGraph);
		janusGraph.close();
	}
}
