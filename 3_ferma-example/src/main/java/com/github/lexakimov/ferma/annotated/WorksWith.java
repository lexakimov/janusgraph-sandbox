package com.github.lexakimov.ferma.annotated;

import com.github.lexakimov.ferma.annotated.domain.vertex.Person;
import com.syncleus.ferma.AbstractEdgeFrame;
import com.syncleus.ferma.annotations.GraphElement;
import com.syncleus.ferma.annotations.InVertex;
import com.syncleus.ferma.annotations.OutVertex;
import com.syncleus.ferma.annotations.Property;

/**
 * @author akimov
 * created at 11.10.2020 12:35
 */
@GraphElement
public abstract class WorksWith extends AbstractEdgeFrame {
	
	public static final String EDGE_LABEL = "works_with";
	
	@Property("desc")
	public abstract void setDesc(String desc);
	
	@Property("desc")
	public abstract String getDesc();
	
	@InVertex
	public abstract Person getIn();
	
	@OutVertex
	public abstract Person getOut();
}