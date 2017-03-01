package fr.gaston147.dbot.perm;

public class PermCo extends Perm {
	public final Perm yield;

	public PermCo(Perm parent) {
		super(parent, "coroutine");
		children.add(yield = new Perm(this, "yield"));
	}
}
