package fr.gaston147.dbot.config;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import fr.gaston147.dbot.Parser;
import fr.gaston147.dbot.ParserException;

public class ConfigParser extends Parser<Config> {
	
	public Config parse(Reader r) throws ParserException {
		try {
			super.parse(r);
			ConfigNodeMap nodeMap = new ConfigNodeMap(readMap());
			if (!out())
				throw new ConfigParserException("Unexpected character \"" + c + "\" at " + (ind + 1) + ".");
			return new Config(nodeMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ConfigNode readVal() throws IOException, ConfigParserException {
		if (out())
			return null;
		if (c == '"') {
			nxt();
			StringBuilder sb = new StringBuilder();
			while (!out() && c != '"') {
				sb.append(c);
				nxt();
			}
			if (out())
				throw new ConfigParserException("Premature end of file.");
			nxt();
			return new ConfigNodeStr(new String(sb));
		} else {
			String word = readWrd();
			if (word.length() == 0)
				return null;
			return new ConfigNodeStr(new String(word));
		}
	}
	
	public Map<String, ConfigNode> readMap() throws IOException, ConfigParserException {
		Map<String, ConfigNode> m = new HashMap<String, ConfigNode>();
		while (!out()) {
			blk();
			if (!out() && c == '#') {
				nxt();
				while (!out() && c != '\n')
					nxt();
				continue;
			}
			String key = readWrd();
			if (key.length() == 0)
				break;
			blk();
			if (out())
				throw new ConfigParserException("Premature end of file.");
			if (c == ':') {
				nxt();
				blk();
				ConfigNode val = readVal();
				if (val == null)
					throw new ConfigParserException("Value expected at " + (ind + 1) + ".");
				m.put(key, val);
			} else if (c == '{') {
				nxt();
				blk();
				ConfigNodeMap map = new ConfigNodeMap(readMap());
				if (out())
					throw new ConfigParserException("Premature end of file.");
				if (c != '}')
					throw new ConfigParserException("Unexpected character \"" + c + "\" at " + (ind + 1) + ".");
				nxt();
				m.put(key, map);
			} else {
				throw new ConfigParserException("Unexpected character \"" + c + "\" at " + (ind + 1) + ".");
			}
		}
		return m;
	}
}
