package fr.gaston147.dbot.perm;

public class PermBool extends Perm {
	public final Perm equal;
	public final Perm not;
	public final Perm and;
	public final Perm or;

	public PermBool(Perm parent) {
		super(parent, "boolean");
		children.add(equal	= new Perm(this, "equal"));
		children.add(not	= new Perm(this, "not"));
		children.add(and	= new Perm(this, "and"));
		children.add(or		= new Perm(this, "or"));
	}
}
