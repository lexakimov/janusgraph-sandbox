package com.example.ferma;

import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.WrappedFramedGraph;
import com.syncleus.ferma.WrappedTransaction;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.graphdb.database.StandardJanusGraph;

/**
 * @author akimov
 * created at 17.10.2020 8:39
 */
public class Transactions {
	public static void main(String[] args) throws ConfigurationException {
		String configFileName = "3_ferma-example/src/main/resources/local-inmemory.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);
//		Graph graph = TinkerGraph.open();
		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(conf);

		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph);


		JanusGraphTransaction currentThreadTx;
		currentThreadTx = graph.getCurrentThreadTx();
		currentThreadTx.commit();
		currentThreadTx = graph.getCurrentThreadTx();
		currentThreadTx.rollback();
		currentThreadTx = graph.getCurrentThreadTx();
		currentThreadTx.commit();
		currentThreadTx.close();
//		currentThreadTx.hasModifications();

		currentThreadTx = graph.getCurrentThreadTx();

		JanusGraphTransaction janusGraphTransaction = graph.newThreadBoundTransaction();
		JanusGraphTransaction janusGraphTransaction1 = graph.newTransaction();
//		graph.buildTransaction()
//		graph.closeTransaction();

		Transaction tx = graph.tx();
		Transaction gTx = g.tx();
		tx.open();
		tx.commit();
		tx.rollback();
		tx.readWrite();
		tx.close();
		Graph threadedTx = tx.createThreadedTx();


		WrappedTransaction fgTx = fg.tx();
		fgTx.open();
		fgTx.commit();
		fgTx.rollback();
		fgTx.readWrite();
		fgTx.close();
		WrappedFramedGraph<?> threadedTx1 = fgTx.createThreadedTx();
	}
}
