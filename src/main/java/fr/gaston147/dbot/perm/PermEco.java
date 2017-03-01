package fr.gaston147.dbot.perm;

public class PermEco extends Perm {
	public final Perm balance;
	public final Perm give;
	public final Perm list;

	public PermEco(Perm parent) {
		super(parent, "economy");
		children.add(balance	= new Perm(this, "balance"));
		children.add(give		= new Perm(this, "give"));
		children.add(list		= new Perm(this, "list"));
	}
}
