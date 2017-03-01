package fr.gaston147.dbot.perm;

public class PermMath extends Perm {
	public final Perm plus;
	public final Perm minus;
	public final Perm times;
	public final Perm div;
	public final Perm less;
	public final Perm lessEq;
	public final Perm greater;
	public final Perm greaterEq;
	public final Perm r;

	public PermMath(Perm parent) {
		super(parent, "math");
		children.add(plus		= new Perm(this, "plus"));
		children.add(minus		= new Perm(this, "minus"));
		children.add(times		= new Perm(this, "times"));
		children.add(div		= new Perm(this, "div"));
		children.add(less		= new Perm(this, "less"));
		children.add(lessEq		= new Perm(this, "less-eq"));
		children.add(greater	= new Perm(this, "greater"));
		children.add(greaterEq	= new Perm(this, "greater-eq"));
		children.add(r			= new Perm(this, "r"));
	}
}
