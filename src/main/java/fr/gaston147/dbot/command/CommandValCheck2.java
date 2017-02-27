package fr.gaston147.dbot.command;

public abstract class CommandValCheck2 extends CommandVal {
	
	public CommandValCheck2() {
		super(-1, -1);
	}
	
	public abstract int min();
	public abstract int max();
	public abstract CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException;
	
	public CommandVal run(Env env) throws CommandException {
		throw new CommandException("This command must be runned.");
	}
	
	public CommandVal call(Env env, CommandVal[] args) throws CommandException {
		if (args.length < min())
			throw new CommandException(this, "Not enough arguments, expected at least " + min() + ", at most " + max() + ".\n");
		else if (max() != -1 && args.length > max())
			throw new CommandException(this, "Too many arguments, expected at least " + min() + ", at most " + max() + ".");
		return invoke(env, null, args);
	}
	
	public String type() {
		return "native";
	}
}
