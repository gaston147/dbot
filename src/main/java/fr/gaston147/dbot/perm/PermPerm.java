package fr.gaston147.dbot.perm;

public class PermPerm extends Perm {
	public final Perm ls;
	
	public PermPerm(Perm parent) {
		super(parent, "perm");
		children.add(ls = new Perm(this, "ls"));
	}
}
