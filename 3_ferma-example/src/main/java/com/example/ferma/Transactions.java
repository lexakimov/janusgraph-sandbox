package com.example.ferma;

import com.example.ferma.annotated.Person;
import com.example.ferma.annotated.Programmer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.graphdb.database.StandardJanusGraph;

/**
 * @author akimov
 * created at 17.10.2020 8:39
 */
@Slf4j
public class Transactions {
	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(conf);
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.example.ferma.annotated");

		Transaction tx;
		tx = graph.tx();
//		tx = g.tx();
//		tx = fg.tx().getDelegate();

		// если вызвать tx.close() то всё это обнулится
		tx.onReadWrite(Transaction.READ_WRITE_BEHAVIOR.MANUAL);
		tx.onClose(Transaction.CLOSE_BEHAVIOR.MANUAL);
		tx.addTransactionListener(status -> {
			log.info("\u001b[42;1m\u001b[36mTransaction listener: {}\u001b[0m", status);
		});


		fg.tx().open();
		Person лёша = fg.addFramedVertex(Programmer.class);
		лёша.setName("Лёша");
		лёша.setAge(26);
		fg.tx().commit();

		fg.tx().open();
		Person саша = fg.addFramedVertex(Programmer.class);
		саша.setName("саша");
		саша.setAge(29);
		fg.tx().commit();

		fg.tx().open();
		лёша.worksWith(саша);
		fg.tx().commit();

		JanusGraphTransaction janusGraphTransaction1 = graph.newTransaction();
		JanusGraphTransaction janusGraphTransaction2 = graph.newThreadBoundTransaction();
//		graph.buildTransaction()
//		graph.closeTransaction();


		JanusGraphFactory.drop(graph);
		graph.close();
	}
}
