package fr.gaston147.dbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.gaston147.dbot.command.CommandException;

public class MoneyManager {
	private Map<String, Long> money;
	private List<String> userList;
	
	public MoneyManager() {
		money = new HashMap<String, Long>();
		userList = new ArrayList<String>();
	}
	
	private boolean exists(String id) {
		return money.containsKey(id);
	}
	
	public long balance(String id) {
		if (!exists(id))
			return 0;
		return money.get(id);
	}
	
	private void sort() {
		Collections.sort(userList, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return Main.instance.username(s1).compareToIgnoreCase(Main.instance.username(s2));
			}
		});
		Collections.sort(userList, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return (money.get(s1) > money.get(s2) ? -1 : (money.get(s1) < money.get(s2) ? 1 : 0));
			}
		});
	}
	
	public void setBalance(String id, long amt) {
		if (!exists(id))
			userList.add(id);
		money.put(id, amt);
	}
	
	public void add(String id, long amt) {
		setBalance(id, balance(id) + amt);
	}
	
	public void give(String from, String to, long amt) throws CommandException {
		if (amt < 0)
			throw new CommandException("The amount must be positive.");
		if (balance(from) < amt)
			throw new CommandException("User <@" + from + "> doesn't have enough money.");
		add(from, -amt);
		add(to, amt);
	}
	
	public List<String> getUserList() {
		sort();
		return userList;
	}
	
	public void load(BufferedReader r) throws IOException {
		String line;
		while ((line = r.readLine()) != null) {
			String[] sp = line.split("=");
			money.put(sp[0], Long.parseLong(sp[1]));
			userList.add(sp[0]);
		}
	}
	
	public void save(Writer w) throws IOException {
		for (String id : money.keySet())
			w.write(id + "=" + money.get(id) + "\n");
	}
}
