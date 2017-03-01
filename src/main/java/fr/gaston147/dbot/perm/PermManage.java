package fr.gaston147.dbot.perm;

public class PermManage extends Perm {
	public final Perm sleep;
	public final Perm wakeup;
	public final Perm cmdWhileAsleep;

	public PermManage(Perm parent) {
		super(parent, "manage");
		children.add(sleep			= new Perm(this, "sleep"));
		children.add(wakeup			= new Perm(this, "wakeup"));
		children.add(cmdWhileAsleep	= new Perm(this, "cmd-while-asleep"));
	}
}
