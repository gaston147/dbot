package fr.gaston147.dbot.config;

import java.io.IOException;
import java.io.Writer;

public class Config {
	private ConfigNodeMap root;
	
	public Config(ConfigNodeMap root) {
		this.root = root;
	}
	
	public String get(String key) {
		return ((ConfigNodeStr) root.get(key)).str;
	}
	
	public boolean exists(String key) {
		return root.exists(key);
	}
	
	public void save(Writer w) throws IOException {
		w.write("# Generated config file\n");
		w.write("\n");
		root.save(w);
	}
}
