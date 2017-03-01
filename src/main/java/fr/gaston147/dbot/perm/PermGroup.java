package fr.gaston147.dbot.perm;

import java.util.List;

public class PermGroup {
	public final String name;
	public final List<PermGroup> parents;
	public final List<Perm> perms;
	
	public PermGroup(String name, List<PermGroup> parents, List<Perm> perms) {
		this.name = name;
		this.parents = parents;
		this.perms = perms;
	}
	
	protected boolean selfHasPermission(Perm perm) {
		for (Perm p : perms)
			if (perm.isAtMost(p))
				return true;
		return false;
	}
	
	public boolean hasPermission(Perm perm) {
		if (selfHasPermission(perm))
			return true;
		for (PermGroup parent : parents)
			if (parent.hasPermission(perm))
				return true;
		return false;
	}

}
