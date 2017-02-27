package fr.gaston147.dbot.command;

import java.io.IOException;
import java.io.Writer;

public class CommandVal {
	public static final CommandVal EMPTY_STR = new CommandValStr(-1, -1, "");

	private static String _checkStr(CommandVal val, String type) throws CommandException {
		if (!(val instanceof CommandValStr))
			wrongType(type, val.type());
		return ((CommandValStr) val).str;
	}
	
	public static String checkStr(CommandVal val) throws CommandException {
		return _checkStr(val, "string");
	}
	
	public static long checkNbr(CommandVal val) throws CommandException {
		try {
			String s = _checkStr(val, "number");
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			wrongType("number", "string");
		}
		return -1;
	}
	
	public static int checkInt(CommandVal val) throws CommandException {
		long n = checkNbr(val);
		if (n < Integer.MIN_VALUE)
			throw new CommandException(val, "Number too small.");
		if (n > Integer.MAX_VALUE)
			throw new CommandException(val, "Number too big.");
		return (int) n;
	}
	
	public static CommandValList checkList(CommandVal val) throws CommandException {
		if (!(val instanceof CommandValList))
			wrongType("list", val.type());
		return (CommandValList) val;
	}
	
	public static void wrongType(String expected, String got) throws CommandException {
		throw new CommandException(expected + " expected, got " + got + ".");
	}
	
	public CommandVal parent;
	public int line, col;
	
	public CommandVal(int line, int col) {
		this.line = line;
		this.col = col;
	}
	
	public CommandVal() {
		this(-1, -1);
	}
	
	public CommandVal run(Env env) throws CommandException {
		throw new CommandException("Method should be extended.");
	}
	
	public void run(Writer w, Env env) throws CommandException {
		try {
			w.write(runStr(env));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String runStr(Env env) throws CommandException {
		return checkStr(run(env));
	}
	
	public long runNbr(Env env) throws CommandException {
		return checkNbr(run(env));
	}
	
	public int runInt(Env env) throws CommandException {
		return checkInt(run(env));
	}
	
	public CommandValList runList(Env env) throws CommandException {
		return checkList(run(env));
	}
	
	public CommandVal call(Env env, CommandVal[] args) throws CommandException {
		return run(env);
	}
	
	public void call(Writer w, Env env, CommandVal[] args) throws CommandException {
		try {
			CommandVal res = call(env, args);
			if (!(res instanceof CommandValStr))
				wrongType("string", res.type());
			w.write(((CommandValStr) res).str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Env getEnv() {
		return parent.getEnv();
	}
	
	public String type() {
		return null;
	}
	
	public boolean isList() {
		return false;
	}
}
