package fr.gaston147.dbot.perm;

public class PermPat extends Perm {
	public final Perm set;
	public final Perm rm;
	public final Perm ls;
	public final Perm info;

	public PermPat(Perm parent) {
		super(parent, "pattern");
		children.add(set	= new Perm(this, "set"));
		children.add(rm		= new Perm(this, "rm"));
		children.add(ls		= new Perm(this, "ls"));
		children.add(info	= new Perm(this, "info"));
	}
}
