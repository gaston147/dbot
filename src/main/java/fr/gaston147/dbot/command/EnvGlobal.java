package fr.gaston147.dbot.command;

public class EnvGlobal extends Env {
	
	public EnvGlobal() {
		super(null);
	}
	
	protected Env parentSet(String name, CommandVal val) throws CommandException {
		throw new CommandException("\"" + name + "\" isn't define.");
	}
	
	public EnvGlobal getGlobal() {
		return this;
	}
	
	public EnvEach getEach() throws CommandException {
		throw new CommandException("Global environment is above \"each\".");
	}
	
	protected CommandVal parentGet(String name) throws CommandException {
		throw new CommandException("\"" + name + "\" doesn't exists.");
	}
}
