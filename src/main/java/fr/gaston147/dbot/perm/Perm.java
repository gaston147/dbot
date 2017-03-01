package fr.gaston147.dbot.perm;

import java.util.ArrayList;
import java.util.List;

public class Perm {
	public final Perm parent;
	public final List<Perm> children;
	public final String name, fullName;
	
	public Perm(Perm parent, String name, String fullName) {
		this.parent = parent;
		children = new ArrayList<Perm>();
		this.name = name;
		this.fullName = fullName;
	}
	
	public Perm(Perm parent, String name) {
		this(parent, name, parent.fullName + "." + name);
	}
	
	public boolean isAtMost(Perm perm) {
		return perm == this || parent.isAtMost(perm);
	}
	
	public Perm getChildByName(String name) {
		for (Perm p : children)
			if (p.name.equals(name))
				return p;
		return null;
	}
}
