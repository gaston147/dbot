package fr.gaston147.dbot.command;

public class CommandException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public CommandException(String message) {
		super(message);
	}
	
	public CommandException(CommandVal val, String message) {
		super(message + " (line: " + (val.line + 1) + ", col: " + (val.col + 1) + ")");
	}
}
