package fr.gaston147.dbot.command;

public class CommandValStr extends CommandVal {
	public final String str;
	
	public CommandValStr(int line, int col, String str) {
		super(line, col);
		this.str = str;
	}
	
	public CommandValStr(String str) {
		this(-1, -1, str);
	}
	
	public CommandVal run(Env env) throws CommandException {
		return this;
	}
	
	public CommandVal call(Env env, CommandVal[] args) throws CommandException {
		return run(env);
	}
	
	public String toString() {
		if (str.contains(" ") || str.contains("\t") || str.contains("\r") || str.contains("\n")) // DIRTY
			return "\"" + str.replaceAll("\"", "\\\"") + "\"";
		return str;
	}
	
	public String type() {
		return "string";
	}
}
