package fr.gaston147.dbot.command;

public class CommandValList extends CommandVal {
	public final CommandVal[] ls;
	
	public CommandValList(int line, int col, CommandVal[] ls) {
		super(line, col);
		this.ls = ls;
		for (CommandVal val : ls)
			val.parent = this;
	}
	
	public CommandValList(CommandVal[] ls) {
		this(-1, -1, ls);
	}
	
	public CommandVal run(Env env) throws CommandException {
		if (ls.length == 0)
			return EMPTY_STR;
		String cmdName = ls[0].runStr(env);
		CommandVal[] args = new CommandVal[ls.length - 1];
		System.arraycopy(ls, 1, args, 0, args.length);
		CommandVal cmd;
		try {
			cmd = getEnv().get(cmdName);
		} catch (CommandException e) {
			throw new CommandException(this, e.getLocalizedMessage());
		}
		Commands.opCount++;
		if (Commands.opCount > Commands.OP_COUNT_CAP)
			throw new CommandException(this, "Too many operations.");
		Commands.level++;
		if (Commands.level > Commands.LEVEL_CAP)
			throw new CommandException(this, "Stack overfow.");
		CommandVal res = cmd.call(env, args);
		Commands.level--;
		return res;
	}
	
	public String toString() {
		if (ls.length > 0) {
			StringBuilder s = new StringBuilder();
			s.append("(");
			s.append(ls[0].toString());
			for (int i = 1; i < ls.length; i++)
				s.append(" ").append(ls[i]);
			s.append(")");
			return s.toString();
		}
		return "()";
	}
	
	public String type() {
		return "list";
	}
	
	public boolean isList() {
		return true;
	}
}
