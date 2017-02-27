package fr.gaston147.dbot.command;

public class CommandValEnv extends CommandVal {
	public final Env env;
	public final CommandVal child;
	
	public CommandValEnv(Env env, CommandVal child) {
		super(-1, -1);
		this.env = env;
		this.child = child;
		child.parent = this;
	}
	
	public CommandVal run(Env env) throws CommandException {
		return child.run(env);
	}
	
	public CommandVal call(Env env, CommandVal[] args) throws CommandException {
		return child.call(env, args);
	}
	
	public Env getEnv() {
		return env;
	}
	
	public String type() {
		return "env";
	}
}
