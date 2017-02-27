package fr.gaston147.dbot.config;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class ConfigNodeMap extends ConfigNode {
	private Map<String, ConfigNode> m;
	
	public ConfigNodeMap() {
		m = new HashMap<String, ConfigNode>();
	}
	
	public ConfigNodeMap(Map<String, ConfigNode> m) {
		this.m = m;
	}
	
	public ConfigNode get(String key) {
		return m.get(key);
	}
	
	public ConfigNodeMap put(String key, ConfigNode val) {
		m.put(key, val);
		return this;
	}
	
	public boolean exists(String key) {
		return m.containsKey(key);
	}
	
	public void save(Writer w, String indent) throws IOException {
		for (String key : m.keySet())
			w.write(key + ": " + m.get(key).toString() + "\n");
	}
	
	public void save(Writer w) throws IOException {
		save(w, "");
	}
}
