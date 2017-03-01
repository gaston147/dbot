package fr.gaston147.dbot.perm;

public class PermPrg extends Perm {
	public final Perm loop;
	public final Perm do_;
	public final Perm rand;
	public final Perm map;
	public final Perm d;
	public final Perm s;
	public final Perm ds;
	public final Perm g;
	public final Perm if_;
	public final Perm while_;
	public final Perm null_;
	public final Perm raw;
	public final Perm run;
	public final Perm lcrt;
	public final Perm ls;
	public final Perm lg;
	public final Perm ldo;
	public final Perm type;
	public final Perm dice;
	public final Perm waitEvents;
	public final Perm pAll;
	public final Perm echo;

	public PermPrg(Perm parent) {
		super(parent, "program");
		children.add(loop		= new Perm(this, "loop"));
		children.add(do_		= new Perm(this, "do"));
		children.add(rand		= new Perm(this, "rand"));
		children.add(map		= new Perm(this, "map"));
		children.add(d			= new Perm(this, "d"));
		children.add(s			= new Perm(this, "s"));
		children.add(ds			= new Perm(this, "ds"));
		children.add(g			= new Perm(this, "g"));
		children.add(if_		= new Perm(this, "if"));
		children.add(while_		= new Perm(this, "while"));
		children.add(null_		= new Perm(this, "null"));
		children.add(raw		= new Perm(this, "raw"));
		children.add(run		= new Perm(this, "run"));
		children.add(lcrt		= new Perm(this, "lcrt"));
		children.add(ls			= new Perm(this, "ls"));
		children.add(lg			= new Perm(this, "lg"));
		children.add(ldo		= new Perm(this, "ldo"));
		children.add(type		= new Perm(this, "type"));
		children.add(dice		= new Perm(this, "dice"));
		children.add(waitEvents	= new Perm(this, "wait-events"));
		children.add(pAll		= new Perm(this, "p-all"));
		children.add(echo		= new Perm(this, "echo"));
	}
}
