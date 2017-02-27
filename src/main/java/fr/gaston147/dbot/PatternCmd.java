package fr.gaston147.dbot;

import java.util.regex.Pattern;

public class PatternCmd {
	public final String name;
	public final Pattern p;
	public final String v;
	
	public PatternCmd(String name, Pattern p, String v) {
		this.name = name;
		this.p = p;
		this.v = v;
	}
}
