package com.github.lexakimov.janusgraph;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author akimov
 * created at 09.10.2020 23:23
 */
public class JanusRemoteExample {
	
	public static void main(String[] args) throws Exception {
		// ------  bytecode
//		DriverRemoteConnection remoteConnection = DriverRemoteConnection.using("localhost", 8182, "g");
//		GraphTraversalSource g = AnonymousTraversalSource.traversal().withRemote(remoteConnection);
		
		String configFileName = "configs/remote-graph.properties";
		GraphTraversalSource g = AnonymousTraversalSource.traversal().withRemote(configFileName);
		
		//Transaction tx = g.tx();
		
		Vertex v1 = g.addV("person").property("name", "marko").next();
		System.out.println(v1);
		
		//tx.open();
		
		Vertex v2 = g.addV("person").property("name", "stephen").next();
		System.out.println(v2);
		
		Edge edge = g.V(v1).addE("knows").to(v2).property("weight", 0.75).next();
		System.out.println(edge);
		
		//tx.rollback();
		
		Vertex marko = g.V().has("person", "name", "marko").next();
		List<Vertex> peopleMarkoKnows = g.V().has("person", "name", "marko").out("knows").toList();
		System.out.println(marko);
		System.out.println(peopleMarkoKnows);
		
		g.close();
		
		// ------  script (with driver)
		Configuration conf = new PropertiesConfiguration(configFileName);
		Cluster cluster = Cluster.open(conf.getString("gremlin.remote.driver.clusterFile"));
		Client client = cluster.connect();
		
		Client sessionClient = cluster.connect("session123", true);
		sessionClient.close();
		
		Map<String, Object> params = new HashMap<>();
		params.put("name", "marko");
		List<Result> list = client.submit("g.V().has('person','name',name).out('knows')", params).all().get();
		System.out.println(list);
		
		client.close();
		cluster.close();
	}
}
