package com.github.lexakimov.ferma.annotated;

import com.github.lexakimov.ferma.annotated.domain.vertex.Person;
import com.github.lexakimov.ferma.annotated.domain.vertex.Programmer;
import com.github.lexakimov.ferma.annotated.domain.vertex.TeamLead;
import com.github.lexakimov.ferma.annotated.domain.vertex.Tester;
import com.syncleus.ferma.ClassInitializer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.framefactories.FrameFactory;
import com.syncleus.ferma.typeresolvers.TypeResolver;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.janusgraph.core.JanusGraphFactory;

import java.util.Arrays;
import java.util.Collection;

/**
 * REMEMBER: vertex or edge label are immutable (sets while creation).
 *
 * @author akimov
 * created at 11.10.2020 12:34
 */
public class FermaExampleAnnotatedCustomTypeResolver {
	
	static class CustomDelegatingFramedGraph<G extends Graph> extends DelegatingFramedGraph<G> {

		public CustomDelegatingFramedGraph(G delegate, FrameFactory builder, TypeResolver defaultResolver) {
			super(delegate, builder, defaultResolver);
		}

		public CustomDelegatingFramedGraph(G delegate) {
			super(delegate);
		}

		public CustomDelegatingFramedGraph(G delegate, TypeResolver defaultResolver) {
			super(delegate, defaultResolver);
		}

		public CustomDelegatingFramedGraph(G delegate, boolean typeResolution, boolean annotationsSupported) {
			super(delegate, typeResolution, annotationsSupported);
		}

		public CustomDelegatingFramedGraph(G delegate, ReflectionCache reflections, boolean typeResolution, boolean annotationsSupported) {
			super(delegate, reflections, typeResolution, annotationsSupported);
		}

		public CustomDelegatingFramedGraph(G delegate, Collection<? extends Class<?>> types) {
			super(delegate, types);
		}

		public CustomDelegatingFramedGraph(G delegate, String modelPackage) {
			super(delegate, modelPackage);
		}

		public CustomDelegatingFramedGraph(G delegate, boolean typeResolution, Collection<? extends Class<?>> types) {
			super(delegate, typeResolution, types);
		}

		@Override
		public <TYPE> TYPE addFramedVertex(ClassInitializer<TYPE> initializer, Object... keyValues) {
			Class<TYPE> type = initializer.getInitializationType();
			
			if (keyValues != null) {
				if (Arrays.asList(keyValues).contains(T.label)) {
					return frameNewElement(this.getBaseGraph().addVertex(keyValues), initializer);
				} else {
					Object[] keyValuesNew = new Object[keyValues.length + 1];
					keyValuesNew[0] = T.label;
					keyValuesNew[1] = type.getSimpleName().toLowerCase();

					System.arraycopy(keyValues, 0, keyValuesNew, 2, keyValues.length);

					return frameNewElement(this.getBaseGraph().addVertex(keyValuesNew), initializer);
				}
			} else {
				return frameNewElement(this.getBaseGraph().addVertex(type.getSimpleName().toLowerCase()), initializer);
			}
		}
	}
	
	/**
	 * Allows to resolve java type from vertex label.
	 */
	static class CustomTypeResolver implements TypeResolver {
		
		@Override
		public <T> Class<? extends T> resolve(Element element, Class<T> kind) {
			return null;
		}
		
		@Override
		public Class<?> resolve(Element element) {
			String label = element.label();
			return null;
		}
		
		@Override
		public void init(Element element, Class<?> kind) {
			// no property set
		}
		
		@Override
		public void deinit(Element element) {
		
		}
		
		@Override
		public <P extends Element, T extends Element> GraphTraversal<P, T> hasType(GraphTraversal<P, T> traverser, Class<?> type) {
			return null;
		}
		
		@Override
		public <P extends Element, T extends Element> GraphTraversal<P, T> hasNotType(GraphTraversal<P, T> traverser, Class<?> type) {
			return null;
		}
	}
	
	/**
	 * By default, Syncleus Ferma sets the label of vertex to "vertex".
	 * This example shows how you can change this behaviour.
	 */
	public static void main(String[] args) throws Exception {
//		Graph graph = TinkerGraph.open();
		Graph graph = JanusGraphFactory.open("inmemory");
		GraphTraversalSource g = graph.traversal();
		FramedGraph fg = new DelegatingFramedGraph<>(graph, "com.github.lexakimov.ferma.annotated");
		
		Person alice = fg.addFramedVertex(Programmer.class);
		alice.setName("Alice");
		alice.setAge(26);
		
		Person bob = fg.addFramedVertex(TeamLead.class);
		bob.setName("Bob");
		bob.setAge(29);
		
		Person john = fg.addFramedVertex(Tester.class);
		john.setName("John");
		john.setAge(31);
		
		Person maggie = fg.addFramedVertex(Person.class);
		maggie.setName("Maggie");
		maggie.setAge(25);
		
		Person robert = fg.addFramedVertex(Person.class);
		robert.setName("Robert");
		robert.setAge(71);
		
		g.V().elementMap().forEachRemaining(System.out::println);
		
		graph.close();
	}
}
