package com.example.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.graphdb.database.StandardJanusGraph;

/**
 * @author akimov
 * created at: 15.10.2020 9:00
 */
public class Main {

    public static void main(String[] args) throws Exception {
        StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.build().set("storage.backend", "inmemory").open();
        GraphTraversalSource g = graph.traversal();
        //LogProcessorFramework logProcessorFramework = JanusGraphFactory.openTransactionLog(graph);

        JanusGraphVertex juno = graph.addVertex(); //Automatically opens a new transaction

        juno.property("name", "juno");
        graph.tx().commit(); //Ends transaction

        juno.property("age", 26); //Vertex is automatically transitioned
        graph.tx().commit();


        juno.property("weight", 85); //Vertex is automatically transitioned
        graph.tx().commit();


        // If the transaction fails, a JanusGraphException is thrown.
        // обернуть коммит в try catch

        Edge e = juno.addEdge("knows", graph.addVertex("name", "lex"));
        graph.tx().commit(); //Ends transaction

        e = g.E(e).next(); //Need to refresh edge
        e.property("time", 99);
        graph.tx().commit();


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
        //Transaction gTx = g.tx();
        tx.open();
        tx.commit();
        tx.rollback();
        tx.readWrite();
        tx.close();
        Graph threadedTx = tx.createThreadedTx();

//        FramedGraph fg = new DelegatingFramedGraph<>(graph);
//        WrappedTransaction fgTx = fg.tx();
//        fgTx.open();
//        fgTx.commit();
//        fgTx.rollback();
//        fgTx.readWrite();
//        fgTx.close();
//        WrappedFramedGraph<?> threadedTx1 = fgTx.createThreadedTx();

        graph.close();
    }
}
