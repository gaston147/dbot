package fr.gaston147.dbot.perm;

public class PermEcoMan extends Perm {
	public final Perm create;

	public PermEcoMan(Perm parent) {
		super(parent, "economy-manage");
		children.add(create = new Perm(this, "create"));
	}
}
