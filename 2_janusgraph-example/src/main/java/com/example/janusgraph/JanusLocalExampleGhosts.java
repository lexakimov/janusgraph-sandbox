package com.example.janusgraph;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.keycolumnvalue.scan.ScanMetrics;
import org.janusgraph.diskstorage.keycolumnvalue.scan.StandardScanner;
import org.janusgraph.graphdb.database.StandardJanusGraph;
import org.janusgraph.graphdb.olap.job.GhostVertexRemover;

/**
 * @author akimov
 * created at 22.11.2020 17:12
 */
@Slf4j
public class JanusLocalExampleGhosts {
	@SneakyThrows
	public static void main(String[] args) {
		String configFileName = "configs/local-berkeleyje-lucene.properties";
		Configuration conf = new PropertiesConfiguration(configFileName);

		JanusGraph graph = JanusGraphFactory.open(conf);
		StandardJanusGraph sjg = (StandardJanusGraph) graph;
		GraphTraversalSource g = graph.traversal();

		StandardScanner.Builder builder = sjg.getBackend().buildEdgeScanJob();

		builder.setJobId("remove_ghosts_1");
		builder.setJob(new GhostVertexRemover(graph));
		JanusGraphManagement.IndexJobFuture execute = builder.execute();

		ScanMetrics scanMetrics = execute.get();

		log.info(String.valueOf(scanMetrics));

	}
}
