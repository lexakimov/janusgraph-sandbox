package com.github.lexakimov.janusgraph;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Transaction.CLOSE_BEHAVIOR;
import org.apache.tinkerpop.gremlin.structure.Transaction.READ_WRITE_BEHAVIOR;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.schema.JanusGraphManagement;

/**
 * @author akimov
 * created at 17.10.2020 12:52
 */
@Slf4j
public class JanusLocalExampleTransactions {
	
	/**
	 * https://tinkerpop.apache.org/docs/current/reference/#transactions
	 * https://docs.janusgraph.org/basics/transactions/
	 */
	public static void main(String[] args) throws Exception {
		String config = "2_janusgraph-example/src/main/java/com/example/janusgraph/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(config);
		JanusGraph janusGraph = JanusGraphFactory.open(conf);
		GraphTraversalSource g = janusGraph.traversal();
		
		// each management creates it's own tx
		JanusGraphManagement mgmt1 = janusGraph.openManagement();
		JanusGraphManagement mgmt2 = janusGraph.openManagement();
		mgmt1.commit();
		mgmt2.rollback();
		
		
		// thread-bound transaction
		Transaction tx = g.tx()
				.onReadWrite(READ_WRITE_BEHAVIOR.MANUAL)
				.onClose(CLOSE_BEHAVIOR.MANUAL);
		
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
		System.out.println(g.V().values("name").toList());
		tx.commit();
		
		tx.open();
		log.info("\ncheck count after commit: {}\n", g.V().count().next());
		tx.commit();
		
		JanusGraphFactory.drop(janusGraph);
		janusGraph.close();
	}
}
