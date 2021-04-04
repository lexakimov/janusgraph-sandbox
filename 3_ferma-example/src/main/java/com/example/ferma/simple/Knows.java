package com.example.ferma.simple;

import com.syncleus.ferma.AbstractEdgeFrame;

/**
 * @author akimov
 * created at: 08.10.2020 19:01
 */
public class Knows extends AbstractEdgeFrame {
	public void setYears(int years) {
		setProperty("years", years);
	}
	
	public int getYears() {
		return getProperty("years");
	}
}