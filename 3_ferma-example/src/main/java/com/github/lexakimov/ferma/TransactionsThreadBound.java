package com.github.lexakimov.ferma;

import com.github.lexakimov.ferma.annotated.domain.vertex.Person;
import com.github.lexakimov.ferma.annotated.domain.vertex.Programmer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.graphdb.database.StandardJanusGraph;

/**
 * @author akimov
 * created at 17.10.2020 8:39
 */
@Slf4j
public class TransactionsThreadBound {
	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);
		
		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(conf);
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.github.lexakimov.ferma.annotated");
		
		Transaction tx = graph.tx(); // tx = g.tx(); tx = fg.tx().getDelegate();
		
		// если вызвать tx.close() то всё это обнулится
		tx.onReadWrite(Transaction.READ_WRITE_BEHAVIOR.MANUAL);
		tx.onClose(Transaction.CLOSE_BEHAVIOR.MANUAL);
		tx.addTransactionListener(status -> {
			log.info("\u001b[42;1m\u001b[36mTransaction listener: {}\u001b[0m", status);
		});
		
		fg.tx().open();
		Person alice = fg.addFramedVertex(Programmer.class);
		alice.setName("Alice");
		alice.setAge(26);
		fg.tx().commit();
		
		fg.tx().open();
		Person bob = fg.addFramedVertex(Programmer.class);
		bob.setName("Bob");
		bob.setAge(29);
		fg.tx().commit();
		
		fg.tx().open();
		alice.worksWith(bob);
		fg.tx().commit();
		
		fg.tx().open();
		GraphUtils.printAll(fg.getRawTraversal());
		fg.tx().commit();
		
		JanusGraphFactory.drop(graph);
		graph.close();
	}
}
