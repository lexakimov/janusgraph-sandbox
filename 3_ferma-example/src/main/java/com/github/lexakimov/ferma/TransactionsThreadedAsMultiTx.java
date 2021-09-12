package com.github.lexakimov.ferma;

import com.github.lexakimov.ferma.annotated.domain.vertex.Person;
import com.github.lexakimov.ferma.annotated.domain.vertex.Programmer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.WrappedFramedGraph;
import com.syncleus.ferma.WrappedTransaction;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.graphdb.database.StandardJanusGraph;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author akimov
 * created at 17.10.2020 8:39
 */
@Slf4j
public class TransactionsThreadedAsMultiTx {
	
	static ExecutorService executor = Executors.newFixedThreadPool(10);
	
	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);
		
		StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.open(conf);
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.github.lexakimov.ferma.annotated");
		
		WrappedFramedGraph<?> graphTx1 = getThreadedTx(fg);
		WrappedFramedGraph<?> graphTx2 = getThreadedTx(fg);
		WrappedFramedGraph<?> graphTx3 = getThreadedTx(fg);

//		graphTx1.tx().open();
		Person rick = graphTx1.addFramedVertex(Programmer.class);
		rick.setName("Rick");
		rick.setAge(26);

//		graphTx2.tx().open();
		Person max = graphTx2.addFramedVertex(Programmer.class);
		max.setName("Max");
		max.setAge(29);

//		graphTx3.tx().open();
		Person paul = graphTx3.addFramedVertex(Programmer.class);
		paul.setName("Paul");
		paul.setAge(29);

//		GraphUtils.printAll(fg.getRawTraversal()); // now fg is empty

//		rick.worksWith(max); //IllegalStateException: The vertex or type is not associated with this transaction [v[4328]]
		
		graphTx1.tx().commit(); // after that data from graphTx1 will be able from graphTx2 and graphTx3
		GraphUtils.printAll(fg.getRawTraversal());  // has  data from graphTx1
		
		graphTx2.tx().commit(); // after that data from graphTx2 will be able from graphTx3
		GraphUtils.printAll(fg.getRawTraversal()); // has  data from graphTx1 and graphTx2
		
		graphTx3.tx().rollback(); // rollback
		
		executor.shutdown();
		JanusGraphFactory.drop(graph);
		graph.close();
	}
	
	/**
	 * Transactions that can be shared across multiple threads.
	 */
	private static WrappedFramedGraph getThreadedTx(FramedGraph fg) {
		WrappedTransaction fgTx = fg.tx();
		WrappedFramedGraph threadedFg = fgTx.createThreadedTx();
		
		// all that setup will reset if tx.close() will be invoked
//		threadedFg.tx().getDelegate().onReadWrite(Transaction.READ_WRITE_BEHAVIOR.MANUAL);
//		threadedFg.tx().getDelegate().onClose(Transaction.CLOSE_BEHAVIOR.MANUAL);
		threadedFg.tx().getDelegate().addTransactionListener(status -> {
			log.info("\u001b[42;1m\u001b[36mTransaction listener: {}\u001b[0m", status);
		});
		
		return threadedFg;
	}
	
}
