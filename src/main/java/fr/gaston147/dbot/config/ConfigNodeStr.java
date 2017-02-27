package fr.gaston147.dbot.config;

public class ConfigNodeStr extends ConfigNode {
	public String str;
	
	public ConfigNodeStr(String str) {
		this.str = str;
	}
	
	public String toString() {
		return "\"" + str + "\"";
	}
}
