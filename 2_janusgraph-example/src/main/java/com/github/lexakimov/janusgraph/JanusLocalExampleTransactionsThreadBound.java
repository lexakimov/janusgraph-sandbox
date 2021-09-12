package com.github.lexakimov.janusgraph;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.TransactionBuilder;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.graphdb.database.StandardJanusGraph;

import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * @author akimov
 * created at 17.10.2020 12:52
 */
@Slf4j
public class JanusLocalExampleTransactionsThreadBound {

	static Consumer<Transaction.Status> txListener = status -> {
		log.info("\u001b[42;1m\u001b[36mTransaction listener: {}\u001b[0m", status);
	};

	/**
	 * https://tinkerpop.apache.org/docs/current/reference/#transactions
	 * https://docs.janusgraph.org/basics/transactions/
	 */
	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		JanusGraph graph = JanusGraphFactory.open(conf);
		StandardJanusGraph sjg = (StandardJanusGraph) graph;
		GraphTraversalSource g = graph.traversal();

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		Transaction tx = g.tx();

		// если вызвать tx.close() то всё это обнулится
		tx.onReadWrite(Transaction.READ_WRITE_BEHAVIOR.MANUAL);
		tx.onClose(Transaction.CLOSE_BEHAVIOR.MANUAL);
		tx.addTransactionListener(txListener);

		tx.open(); // opens new thread-bound tx. invoke of StandardJanusGraph.newThreadBoundTransaction()
		g.V().drop().toList();
		tx.commit();

		tx.open();
		g.addV("person").property("name", "stephen").next();
		tx.commit();

		tx.open();
		g.addV("person").property("name", "bill").next();
		tx.commit();

		tx.open();
		log.info("{}", g.V().values("name").toList());    // stephen, bill
		g.addV("person").property("name", "john").next();
		log.info("{}", g.V().values("name").toList());    // stephen, bill, john
		tx.rollback();

		tx.open();
		log.info("{}", g.V().values("name").toList()); // stephen, bill
		tx.commit();

		tx.open();
		JanusGraphTransaction jgTx1 = sjg.getCurrentThreadTx(); // it's thread-bound tx
		g.addV("person").property("name", "peter").next();

		JanusGraphTransaction jgTx2 = sjg.newTransaction();     // non thread-bound tx. StandardJanusGraph.newThreadBoundTransaction()
		jgTx2.addVertex("person").property("name", "thomas").element();

		JanusGraphTransaction jgTx3 = sjg.newThreadBoundTransaction();
		jgTx3.addVertex("person").property("name", "james").element();

		log.info("{}", g.V().values("name").toList());
		jgTx1.commit(); // it affects to jgTx2 and jgTx3

		log.info("{}", stream(jgTx2.getVertices().spliterator(), false)
				.map(v -> v.value("name"))
				.collect(toList()));

		log.info("{}", stream(jgTx3.getVertices().spliterator(), false)
				.map(v -> v.value("name"))
				.collect(toList()));

		jgTx2.commit();
		jgTx3.commit();

		tx.open();
		log.info("{}", g.V().values("name").toList());
		tx.commit();

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// each management creates it's own tx
		JanusGraphManagement mgmt1 = graph.openManagement(); // disableBatchLoading()
		JanusGraphManagement mgmt2 = graph.openManagement();
		mgmt1.commit();
		mgmt2.rollback();

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// builder
		TransactionBuilder transactionBuilder = graph
				.buildTransaction()
				.consistencyChecks(true)
				.checkExternalVertexExistence(true)
				.checkInternalVertexExistence(true);

		JanusGraphTransaction buildedTx1 = transactionBuilder.start();
		buildedTx1.tx().addTransactionListener(txListener);
		buildedTx1.addVertex("new");

		JanusGraphTransaction buildedTx2 = transactionBuilder.start();
		buildedTx2.tx().addTransactionListener(txListener);
		buildedTx2.addVertex("new");

		JanusGraphTransaction buildedTx3 = transactionBuilder.start();
		buildedTx3.tx().addTransactionListener(txListener);
		buildedTx3.addVertex("new");

		// BUG? - listeners dont invoke
		buildedTx1.tx().commit();
		buildedTx2.tx().commit();
		buildedTx3.tx().commit();

		
//		JanusGraphFactory.drop(graph);
		graph.close();
	}
}
