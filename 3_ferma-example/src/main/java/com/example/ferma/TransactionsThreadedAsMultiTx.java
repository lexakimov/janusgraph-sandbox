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
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.example.ferma.annotated");
		
		WrappedFramedGraph<?> graphTx1 = getThreadedTx(fg);
		WrappedFramedGraph<?> graphTx2 = getThreadedTx(fg);
		WrappedFramedGraph<?> graphTx3 = getThreadedTx(fg);

//		graphTx1.tx().open();
		Person man1 = graphTx1.addFramedVertex(Programmer.class);
		man1.setName("вася");
		man1.setAge(26);

//		graphTx2.tx().open();
		Person man2 = graphTx2.addFramedVertex(Programmer.class);
		man2.setName("петя");
		man2.setAge(29);

//		graphTx3.tx().open();
		Person man3 = graphTx3.addFramedVertex(Programmer.class);
		man3.setName("саша");
		man3.setAge(29);

//		GraphUtils.printAll(fg.getRawTraversal()); // now fg is empty

//		лёша.worksWith(саша); //IllegalStateException: The vertex or type is not associated with this transaction [v[4328]]
		
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
		
		// если вызвать tx.close() то всё это обнулится
//		threadedFg.tx().getDelegate().onReadWrite(Transaction.READ_WRITE_BEHAVIOR.MANUAL);
//		threadedFg.tx().getDelegate().onClose(Transaction.CLOSE_BEHAVIOR.MANUAL);
		threadedFg.tx().getDelegate().addTransactionListener(status -> {
			log.info("\u001b[42;1m\u001b[36mTransaction listener: {}\u001b[0m", status);
		});
		
		return threadedFg;
	}
	
}
