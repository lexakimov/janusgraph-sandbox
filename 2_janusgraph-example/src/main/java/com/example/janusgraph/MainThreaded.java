package com.example.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.graphdb.database.StandardJanusGraph;

/**
 * @author akimov
 * created at: 15.10.2020 16:30
 */
public class MainThreaded {

    public static void main(String[] args) throws InterruptedException {
        StandardJanusGraph graph = (StandardJanusGraph) JanusGraphFactory.build().set("storage.backend", "inmemory").open();
        GraphTraversalSource g = graph.traversal();

        // этот граф можно юзать одновременно несколькими потоками
        Graph multiThreadGraph = graph.tx().createThreadedTx();

        Runnable job = () -> {
            try {
                Thread.sleep(1500);
                System.out.println(multiThreadGraph);

                Vertex vertex = multiThreadGraph.addVertex();
                vertex.property("name" + Thread.currentThread().getId(), "juno-" + Thread.currentThread().getName());

                Thread.sleep(1500);
                System.out.println(graph.getOpenTransactions().size());


            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        };

        Thread thread0 = new Thread(job);
        Thread thread1 = new Thread(job);
        Thread thread2 = new Thread(job);
        Thread thread3 = new Thread(job);

        thread0.start();
        thread1.start();
        thread2.start();
        thread3.start();

        thread0.join();
        thread1.join();
        thread2.join();
        thread3.join();


        print(g);
        System.out.println();

        // изменения в основном графе будут только после коммита
        multiThreadGraph.tx().commit();
        print(g);

        g.V().drop();
        g.tx().commit();

        assert graph.getOpenTransactions().size() == 0;
    }

    public static void print(GraphTraversalSource g) {
        System.out.println("------");
        for (Vertex vertex : g.V().toList()) {
            VertexProperty<Object> next = vertex.properties().next();
            System.out.println(next.key() + " : " + next.value());
        }
        System.out.println("------");
    }
}
