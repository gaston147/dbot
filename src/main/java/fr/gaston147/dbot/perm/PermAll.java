package fr.gaston147.dbot.perm;

public class PermAll extends Perm {
	public final PermMisc	misc;
	public final PermManage	manage;
	public final PermUser	user;
	public final PermPrg	program;
	public final PermBool	boolean_;
	public final PermMath	math;
	public final PermEco	economy;
	public final PermEcoMan economyMan;
	public final PermCo		coroutine;
	public final PermPat	pattern;
	public final PermPerm	perm;
	
	public PermAll() {
		super(null, "all", "all");
		children.add(misc		= new PermMisc(this));
		children.add(manage		= new PermManage(this));
		children.add(user		= new PermUser(this));
		children.add(program	= new PermPrg(this));
		children.add(boolean_	= new PermBool(this));
		children.add(math		= new PermMath(this));
		children.add(economy	= new PermEco(this));
		children.add(economyMan	= new PermEcoMan(this));
		children.add(coroutine	= new PermCo(this));
		children.add(pattern	= new PermPat(this));
		children.add(perm		= new PermPerm(this));
	}
	
	public boolean isAtMost(Perm perm) {
		return perm == this;
	}
}
