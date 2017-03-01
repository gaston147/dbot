package fr.gaston147.dbot.perm;

public class PermMisc extends Perm {
	public final Perm renderTable;
	public final Perm render;
	public final Perm show;
	public final Perm spam;
	public final Perm msg;
	public final Perm icrt;
	public final Perm irm;
	public final Perm idrawText;
	public final Perm ishow;
	public final Perm mhh;
	public final Perm itoText;
	public final Perm code;
	public final Perm math;
	public final Perm iloadUrl;
	public final Perm rip;
	public final Perm lmgtfy;
	
	public PermMisc(Perm parent) {
		super(parent, "misc");
		children.add(renderTable	= new Perm(this, "render-table"));
		children.add(render			= new Perm(this, "render"));
		children.add(show			= new Perm(this, "show"));
		children.add(spam			= new Perm(this, "spam"));
		children.add(msg			= new Perm(this, "msg"));
		children.add(icrt			= new Perm(this, "icrt"));
		children.add(irm			= new Perm(this, "irm"));
		children.add(idrawText		= new Perm(this, "idraw-text"));
		children.add(ishow			= new Perm(this, "ishow"));
		children.add(mhh			= new Perm(this, "mhh"));
		children.add(itoText		= new Perm(this, "ito-text"));
		children.add(code			= new Perm(this, "code"));
		children.add(math			= new Perm(this, "math"));
		children.add(iloadUrl		= new Perm(this, "iload-url"));
		children.add(rip			= new Perm(this, "rip"));
		children.add(lmgtfy			= new Perm(this, "lmgtfy"));
	}
}
