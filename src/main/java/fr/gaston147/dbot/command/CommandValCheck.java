package fr.gaston147.dbot.command;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public abstract class CommandValCheck extends CommandVal {
	
	public CommandValCheck() {
		super(-1, -1);
	}
	
	public abstract int min();
	public abstract int max();
	public abstract void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException;
	
	public CommandVal run(Env env) throws CommandException {
		throw new CommandException("This command must be runned.");
	}
	
	public CommandVal call(Env env, CommandVal[] args) throws CommandException {
		if (args.length < min())
			throw new CommandException(this, "Not enough arguments, expected at least " + min() + ", at most " + max() + ".\n");
		else if (max() != -1 && args.length > max())
			throw new CommandException(this, "Too many arguments, expected at least " + min() + ", at most " + max() + ".\n");
		try {
			StringWriter sw = new StringWriter();
			invoke(sw, env, null, args);
			sw.close();
			return new CommandValStr(-1, -1, sw.toString());
		} catch (IOException e) {
			throw new CommandException(this, e.getLocalizedMessage());
		}
	}
	
	public String type() {
		return "native";
	}
}
