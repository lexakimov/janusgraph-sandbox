package com.example.ferma.simple;

/**
 * @author akimov
 * created at 11.10.2020 12:33
 */
public class Programmer extends Person {

	public enum Language {
		Java, Python, Go, Cpp, JavaScript
	}

	public Language getLanguage() {
		return getProperty("language");
	}

	public void setLanguage(Language language) {
		setProperty("language", language);
	}

}
