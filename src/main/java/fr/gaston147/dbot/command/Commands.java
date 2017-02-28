package fr.gaston147.dbot.command;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import fr.gaston147.dbot.CommandThread;
import fr.gaston147.dbot.DImage;
import fr.gaston147.dbot.MSGStruct;
import fr.gaston147.dbot.Main;
import fr.gaston147.dbot.ParserException;
import fr.gaston147.dbot.PatternCmd;
import fr.gaston147.dbot.Utils;

public class Commands {
	private static List<String> commandsSorted;
	private static Map<String, String> commandsUsage;
	private static Map<String, String> commandsDescription;
	private static Map<String, String> commandsCat;
	private static Map<String, String> cats;
	public static int level, opCount;
	public static final int LEVEL_CAP		= 100000;
	public static final int OP_COUNT_CAP	= 1000000;
	public static final int PAGE_CAP		= 10;
	public static EnvGlobal global;
	
	public static CommandVal addCommand(String name, String cat, String usage, String description, CommandVal cmd) throws CommandException {
		name = name.toLowerCase();
		global.defSet(name, cmd);
		commandsSorted.add(name);
		Collections.sort(commandsSorted);
		commandsCat.put(cat, name);
		commandsUsage.put(name, usage);
		commandsDescription.put(name, description);
		return cmd;
	}
	
	public static void init() throws CommandException {
		global = new EnvGlobal();
		commandsSorted = new ArrayList<String>();
		commandsUsage = new HashMap<String, String>();
		commandsDescription = new HashMap<String, String>();
		commandsCat = new HashMap<String, String>();
		cats = new HashMap<String, String>();
		
		cats.put("misc", "misc");
		cats.put("manage", "manage");
		cats.put("user", "user");
		cats.put("program", "program");
		cats.put("boolean", "boolean");
		cats.put("math", "math");
		cats.put("economy", "economy");
		cats.put("program.coroutines", "coroutines");
		
		addCommand("render-table", "misc", "{cmd} <columns>[ <string> ...", "Render a table.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				int rows = args[0].runInt(env);
				if (rows < 0)
					throw new CommandException(this, "The number of columns must be positive.");
				String[] arr = new String[args.length - 1];
				for (int i = 0; i < arr.length; i++)
					arr[i] = args[1 + i].runStr(env);
				w.write("```\n");
				Main.instance.showArr(w, arr, rows);
				w.write("\n```");
			}
		});
		addCommand("stop", "manage", "{cmd}", "Stop the bot.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 0;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				if (env.getEach().msg.getAuthor().getID().equals("224240069362319360")) {
					w.write("NTM mayem");
				} else {
					w.write("Stopping bot...");
					Main.instance.stop();
					w.write("Stopped.");
				}
			}
		});
		addCommand("help", "user", "{cmd}[ <page>[ <commands per page>]]", "Show available commands.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 2;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long page = 0, pageCap = PAGE_CAP;
				if (args.length >= 1) {
					page = args[0].runNbr(env) - 1;
					if (page < 0)
						throw new CommandException("\"page\" must be at least 1.");
					if (args.length >= 2) {
						pageCap = args[1].runNbr(env);
						if (pageCap <= 0)
							throw new CommandException("\"page\" must be at least 1.");
					}
				}
				StringBuilder s = new StringBuilder();
				s.append("```\n");
				s.append("Available commands (page " + (page + 1) + "/" + ((commandsSorted.size() + pageCap - 1) / pageCap) + "):\n");
				int curpage = 0, cursub = 0;
				for (String cName : commandsSorted) {
					if (curpage == page)
						s.append(" - " + commandsUsage.get(cName).replaceAll("\\{cmd\\}", cName) + " : " + commandsDescription.get(cName) + "\n");
					cursub++;
					if (cursub >= pageCap) {
						if (curpage == page)
							break;
						cursub = 0;
						curpage++;
					}
				}
				s.append("\n```");
				w.write(s.toString());
			}
		});
		addCommand("cmd", "user", "{cmd}", "Toggle command mode.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 0;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				String id = env.getEach().msg.getAuthor().getID();
				boolean mode = Main.instance.modeSet(id, !Main.instance.modeGet(id));
				w.write("Command mode " + (mode ? "enabled" : "disabled") + ".");
			}
		});
		addCommand("render", "misc", "{cmd} <number>[ <number>", "Render characters passed as numbers.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				StringBuilder str = new StringBuilder();
				for (CommandVal v : args) {
					String s = v.runStr(env);
					int radix = 10;
					if (s.toLowerCase().startsWith("0x")) {
						s = s.substring(2);
						radix = 16;
					} else if (s.toLowerCase().startsWith("0b")) {
						s = s.substring(2);
						radix = 2;
					}
					try {
						str.append(Character.toString((char) Long.parseLong(s, radix)));
					} catch (NumberFormatException e) {
						throw new CommandException(this, "\"" + s + "\" isn't a number.");
					}
				}
				w.write(str.toString());
			}
		});
		addCommand("show", "misc", "{cmd} <string>", "Show characters used in the string.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				StringBuilder str = new StringBuilder();
				for (char s : args[0].runStr(env).toCharArray())
					str.append(Integer.toString((int) s) + " ");
				if (str.length() > 0)
					str.deleteCharAt(str.length() - 1);
				w.write(str.toString());
			}
		});
		addCommand("spam", "misc", "{cmd}[ <user>[ <string>[ <string> ...", "Spam user with random messages every time he send a message. ", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				String id = env.getEach().msg.getAuthor().getID();
				if (args.length > 0) {
					String user = args[0].runStr(env);
					if (user.length() < 3 || !user.startsWith("<@") || !user.endsWith(">"))
						throw new CommandException(this, "You must specify a user.");
					id = user.substring("<@".length(), user.length() - ">".length());
				}
				if (args.length <= 1) {
					Main.instance.commandSpam.remove(id);
				} else {
					CommandVal[] sub = new CommandVal[args.length - 1];
					System.arraycopy(args, 1, sub, 0, sub.length);
					Main.instance.commandSpam.put(id, sub);
				}
				w.write("User <@" + id + "> will " + (args.length <= 1 ? "no longer" : "now") + " be spammed.");
			}
		});
		addCommand("loop", "program", "{cmd} <count> <string>", "Duplicate string.", new CommandValCheck() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				int n = args[0].runInt(env);
				if (n < 0)
					throw new CommandException(this, "\"count\" must be positive.");
				for (int i = 0; i < n; i++)
					w.write(args[1].runStr(env));
			}
		});
		addCommand("do", "program", "{cmd}[ <string>[ <string> ...", "Concat strings.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				for (int i = 0; i < args.length; i++)
					args[i].run(w, env);
			}
		});
		addCommand("rand", "program", "{cmd} <string>[ <string> ...", "Choose a random string.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				w.write(args[(int) (Math.random() * args.length)].runStr(env));
			}
		});
		addCommand("map", "program", "{cmd} <list> <var> <func>", "Execute <func> for each elements in <list>.", new CommandValCheck2() {
			
			public int min() {
				return 3;
			}
			
			public int max() {
				return 3;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				StringBuilder sb = new StringBuilder();
				CommandValList list = args[0].runList(env);
				for (int i = 0; i < list.ls.length; i++) {
					env.set(args[1].runStr(env), list.ls[i]);
					sb.append(args[2].runStr(env));
				}
				return new CommandValStr(sb.toString());
			}
		});
		addCommand("d", "program", "{cmd} <string>", "Define variable / define function.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				env.def(args[0].runStr(env));
			}
		});
		addCommand("s", "program", "{cmd} <string> [ <string>]", "Compute the arguments, then set the value. Delete if no arguments.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 2;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				String varName = args[0].runStr(env);
				if (args.length == 1)
					env.remove(varName);
				else {
					try {
						env.set(varName, args[1].run(env));
					} catch (CommandException e) {
						throw new CommandException(args[1], e.getLocalizedMessage());
					}
				}
			}
		});
		addCommand("ds", "program", "{cmd} <string> <string>", "###", new CommandValCheck() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				String varName = args[0].runStr(env);
				try {
					env.def(varName);
					env.set(varName, args[1].run(env));
				} catch (CommandException e) {
					throw new CommandException(args[1], e.getLocalizedMessage());
				}
			}
		});
		addCommand("g", "program", "{cmd} <string>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				return ((CommandValEnv) env.get(args[0].runStr(env))).child; // DIRTY
			}
		});
		addCommand("=", "boolean", "{cmd} <string>[ <string> ...", "Compare strings. Return \"1\" if they're equal, otherwise return empty.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				String s0 = args[0].runStr(env);
				for (int i = 1; i < args.length; i++)
					if (!s0.equals(args[i].runStr(env)))
						return;
				w.write("1");
			}
		});
		addCommand("!", "boolean", "{cmd} <string>", "Return \"1\" if empty, otherwise return empty.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				if (args[0].runStr(env).length() == 0)
					w.write("1");
			}
		});
		addCommand("&", "boolean", "{cmd}[ <string> <string> ...", "Return \"1\" if all strings aren't empty, otherwise return empty.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				for (int i = 0; i < args.length; i++)
					if (args[i].runStr(env).length() == 0)
						return;
				w.write("1");
			}
		});
		addCommand("|", "boolean", "{cmd}[ <string>[ <string> ...", "Return \"1\" if at least one string isn't empty, otherwise return empty.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				for (int i = 0; i < args.length; i++) {
					String sarg = args[i].runStr(env);
					if (sarg.length() > 0) {
						w.write(sarg);
						return;
					}
				}
			}
		});
		addCommand("if", "program", "{cmd} <string> <command1>[ <command2>]", "Run <command1> if <string> isn't empty, otherwise run <command2>.", new CommandValCheck() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 3;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				if (args[0].runStr(env).length() > 0)
					args[1].run(w, env);
				else if (args.length == 3)
					args[2].run(w, env);
			}
		});
		addCommand("while", "program", "{cmd} <string> <command>", "Run <command> while <string> isn't empty.", new CommandValCheck() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				while (args[0].runStr(env).length() > 0)
					args[1].run(w, env);
			}
		});
		addCommand("null", "program", "{cmd}[ <string>]", "Output nothing.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				if (args.length == 1)
					args[0].run(env);
			}
		});
		addCommand("+", "math", "{cmd}[ <number>[ <number> ...", "Sum arguments, 0 if no arguments.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n = 0;
				for (int i = 0; i < args.length; i++)
					n += args[i].runNbr(env);
				w.write(Long.toString(n));
			}
		});
		addCommand("-", "math", "{cmd}[ <number>[ <number> ...", "First argument minus all the other, 0 if no arguments.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n = 0;
				if (args.length > 0) {
					n = args[0].runNbr(env);
					for (int i = 1; i < args.length; i++)
						n -= args[i].runNbr(env);
				}
				w.write(Long.toString(n));
			}
		});
		addCommand("*", "math", "{cmd}[ <number>[ <number> ...", "Compute the product of all arguments, 1 if no arguments.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n = 1;
				for (int i = 0; i < args.length; i++)
					n *= args[i].runNbr(env);
				w.write(Long.toString(n));
			}
		});
		addCommand("/", "math", "{cmd}[ <number>[ <number> ...", "First argument divided by all the other, 1 if no arguments.", new CommandValCheck() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n = 1;
				if (args.length > 0) {
					n = args[0].runNbr(env);
					for (int i = 1; i < args.length; i++)
						n /= args[i].runNbr(env);
				}
				w.write(Long.toString(n));
			}
		});
		addCommand("<", "math", "{cmd} <number>[ <number> ...", "\"1\" if arguments are passed in strictly ascending order, empty string otherwise.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n0, n1;
				
				n1 = args[0].runNbr(env);
				for (int i = 1; i < args.length; i++) {
					n0 = n1;
					n1 = args[i].runNbr(env);
					if (n0 >= n1)
						return;
				}
				w.write("1");
			}
		});
		addCommand("<=", "math", "{cmd} <number>[ <number> ...", "\"1\" if arguments are passed in ascending order, empty string otherwise.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n0, n1;
				
				n1 = args[0].runNbr(env);
				for (int i = 1; i < args.length; i++) {
					n0 = n1;
					n1 = args[i].runNbr(env);
					if (n0 > n1)
						return;
				}
				w.write("1");
			}
		});
		addCommand(">", "math", "{cmd} <number>[ <number> ...", "\"1\" if arguments are passed in strictly descending order, empty string otherwise.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n0, n1;
				
				n1 = args[0].runNbr(env);
				for (int i = 1; i < args.length; i++) {
					n0 = n1;
					n1 = args[i].runNbr(env);
					if (n0 <= n1)
						return;
				}
				w.write("1");
			}
		});
		addCommand(">=", "math", "{cmd} <number>[ <number> ...", "\"1\" if arguments are passed in descending order, empty string otherwise.", new CommandValCheck() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return -1;
			}
			
			public void invoke(Writer w, Env env, String cmdName, CommandVal[] args) throws IOException, CommandException {
				long n0, n1;
				
				n1 = args[0].runNbr(env);
				for (int i = 1; i < args.length; i++) {
					n0 = n1;
					n1 = args[i].runNbr(env);
					if (n0 < n1)
						return;
				}
				w.write("1");
			}
		});
		addCommand("raw", "program", "{cmd} <value>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				return args[0];
			}
		});
		addCommand("run", "program", "{cmd} <value>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				return args[0].run(env).run(env);
			}
		});
		addCommand("lcrt", "program", "{cmd} <size>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				int size = args[0].runInt(env);
				if (size < 0)
					throw new CommandException("Size must be positive.");
				CommandVal[] lsArr = new CommandVal[size];
				for (int i = 0; i < lsArr.length; i++)
					lsArr[i] = EMPTY_STR;
				CommandValList ls = new CommandValList(-1, -1, lsArr);
				ls.parent = this;
				return ls;
			}
		});
		addCommand("ls", "program", "{cmd} <list> <ind> <value>", "###", new CommandValCheck2() {
			
			public int min() {
				return 3;
			}
			
			public int max() {
				return 3;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				CommandValList ls = args[0].runList(env);
				int ind = args[1].runInt(env);
				if (ind < 0)
					throw new CommandException("Index must be positive.");
				if (ind >= ls.ls.length)
					throw new CommandException("Index must be less than the list size.");
				ls.ls[ind] = args[2].run(env);
				return EMPTY_STR;
			}
		});
		addCommand("lg", "program", "{cmd} <list> <ind>", "###", new CommandValCheck2() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				CommandValList ls = args[0].runList(env);
				int ind = args[1].runInt(env);
				if (ind < 0)
					throw new CommandException("Index must be positive.");
				if (ind >= ls.ls.length)
					throw new CommandException("Index must be less than the list size.");
				return ls.ls[ind];
			}
		});
		addCommand("ldo", "program", "{cmd}[ <list>[ <list> ...", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				CommandValList[] lists = new CommandValList[args.length];
				int totalLen = 0;
				for (int i = 0; i < args.length; i++)
					totalLen += (lists[i] = args[i].runList(env)).ls.length;
				CommandVal[] totalArr = new CommandVal[totalLen];
				int ind = 0;
				for (int i = 0; i < args.length; i++) {
					System.arraycopy(lists[i].ls, 0, totalArr, ind, lists[i].ls.length);
					ind += lists[i].ls.length;
				}
				CommandValList total = new CommandValList(-1, -1, totalArr);
				total.parent = this; // DIRTY
				return total;
			}
		});
		addCommand("type", "program", "{cmd} <any>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				return new CommandValStr(args[0].run(env).type());
			}
		});
		addCommand("msg", "misc", "{cmd} <user> <any>", "###", new CommandValCheck2() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String id = env.getEach().msg.getAuthor().getID();
				if (args.length > 0) {
					String user = args[0].runStr(env);
					if (user.length() < 3 || !user.startsWith("<@") || !user.endsWith(">"))
						throw new CommandException(this, "You must specify a user.");
					id = user.substring("<@".length(), user.length() - ">".length());
				}
				if (!Main.instance.commandMsg.containsKey(id))
					Main.instance.commandMsg.put(id, new ArrayList<MSGStruct>());
				Main.instance.commandMsg.get(id).add(new MSGStruct(env.getEach().msg.getAuthor().getID(), args[1]));
				return new CommandValStr("Message will be sent to <@" + id + ">.");
			}
		});
		addCommand("dice", "program", "{cmd} <number>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				long n = args[0].runNbr(env);
				if (n < 0)
					throw new CommandException("Number must be positive.");
				return new CommandValStr(Long.toString(1L + (long) (Math.random() * n)));
			}
		});
		addCommand("icrt", "misc", "{cmd} <width> <height>", "###", new CommandValCheck2() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				int w, h;
				w = args[0].runInt(env);
				if (w <= 0)
					throw new CommandException("Width must be at least 1.");
				h = args[1].runInt(env);
				if (h <= 0)
					throw new CommandException("Height must be at least 1.");
				if (w > 256 * 256 / h)
					throw new CommandException("Image too big: width * height > 256 * 256.");
				return new CommandValStr(env.getEach().createImage(w, h));
			}
		});
		addCommand("idel", "misc", "{cmd} <image>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				env.getEach().deleteImage(args[0].runStr(env));
				return EMPTY_STR;
			}
		});
		addCommand("idraw-text", "misc", "{cmd} <image> <font> <x> <y> <text>", "###", new CommandValCheck2() {
			
			public int min() {
				return 5;
			}
			
			public int max() {
				return 5;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				DImage dimg = env.getEach().getImage(args[0].runStr(env));
				args[1].run(env);
				dimg.drawText(null, args[2].runInt(env), args[3].runInt(env), args[4].runStr(env));
				return EMPTY_STR;
			}
		});
		addCommand("ishow", "misc", "{cmd} <image>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				env.getEach().getImage(args[0].runStr(env)).show(env.getEach().msg.getChannel());
				return EMPTY_STR;
			}
		});
		addCommand("r", "math", "{cmd} <radix> <number>", "###", new CommandValCheck2() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				int r = args[0].runInt(env);
				if (r < 2)
					throw new CommandException("Radix must be at least 2.");
				if (r > 36)
					throw new CommandException("Radix must be at most 36.");
				try {
					return new CommandValStr(Long.toString(Long.parseLong(args[1].runStr(env), r)));
				} catch (NumberFormatException e) {
					CommandVal.wrongType("number", "string");
				}
				return null;
			}
		});
		addCommand("mhh", "misc", "{cmd} <image>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String id = args[0].runStr(env);
				DImage dimg = env.getEach().getImage(id);
				for (int y = 0; y < dimg.h; y++)
					for (int x = 0; x < dimg.w; x++)
						dimg.setRGBA(x, y, ((x + y) % 2 == 0 ? 0x000000ff : 0xffffffff));
				return new CommandValStr(id);
			}
		});
		addCommand("ito-text", "misc", "{cmd} <image>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				DImage dimg = env.getEach().getImage(args[0].runStr(env));
				return new CommandValStr(Utils.renderImage(dimg, dimg.h));
			}
		});
		addCommand("code", "misc", "{cmd}[ <language>] <string>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String lang = "", str = args[0].runStr(env);
				if (args.length >= 2) {
					lang = str;
					str = args[1].runStr(env);
				}
				return new CommandValStr("```" + lang + "\n" + str + "```");
			}
		});
		addCommand("math", "misc", "{cmd} <latex>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				try {
					TeXFormula fomule = new TeXFormula(args[0].runStr(env));
					TeXIcon ti = fomule.createTeXIcon(TeXConstants.STYLE_DISPLAY, 40);
					BufferedImage b = new BufferedImage(ti.getIconWidth(), ti.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
					b.getGraphics().setColor(Color.WHITE);
					b.getGraphics().fillRect(0, 0, b.getWidth(), b.getHeight());
					b.getGraphics().setColor(Color.BLACK);
					ti.paintIcon(new JLabel(), b.getGraphics(), 0, 0);
					File t2 = new File(Main.instance.saveDir, "temp2.png");
					try {
						ImageIO.write(b, "png", t2);
						env.getEach().msg.getChannel().sendFile(t2);
					} catch (IOException | MissingPermissionsException | RateLimitException | DiscordException e) {
						e.printStackTrace();
					}
				} catch (org.scilab.forge.jlatexmath.ParseException e) {
					throw new CommandException(e.getLocalizedMessage());
				}
				return EMPTY_STR;
			}
		});
		addCommand("iload-url", "misc", "{cmd} <url>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				if (!Main.instance.hasPermission(env.getEach().msg.getAuthor().getID(), "image.load-url"))
					throw new CommandException("You don't have permission to run this command.");
				env.getEach().newImageID(); // Check if the user can create an image
				DImage dimg;
				try {
					dimg = DImage.loadFromUrl(new URL(args[0].runStr(env)));
				} catch (IOException e) {
					throw new CommandException(e.getLocalizedMessage());
				}
				return new CommandValStr(env.getEach().addImage(dimg));
			}
		});
		addCommand("rip", "misc", "{cmd} <name> <birth>[ <death>]", "###", new CommandValCheck2() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 3;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				try {
					DImage dimg = DImage.loadFromUrl(new URL("http://www.monumentsgagnon.com/images/laser/canards.jpg"));
					String	line1 = args[0].runStr(env),
							line2 = args[1].runStr(env) + " - " + (args.length >= 3 ? args[2].runStr(env) : Calendar.getInstance().get(Calendar.YEAR));
					dimg.nativeFont = dimg.nativeFont.deriveFont(Font.PLAIN, 40);
					dimg.nativeFont = dimg.nativeFont.deriveFont(Font.PLAIN, 40 * 225 / dimg.fontWidth(line1));
					dimg.drawText(null, 320 - dimg.fontWidth(line1) / 2, 100, line1);
					dimg.nativeFont = dimg.nativeFont.deriveFont(Font.PLAIN, 40);
					dimg.drawText(null, 320 - dimg.fontWidth(line2) / 2, 170, line2);
					dimg.show(env.getEach().msg.getChannel());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return EMPTY_STR;
			}
		});
		addCommand("m-balance", "economy", "{cmd}[ <user>]", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String user, userID;
				if (args.length == 0) {
					userID = env.getEach().msg.getAuthor().getID();
					user = "You";
				} else {
					userID = args[0].runStr(env);
					if (userID.length() < 3 || !userID.startsWith("<@") || !userID.endsWith(">"))
						throw new CommandException("User expected.");
					userID = userID.substring("<@".length(), userID.length() - ">".length());
					if (Main.instance.client.getUserByID(userID) == null)
						throw new CommandException("Unknown user <@" + userID + ">.");
					user = "<@" + userID + ">";
				}
				return new CommandValStr(user + " currently have: " + Main.instance.moneyManager.balance(userID) + ".");
			}
		});
		addCommand("m-give", "economy", "{cmd} <player> <amount>", "###", new CommandValCheck2() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String authorID = env.getEach().msg.getAuthor().getID();
				String id = args[0].runStr(env);
				if (id.length() < 3 || !id.startsWith("<@") || !id.endsWith(">"))
					throw new CommandException("User expected.");
				id = id.substring("<@".length(), id.length() - ">".length());
				if (Main.instance.client.getUserByID(id) == null)
					throw new CommandException("Unknown user <@" + id + ">.");
				long amt = args[1].runNbr(env);
				if (Main.instance.moneyManager.balance(authorID) < amt)
					throw new CommandException("You don't have enough money.");
				Main.instance.moneyManager.give(authorID, id, amt);
				return new CommandValStr("You gave <@" + id + "> " + amt + ".");
			}
		});
		addCommand("m-create", "economy", "{cmd} <player> <amount>", "###", new CommandValCheck2() {
			
			public int min() {
				return 2;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				if (!Main.instance.hasPermission(env.getEach().msg.getAuthor().getID(), "economy.manage"))
					throw new CommandException("You don't have permission to run this command.");
				String id = args[0].runStr(env);
				if (id.length() < 3 || !id.startsWith("<@") || !id.endsWith(">"))
					throw new CommandException("User expected.");
				id = id.substring("<@".length(), id.length() - ">".length());
				if (Main.instance.client.getUserByID(id) == null)
					throw new CommandException("Unknown user <@" + id + ">.");
				long amt = args[1].runNbr(env);
				Main.instance.moneyManager.add(id, amt);
				return new CommandValStr("User <@" + id + "> had " + amt + " added to his account.");
			}
		});
		addCommand("m-list", "economy", "{cmd}[ <page>[ <user per page>]]", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				int page = 0, userPerPage = 10;
				if (args.length >= 1) {
					page = args[0].runInt(env) - 1;
					if (page < 0)
						throw new CommandException("Page must be at least 1.");
					if (args.length >= 2) {
						userPerPage = args[1].runInt(env);
						if (userPerPage <= 0)
							throw new CommandException("User per page must be at least 1.");
					}
				}
				List<String> userList = Main.instance.moneyManager.getUserList();
				int total = Utils.clamp(userList.size() - userPerPage * page, 0, userPerPage);
				String[] arr = new String[2 + total * 2];
				arr[0] = "=USER";
				arr[1] = "=BALANCE";
				for (int i = 0; i < total; i++) {
					String userID = userList.get(userPerPage * page + i);
					arr[2 + i * 2 + 0] = Main.instance.username(userID);
					arr[2 + i * 2 + 1] = ">" + Long.toString(Main.instance.moneyManager.balance(userID));
				}
				StringWriter sw = new StringWriter();
				try {
					Main.instance.showArr(sw, arr, 2);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new CommandValStr("```\n(" + (page + 1) + "/" + ((userList.size() + userPerPage - 1) / userPerPage) + ")\n" + sw.toString() + "```");
			}
		});
//		addCommand("co-create", "{cmd} <func>", "###", new CommandValCheck2() {
//			
//			public int min() {
//				return 1;
//			}
//			
//			public int max() {
//				return 1;
//			}
//			
//			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
//				((CommandThread) Thread.currentThread()).co_yield();
//				return EMPTY_STR;
//			}
//		});
//		addCommand("co-resume", "{cmd}[ <arg>[ <arg> ...", "###", new CommandValCheck2() {
//			
//			public int min() {
//				return 0;
//			}
//			
//			public int max() {
//				return -1;
//			}
//			
//			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
//				((CommandThread) Thread.currentThread()).co_yield();
//				return EMPTY_STR;
//			}
//		});
		addCommand("co-yield", "program.coroutines", "{cmd}[ <arg>[ <arg> ...", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				return ((CommandThread) Thread.currentThread()).co_yield(new CommandValList(args));
			}
		});
		addCommand("lmgtfy", "misc", "{cmd} <term>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				return new CommandValStr("https://lmgtfy.com/?q=" + Utils.urlEncode(args[0].runStr(env)));
			}
		});
		addCommand("wait-events", "program", "{cmd}[ <event>[ <event> ...", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String[] events = new String[args.length];
				for (int i = 0; i < args.length; i++)
					events[i] = args[i].runStr(env);
				while (true) {
					CommandValList ls = (CommandValList) (((CommandThread) Thread.currentThread()).co_yield(new CommandValList(args)));
					String caught = CommandVal.checkStr(ls.ls[0]);
					for (String event : events)
						if (caught.equals(event))
							return ls;
				}
			}
		});
		addCommand("p-all", "program", "{cmd}[ <func>[ <func> ...", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return -1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				StringBuilder total = new StringBuilder();
				int count = args.length;
				CommandThread[] cos = new CommandThread[args.length];
				String[] events = new String[cos.length];
				for (int i = 0; i < cos.length; i++) {
					cos[i] = Main.co_create(args[i]);
					CommandVal res = cos[i].co_resume(new CommandValList(new CommandVal[0]));
					if (cos[i].dead) {
						total.append(CommandVal.checkStr(res));
						count--;
					} else {
						CommandValList resLs = (CommandValList) res;
						if (resLs.ls.length == 0)
							throw new CommandException("Event name expected.");
						events[i] = resLs.ls[0].runStr(env);
					}
				}
				while (count > 0) {
					CommandValList ls = (CommandValList) (((CommandThread) Thread.currentThread()).co_yield(new CommandValList(new CommandVal[] { EMPTY_STR })));
					String caught = CommandVal.checkStr(ls.ls[0]);
					for (int i = 0; i < cos.length; i++) {
						if (!cos[i].dead && caught.equals(events[i])) {
							CommandVal res = cos[i].co_resume(ls);
							if (cos[i].dead) {
								total.append(CommandVal.checkStr(res));
								count--;
							} else {
								CommandValList resLs = (CommandValList) res;
								if (resLs.ls.length == 0)
									throw new CommandException("Event name expected.");
								events[i] = resLs.ls[0].runStr(env);
							}
						}
					}
				}
				return new CommandValStr(total.toString());
			}
		});
		addCommand("echo", "program", "{cmd}[ <stream>] <message>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				if (args.length == 2)
					throw new CommandException("Not implemented yet.");
				((CommandThread) Thread.currentThread()).send(args[0].runStr(env));
				return EMPTY_STR;
			}
		});
		addCommand("patset", "misc", "{cmd} <name> <pattern> <replacement>", "###", new CommandValCheck2() {
			
			public int min() {
				return 3;
			}
			
			public int max() {
				return 3;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				try {
					String name = args[0].runStr(env);
					Main.patterns.put(name, new PatternCmd(name, Pattern.compile(args[1].runStr(env)), args[2].runStr(env)));
					return new CommandValStr("Pattern set.");
				} catch (PatternSyntaxException e) {
					throw new CommandException("Pattern syntax error: " + e.getLocalizedMessage());
				}
			}
		});
		addCommand("patrm", "misc", "{cmd} <name>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String name = args[0].runStr(env);
				if (!Main.patterns.containsKey(name))
					throw new CommandException("Unknown pattern \"" + name + "\".");
				Main.patterns.remove(name);
				return new CommandValStr("Pattern removed.");
			}
		});
		addCommand("patls", "misc", "{cmd}[ <page>[ <per page>]]", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 2;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				int page = 0, perPage = 10;
				if (args.length >= 1) {
					page = args[0].runInt(env) - 1;
					if (page < 0)
						throw new CommandException("Page must be at least 1.");
					if (args.length >= 2) {
						perPage = args[1].runInt(env);
						if (perPage <= 0)
							throw new CommandException("Per page must be at least 1.");
					}
				}
				Collection<PatternCmd> ls = Main.patterns.values();
				Iterator<PatternCmd> it = ls.iterator();
				StringBuilder sb = new StringBuilder();
				sb.append("```\n(" + (page + 1) + "/" + ((ls.size() + perPage - 1) / perPage) + ")\n");
				while (it.hasNext())
					sb.append(" - " + it.next().name + "\n");
				sb.append("```");
				return new CommandValStr(sb.toString());
			}
		});
		addCommand("patinfo", "misc", "{cmd} <name>", "###", new CommandValCheck2() {
			
			public int min() {
				return 1;
			}
			
			public int max() {
				return 1;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				String name = args[0].runStr(env);
				PatternCmd pc = Main.patterns.get(name);
				if (pc == null)
					throw new CommandException("Unknown pattern \"" + name + "\".");
				return new CommandValStr("```\nname:\n -> " + pc.name + "\npattern:\n -> " + pc.p.toString() + "\nvalue:\n -> " + pc.v + "\n```");
			}
		});
		addCommand("save", "manage", "{cmd}", "###", new CommandValCheck2() {
			
			public int min() {
				return 0;
			}
			
			public int max() {
				return 0;
			}
			
			public CommandVal invoke(Env env, String cmdName, CommandVal[] args) throws CommandException {
				Main.instance.save();
				return new CommandValStr("Saved.");
			}
		});
		File bootFile = new File(Main.mainDir, "boot.code");
		try {
			Utils.touchOrCrash(bootFile);
			Reader r = new BufferedReader(new FileReader(bootFile));
			CommandThread ct = Main.co_create(new CommandValEnv(global, Main.instance.cp.parse(r)));
			ct.co_resume(new CommandValList(new CommandVal[0]));
			if (!ct.dead)
				throw new CommandException("Unexpected yield from boot code.");
			r.close();
		} catch (ParserException | IOException e) {
			e.printStackTrace();
			System.exit(0); // DIRTY
		}
	}
}
