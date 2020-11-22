package com.example.janusgraph;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.*;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.core.schema.SchemaStatus;
import org.janusgraph.diskstorage.keycolumnvalue.scan.ScanMetrics;
import org.janusgraph.graphdb.database.StandardJanusGraph;
import org.janusgraph.graphdb.database.management.GraphIndexStatusReport;
import org.janusgraph.graphdb.database.management.GraphIndexStatusWatcher;
import org.janusgraph.graphdb.database.management.ManagementSystem;

import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.janusgraph.core.schema.SchemaStatus.*;

/**
 * GRAPH INDEXES EXAMPLES:
 *
 * JanusGraph distinguishes between two types of graph indexes: composite and mixed indexes.
 * <p>
 * Composite indexes are very fast and efficient but limited to equality lookups for a particular, previously-defined
 * combination of property keys.
 * <p>
 * Mixed indexes can be used for lookups on any combination of indexed keys and support multiple condition predicates
 * in addition to equality depending on the backing index store.
 */
@Slf4j
public class JanusLocalExampleIndex {

	public static void main(String[] args) throws Exception {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		JanusGraph janusGraph = JanusGraphFactory.open(conf);
		StandardJanusGraph sjg = (StandardJanusGraph) janusGraph;
		GraphTraversalSource g = janusGraph.traversal();

		// create schema
		JanusGraphManagement mgmt = janusGraph.openManagement();

		if (!mgmt.containsPropertyKey("name")) {
			mgmt.makePropertyKey("name").dataType(String.class).make();
		}

		if (!mgmt.containsPropertyKey("age")) {
			mgmt.makePropertyKey("age").dataType(Long.class).make();
		}

		if (!mgmt.containsPropertyKey("years")) {
			mgmt.makePropertyKey("years").dataType(Integer.class).make();
		}

		if (!mgmt.containsEdgeLabel("knows")) {
			mgmt.makeEdgeLabel("knows").directed().multiplicity(Multiplicity.ONE2MANY).make();
		}

		Iterable<PropertyKey> relationTypes = mgmt.getRelationTypes(PropertyKey.class);
		log.info(StreamSupport
				.stream(relationTypes.spliterator(), false)
				.collect(Collectors.toList()).toString()
		);


		// if not commit, indexes will be enabled simultaneously
		mgmt.commit();


		mgmt = janusGraph.openManagement();

		// ---------------------------------------------------------------------------------------------------
		// Composite indexes retrieve vertices or edges by one or a (fixed) composition of multiple keys.
		// A composite index may comprise just one or multiple keys.
		// A composite index with just one key is sometimes referred to as a key-index.

		// Composite indexes can also be used to enforce property uniqueness in the graph.
		// If a composite graph index is defined as unique() there can be at most one vertex or edge for any given
		// concatenation of property values associated with the keys of that index.



		if (!mgmt.containsGraphIndex("vName")) {
			mgmt.buildIndex("vName", Vertex.class)
//					.indexOnly(mgmt.getVertexLabel("god"))
//					.unique()
					.addKey(mgmt.getPropertyKey("name"))
					.buildCompositeIndex();
		}

		if (!mgmt.containsGraphIndex("vNameAge")) {
			mgmt.buildIndex("vNameAge", Vertex.class)
					.addKey(mgmt.getPropertyKey("name"))
					.addKey(mgmt.getPropertyKey("age"))
					.buildCompositeIndex();
		}

		EdgeLabel knows = mgmt.getEdgeLabel("knows");
		if (!mgmt.containsRelationIndex(knows,"eKnowsYears")) {
			mgmt.buildEdgeIndex(
					knows,
					"eKnowsYears",
					Direction.BOTH,
					Order.desc,
					mgmt.getPropertyKey("years")
			);
		}

//		mgmt.buildPropertyIndex(propertyKey, "idxName");


		//		mgmt.changeName();
		//		mgmt.getConsistency();
		//		janusGraph.indexQuery()
		//      mgmt.addIndexKey();

		//		mgmt.getTTL();


		// ---------------------------------------------------------------------------------------------------
		// Mixed indexes retrieve vertices or edges by any combination of previously added property keys.
		// Mixed indexes provide more flexibility than composite indexes and support additional condition predicates
		// beyond equality.
		// On the other hand, mixed indexes are slower for most equality queries than composite indexes.

/*
		if (!mgmt.containsGraphIndex("vNameAgeMixed")) {
			mgmt.buildIndex("vNameAgeMixed", Vertex.class)
					.addKey(mgmt.getPropertyKey("name"),
							Mapping.STRING.asParameter(),
							Parameter.of("string-analyzer", "org.apache.lucene.analysis.core.SimpleAnalyzer")
					)
					.addKey(mgmt.getPropertyKey("age"))
					.buildMixedIndex("search"); // from property index.[search].backend - which backend to use
		}
*/

		mgmt.commit();

		// ---------------------------------------------------------------------------------------------------
		// indexing process
		// https://docs.janusgraph.org/index-management/index-reindexing/
		// query.force-index

		// Never create new indexes while a transaction is active
		//janusGraph.tx().rollback();

		// Wait for the index to become available

		mgmt = janusGraph.openManagement();
		System.out.println(mgmt.printIndexes());

		Set<String> idxNames = new HashSet<>();


		indexes:
		for (JanusGraphIndex graphIndex : mgmt.getGraphIndexes(Vertex.class)) {
			PropertyKey[] fieldKeys = graphIndex.getFieldKeys();
			for (PropertyKey propertyKey : fieldKeys) {
				SchemaStatus status = graphIndex.getIndexStatus(propertyKey);
				switch (status) {
					case DISABLED:
						// doesn't work
						mgmt.updateIndex(graphIndex, SchemaAction.REMOVE_INDEX).get();
						continue indexes;
					case INSTALLED:
						mgmt.updateIndex(graphIndex, SchemaAction.REGISTER_INDEX).get();
						idxNames.add(graphIndex.name());
						log.info("registration: " + graphIndex.name());
						continue indexes;
					case ENABLED:
						idxNames.add(graphIndex.name());
						continue indexes;

				}
			}
		}

		mgmt.commit();

		// wait until registered or enabled

		for (String graphIndex : idxNames) {
			GraphIndexStatusWatcher graphIndexStatusWatcher =
					ManagementSystem
							.awaitGraphIndexStatus(janusGraph, graphIndex)
							.status(REGISTERED, ENABLED, DISABLED)
							.pollInterval(1, ChronoUnit.SECONDS);
			GraphIndexStatusReport call = graphIndexStatusWatcher.call();
			log.info("{}", call);
		}


		// Reindex the existing data
		mgmt = janusGraph.openManagement();
		System.out.println(mgmt.printIndexes());

		for (JanusGraphIndex graphIndex : mgmt.getGraphIndexes(Vertex.class)) {
			if (!idxNames.contains(graphIndex.name())){
				continue;
			}
			mgmt.updateIndex(graphIndex, SchemaAction.REINDEX).get();
			JanusGraphManagement.IndexJobFuture indexJobStatus = mgmt.getIndexJobStatus(graphIndex);
			ScanMetrics scanMetrics = indexJobStatus.get();
			log.info("{}", scanMetrics);
		}

		log.info(mgmt.printIndexes());
		mgmt.commit();

		for (String graphIndex : idxNames) {
			GraphIndexStatusWatcher graphIndexStatusWatcher =
					ManagementSystem
							.awaitGraphIndexStatus(janusGraph, graphIndex)
							.status(ENABLED);
			GraphIndexStatusReport call = graphIndexStatusWatcher.call();
			log.info("{}", call);
		}

		mgmt = janusGraph.openManagement();
		log.info(mgmt.printIndexes());
		mgmt.commit();

		// ---------------------------------------------------------------------------------------------------
		// add content
		for (int i = 0; i < 1000; i++) {
			g.addV("new_vertex").property("name", "alice", "age", 25 + i).next();
			g.addV("new_vertex").property("name", "bob", "age", 25 + i).next();
			if (i % 1000 == 0) {
				g.tx().commit();
			}
		}
		g.tx().commit();

		// composite graph indexes can only be used for equality constraints
		long l1 = System.currentTimeMillis();
		List<Vertex> result1 = g.V().has("name", "bob").toList();
		log.info("{}", System.currentTimeMillis() - l1);

		l1 = System.currentTimeMillis();
		List<Vertex> result2 = g.V().has("name", "bob").has("age", 25).toList();
		log.info("{}", System.currentTimeMillis() - l1);

		l1 = System.currentTimeMillis();
		List<Vertex> result3 = g.V().has("age", 25).has("name", "bob").toList();
		log.info("{}", System.currentTimeMillis() - l1);

		g.tx().commit();

		mgmt = janusGraph.openManagement();
		log.info(mgmt.printSchema());

		ScanMetrics vName =
				mgmt.updateIndex(mgmt.getGraphIndex("vName"), SchemaAction.DISABLE_INDEX).get();

		log.info(mgmt.printSchema());
		mgmt.commit();


		ManagementSystem
				.awaitGraphIndexStatus(janusGraph, "vName")
				.status(DISABLED)
				.pollInterval(1, ChronoUnit.SECONDS)
				.call();


		// Delete the index using JanusGraphManagement
		mgmt = janusGraph.openManagement();
		JanusGraphIndex nameIndex = mgmt.getGraphIndex("vName");
		JanusGraphManagement.IndexJobFuture future = mgmt.updateIndex(nameIndex, SchemaAction.REMOVE_INDEX);
		mgmt.commit();
		janusGraph.tx().commit();
		future.get();

/*		// Delete the index using MapReduceIndexJobs
		mgmt = janusGraph.openManagement();
		MapReduceIndexManagement mr = new MapReduceIndexManagement(janusGraph);
		future = mr.updateIndex(m.getGraphIndex('name'), SchemaAction.REMOVE_INDEX)
		m.commit()
		graph.tx().commit()
		future.get()

		// Index still shows up in management interface as DISABLED -- this is normal
		m = graph.openManagement()
		idx = m.getGraphIndex('name')
		idx.getIndexStatus(m.getPropertyKey('name'))
		m.rollback()*/

/*		mgmt = janusGraph.openManagement();
		mgmt.changeName(mgmt.getGraphIndex("vName"), "NEW_NAME");
		log.info(mgmt.printIndexes());
		mgmt.commit();*/

		mgmt = janusGraph.openManagement();
		log.info(mgmt.printIndexes());

		g.V().has("name", "jupiter");

/*		new GhostVertexRemover(janusGraph).
		StandardScanner.Builder builder = ((StandardJanusGraph) janusGraph).getBackend().buildEdgeScanJob();
		builder.setFinishJob(indexId.getIndexJobFinisher(graph, SchemaAction.ENABLE_INDEX));
		builder.setJobId(indexId);
		builder.setJob(GhostVertexRemover.  VertexJobConverter.convert(graph, new IndexRepairJob(indexId.indexName, indexId.relationTypeName)));
		JanusGraphManagement.IndexJobFuture future = builder.execute();
		future*/



		// When using order().by() it is important to note that:
		//
		//   Composite graph indexes do not natively support ordering search results.
		//   All results will be retrieved and then sorted in-memory. For large result sets, this can be very expensive.
		//
		//   Mixed indexes support ordering natively and efficiently.
		//   However, the property key used in the order().by() method must have been previously added to the mixed
		//   indexed for native result ordering support. This is important in cases where the the order().by() key is
		//   different from the query keys. If the property key is not part of the index, then sorting requires loading
		//   all results into memory.


		/*
		Composite versus Mixed Indexes:

		1. Use a composite index for exact match index retrievals. Composite indexes do not require configuring or
		operating an external index system and are often significantly faster than mixed indexes.
			1.1. As an exception, use a mixed index for exact matches when the number of distinct values for query constraint
			is relatively small or if one value is expected to be associated with many elements in the graph (i.e. in
			case of low selectivity).

		2. Use a mixed indexes for numeric range, full-text or geo-spatial indexing. Also, using a mixed index can
		speed up the order().by() queries.
		 */

		//JanusGraphFactory.drop(janusGraph);
		janusGraph.close();
	}
}
