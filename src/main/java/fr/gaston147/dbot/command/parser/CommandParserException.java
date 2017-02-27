package fr.gaston147.dbot.command.parser;

import fr.gaston147.dbot.ParserException;

public class CommandParserException extends ParserException {
	private static final long serialVersionUID = 1L;
	
	public CommandParserException(String msg) {
		super(msg);
	}
}
