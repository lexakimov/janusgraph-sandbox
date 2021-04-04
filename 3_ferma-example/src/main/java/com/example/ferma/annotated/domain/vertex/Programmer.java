package com.example.ferma.annotated.domain.vertex;

import com.syncleus.ferma.annotations.GraphElement;
import com.syncleus.ferma.annotations.Property;

/**
 * @author akimov
 * created at 11.10.2020 12:34
 */
@GraphElement
public abstract class Programmer extends Person {
	
	public enum Language {
		Java, Python, Go, Cpp, JavaScript
	}
	
	@Property("language")
	public abstract Language getLanguage();
	
	@Property("language")
	public abstract void setLanguage(Language language);
	
}