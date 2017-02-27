package fr.gaston147.dbot.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Env {
	private static final CommandVal NOT_SET = new CommandVal(-1, -1);
	
	public Env parent;
	private Map<String, CommandVal> vars;
	
	public Env(Env parent) {
		this.parent = parent;
		vars = new HashMap<String, CommandVal>();
	}
	
	public Env def(String name) throws CommandException {
		if (exists(name))
			throw new CommandException("\"" + name + "\" already exists.");
		vars.put(name, NOT_SET);
		return this;
	}
	
	protected Env parentSet(String name, CommandVal val) throws CommandException {
		parent.set(name, val);
		return this;
	}
	
	public Env set(String name, CommandVal val) throws CommandException {
		if (exists(name))
			vars.put(name, new CommandValEnv(this, val));
		else
			parentSet(name, val);
		return this;
	}
	
	public Env set(String name, String val) throws CommandException {
		return set(name, new CommandValStr(-1, -1, val));
	}
	
	public CommandVal get(String name) throws CommandException {
		CommandVal val = vars.get(name);
		if (val == NOT_SET)
			throw new CommandException("\"" + name + "\" exists, but isn't set.");
		else if (val == null)
			val = parentGet(name);
		return val;
	}
	
	protected CommandVal parentGet(String name) throws CommandException {
		return parent.get(name);
	}
	
	public Env defSet(String name, CommandVal val) throws CommandException {
		def(name);
		set(name, val);
		return this;
	}
	
	public Env defIfNeededSet(String name, CommandVal val) throws CommandException {
		if (!exists(name))
			def(name);
		set(name, val);
		return this;
	}
	
	public EnvGlobal getGlobal() {
		return parent.getGlobal();
	}
	
	public EnvEach getEach() throws CommandException {
		return parent.getEach();
	}
	
	public Set<String> all() {
		return vars.keySet();
	}
	
	public boolean exists(String name) {
		return vars.containsKey(name);
	}
	
	public void remove(String name) throws CommandException {
		set(name, NOT_SET);
	}
	
	public Env clear() {
		vars.clear();
		return this;
	}
}
