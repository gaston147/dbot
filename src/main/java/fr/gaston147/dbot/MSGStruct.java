package fr.gaston147.dbot;

import fr.gaston147.dbot.command.CommandVal;

public class MSGStruct {
	public final String sender;
	public final CommandVal msg;
	
	public MSGStruct(String sender, CommandVal msg) {
		this.sender = sender;
		this.msg = msg;
	}
}
