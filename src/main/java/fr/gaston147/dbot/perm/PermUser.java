package fr.gaston147.dbot.perm;

public class PermUser extends Perm {
	public final Perm help;
	public final Perm cmd;

	public PermUser(Perm parent) {
		super(parent, "user");
		children.add(help	= new Perm(this, "help"));
		children.add(cmd	= new Perm(this, "cmd"));
	}
}
