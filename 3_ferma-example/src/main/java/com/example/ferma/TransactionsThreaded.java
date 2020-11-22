package com.example.ferma;

import com.example.ferma.annotated.Person;
import com.example.ferma.annotated.Programmer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.WrappedFramedGraph;
import com.syncleus.ferma.WrappedTransaction;
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
public class TransactionsThreaded {
	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(conf);
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.example.ferma.annotated");

		WrappedFramedGraph<?> graphTx1 = threaded(fg);
		WrappedFramedGraph<?> graphTx2 = threaded(fg);
		WrappedFramedGraph<?> graphTx3 = threaded(fg);

		graphTx1.tx().open();
		Person лёша = graphTx1.addFramedVertex(Programmer.class);
		лёша.setName("Лёша");
		лёша.setAge(26);

		graphTx2.tx().open();
		Person саша = graphTx2.addFramedVertex(Programmer.class);
		саша.setName("саша");
		саша.setAge(29);

		graphTx3.tx().open();
		лёша.worksWith(саша);


		graphTx1.tx().commit();
		graphTx2.tx().commit();
		graphTx3.tx().commit();


		JanusGraphFactory.drop(graph);
		graph.close();
	}

	private static WrappedFramedGraph threaded(FramedGraph fg) {
		// Multi-Threaded Transactions
		WrappedTransaction fgTx = fg.tx();
		WrappedFramedGraph threadedFg = fgTx.createThreadedTx();

		// елси вызвать tx.close() то всё это обнулится
		threadedFg.tx().getDelegate().onReadWrite(Transaction.READ_WRITE_BEHAVIOR.MANUAL);
		threadedFg.tx().getDelegate().onClose(Transaction.CLOSE_BEHAVIOR.MANUAL);
		threadedFg.tx().getDelegate().addTransactionListener(status -> {
			log.info("\u001b[42;1m\u001b[36mTransaction listener: {}\u001b[0m", status);
		});
		return threadedFg;
	}
}
