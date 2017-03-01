package fr.gaston147.dbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.gaston147.dbot.command.CommandException;
import fr.gaston147.dbot.command.CommandVal;
import fr.gaston147.dbot.command.CommandValEnv;
import fr.gaston147.dbot.command.CommandValList;
import fr.gaston147.dbot.command.CommandValStr;
import fr.gaston147.dbot.command.Commands;
import fr.gaston147.dbot.command.EnvEach;
import fr.gaston147.dbot.command.parser.CommandParser;
import fr.gaston147.dbot.config.Config;
import fr.gaston147.dbot.config.ConfigNodeMap;
import fr.gaston147.dbot.config.ConfigNodeStr;
import fr.gaston147.dbot.config.ConfigParser;
import fr.gaston147.dbot.config.ConfigParserException;
import fr.gaston147.dbot.perm.Perm;
import fr.gaston147.dbot.perm.PermGroup;
import fr.gaston147.dbot.perm.Perms;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	public static Main instance;
	public static final File mainDir = new File("dbot");
	public static HashMap<String, PatternCmd> patterns = new HashMap<String, PatternCmd>();

	public static void main(String[] args) {
		instance = new Main();
		instance.start();
	}
	
	public static CommandThread co_create(CommandVal val) {
		CommandThread ct = new CommandThread(val);
		synchronized (ct.otherLock) {
			ct.start();
			try {
				ct.otherLock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return ct;
	}
	
	private String rootUserID, token;
	public File saveDir;
	public IDiscordClient client;
	public Map<String, Boolean> commandMode;
	public Map<String, CommandVal[]> commandSpam;
	public Map<String, List<MSGStruct>> commandMsg;
	public Map<String, List<Perm>> perms;
	public List<PermGroup> groups;
	public Map<String, List<PermGroup>> permGroups;
	public Map<String, List<CommandThread>> coroutines;
	public MoneyManager moneyManager;
	private String ext = "conf";
	public CommandParser cp;
	private final Object eventEnd;
	private boolean sleeping;
	
	public Main() {
		eventEnd = new Object();
	}
	
	public void start() {
		loadConf();
		try {
			client = new ClientBuilder().withToken(token).build();
			client.login();
			commandMode = new HashMap<String, Boolean>();
			commandSpam = new HashMap<String, CommandVal[]>();
			commandMsg = new HashMap<String, List<MSGStruct>>();
			perms = new HashMap<String, List<Perm>>();
			groups = new ArrayList<PermGroup>();
			permGroups = new HashMap<String, List<PermGroup>>();
			coroutines = new HashMap<String, List<CommandThread>>();
			moneyManager = new MoneyManager();
			cp = new CommandParser();
			Commands.init();
			load();
			
			client.getDispatcher().registerListener(this);
		} catch (DiscordException | CommandException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		new Thread() {
			public void run() {
				synchronized (eventEnd) {
					save();
					try {
						client.logout();
					} catch (RateLimitException | DiscordException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public String replaceRight(String a, String b) {
		return a.substring(0, a.length() - b.length()) + b;
	}
	
	public boolean modeGet(String id) {
		if (!commandMode.containsKey(id))
			return false;
		return commandMode.get(id);
	}
	
	public boolean modeSet(String id, boolean v) {
		commandMode.put(id, v);
		return v;
	}
	
	public void showArr(Writer w, String[] arr, int arrw) throws IOException {
		int arrh = (arrw == 0 ? 0 : arr.length / arrw);
		int[] sizes = new int[arrw];
		if (arrh > 0) {
			for (int i = 0; i < arrw; i++) {
				sizes[i] = arr[i].length() - (arr[i].length() > 0 && "<=>".indexOf(arr[i].charAt(0)) >= 0 ? 1 : 0); // DUPLICATE
				for (int j = 0; j < arrh; j++)
					sizes[i] = Math.max(sizes[i], arr[j * arrw + i].length() - (arr[j * arrw + i].length() > 0 && "<=>".indexOf(arr[j * arrw + i].charAt(0)) >= 0 ? 1 : 0));
			}
		}
		int lineLen = 1;
		for (int i = 0; i < arrw; i++)
			lineLen += sizes[i] + 3;
		char[] line0 = new char[lineLen + 1],
			   line1 = new char[lineLen + 1];
		char[] total = new char[(lineLen + 1) * (arrh * 2 + 1)];
		for (int i = 0; i < lineLen; i++) {
			line0[i] = '-';
			line1[i] = ' ';
		}
		int ind = 0;
		for (int i = 0; i < arrw; i++) {
			line0[ind] = '+';
			line1[ind] = '|';
			ind += sizes[i] + 3;
		}
		line0[ind] = '+';
		line1[ind] = '|';
		line0[lineLen] = '\n';
		line1[lineLen] = '\n';
		int yind = 0;
		System.arraycopy(line0, 0, total, yind, lineLen + 1);
		yind += lineLen + 1;
		for (int y = 0; y < arrh; y++) {
			System.arraycopy(line1, 0, total, yind, lineLen + 1);
			int xind = 2;
			for (int x = 0; x < arrw; x++) {
				int side = 0;
				String cur = arr[y * arrw + x];
				if (cur.length() > 0) {
					switch (cur.charAt(0)) {
					case '<':
						cur = cur.substring(1);
						side = 0;
						break;
					case '=':
						cur = cur.substring(1);
						side = 1;
						break;
					case '>':
						cur = cur.substring(1);
						side = 2;
						break;
					}
				}
				int curInd = xind + yind;
				switch (side) {
				case 0:
					break;
				case 1:
					curInd += (sizes[x] - cur.length()) / 2;
					break;
				case 2:
					curInd += sizes[x] - cur.length();
					break;
				}
				System.arraycopy(cur.toCharArray(), 0, total, curInd, cur.length());
				xind += sizes[x] + 3;
			}
			yind += lineLen + 1;
			System.arraycopy(line0, 0, total, yind, lineLen + 1);
			yind += lineLen + 1;
		}
		w.write(total);
	}
	
	private static void patternPut(String name, String p, String v) {
		patterns.put(name, new PatternCmd(name, Pattern.compile(p), v));
	}
	
	private static Pattern[] dollars = new Pattern[10];
	static {
		for (int i = 0; i < dollars.length; i++)
			dollars[i] = dollarCrt(i);
	}
	
	private static Pattern dollarCrt(int i) {
		return Pattern.compile("\\$\\(" + i + "\\)");
	}
	
	private static Pattern dollarFor(int i) {
		return i < 10 ? dollars[i] : dollarCrt(i);
	}
	
	public void sleep(IChannel channel) {
		sleeping = true; // DIRTY
	}
	
	public void wakeup(IChannel channel) {
		sleeping = false; // DIRTY
	}
	
	public boolean isSleeping(IChannel channel) {
		return sleeping; // DIRTY
	}
	
	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) {
		boolean sleeping = isSleeping(event.getMessage().getChannel());
		synchronized (eventEnd) {
			callEvent("msg", new CommandVal[] { new CommandValStr(event.getMessage().getContent()) });
			String str = event.getMessage().getContent();
			String men = "<@" + client.getOurUser().getID() + ">";
			if (modeGet(event.getMessage().getAuthor().getID())) {
				// BLANK
			} else if (str.startsWith(men)) {
				str = str.substring(men.length());
			} else if (str.startsWith("!")) {
				str = str.substring("!".length());
			} else {
				if (!sleeping) {
					StringBuilder sb = new StringBuilder();
					String authorID = event.getMessage().getAuthor().getID();
					
					for (String name : patterns.keySet()) {
						PatternCmd pc = patterns.get(name);
						Matcher m = pc.p.matcher(" " + event.getMessage().getContent() + " ");
						while (m.find()) {
							String v = pc.v;
							v = v.replaceAll("\\$\\(author\\)", event.getMessage().getAuthor().getDisplayName(event.getMessage().getGuild()).replaceAll("\\$", "\\\\$"));
							for (int i = 0; i <= m.groupCount(); i++) {
								Matcher m2 = dollarFor(i).matcher(v);
								if (m2.find())
									v = m2.replaceAll(m.group(i).replaceAll("\\$", "\\\\$"));
							}
							sb.append(v + "\n");
						}
					}
					
					CommandVal[] msgs = commandSpam.get(authorID);
					if (msgs != null) {
						try {
							Commands.level = 0;
							Commands.opCount = 0;
							CommandVal msg = msgs[(int) (Math.random() * msgs.length)];
							sb.append(msg.runStr(msg.getEnv()) + "\n");
						} catch (CommandException e) {
							sb.append("```\nError: " + e.getLocalizedMessage() + "\n```\n");
						}
					}
					
					if (commandMsg.containsKey(authorID)) {
						List<MSGStruct> msgList = commandMsg.get(authorID);
						if (msgList.size() > 0) {
							for (MSGStruct msg : msgList) {
								try {
									sb.append("<@" + msg.sender + ">:\n" + msg.msg.runStr(msg.msg.getEnv()) + "\n");
								} catch (CommandException e) {
								}
							}
							msgList.clear();
						}
					}
					
					send(event.getMessage().getChannel(), sb.toString());
				}
				return;
			}
			if (!sleeping || hasPermission(event.getMessage().getAuthor().getID(), Perms.all.manage.cmdWhileAsleep)) {
				try {
					Commands.level = 0;
					Commands.opCount = 0;
					StringReader sr = new StringReader(str);
					EnvEach ee = new EnvEach(Commands.global, event.getMessage());
					List<IUser> users = event.getMessage().getGuild().getUsers();
					CommandVal[] userList = new CommandVal[users.size()];
					for (int i = 0; i < userList.length; i++)
						userList[i] = new CommandValStr(users.get(i).getID());
					ee.defSet("users", new CommandValList(userList));
					CommandThread ct = co_create(new CommandValEnv(ee, cp.parse(sr)));
					resumeLoop(ct, new CommandVal[0]);
					sr.close();
				} catch (ParserException | CommandException e) {
					sendError(event.getMessage().getChannel(), e.getLocalizedMessage());
				}
			}
			eventEnd.notifyAll();
		}
	}
	
	public void send(IChannel channel, String s) {
		if (s.length() > 2000)
			s = s.substring(0, 2000 - 3) + "...";
		if (s.length() == 0 || Utils.containsOnly(s, "\t\b\n\r\f "))
			return;
		try {
			channel.sendMessage(s);
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			e.printStackTrace();
		}
	}
	
	public void sendError(IChannel channel, String err) {
		send(channel, "```\nError: " + err + "\n```");
	}
	
	public Config getBotConf(ConfigParser parser) throws IOException, ParserException {
		File botFile = getBotFile();
		BufferedReader botReader = new BufferedReader(new FileReader(botFile));
		Config conf = parser.parse(botReader);
		botReader.close();
		if (!conf.exists("root-user-id")) {
			log.error("Missing root user id in bot." + ext + ". Aborting.");
			System.exit(0);
		}
		if (!conf.exists("bot-token")) {
			log.error("Missing bot token in bot." + ext + ". Aborting.");
			System.exit(0);
		}
		if (!conf.exists("save-dir")) {
			log.error("Missing save directory prefix in bot." + ext + ". Aborting.");
			System.exit(0);
		}
		if (!conf.exists("user-dir")) {
			log.error("Missing user directory in bot." + ext + ". Aborting.");
			System.exit(0);
		}
		return conf;
	}
	
	public File getBotFile() throws IOException {
		File botFile = new File(mainDir, "bot." + ext);
		if (!botFile.exists()) {
			Writer botWriter = new BufferedWriter(new FileWriter(botFile));
			genBotFile(botWriter);
			botWriter.close();
		} else if (!botFile.isFile()) {
			log.error("\"bot.conf\" isn't a file. Aborting.");
			System.exit(0);
		}
		return botFile;
	}
	
	public File getUserDir(Config conf) {
		File userDir = new File(new File(mainDir, conf.get("save-dir")), conf.get("user-dir"));
		if (!userDir.exists()) {
			userDir.mkdirs();
			genUserDir(userDir);
		} else if (!userDir.isDirectory()) {
			log.error("\"" + userDir.getAbsolutePath() + "\" isn't a directory. Aborting.");
			System.exit(0);
		}
		return userDir;
	}
	
	public void loadConf() {
		try {
			Utils.mkdirsOrCrash(mainDir);
			ConfigParser parser = new ConfigParser();
			Config botConf = getBotConf(parser);
			rootUserID = botConf.get("root-user-id");
			token = botConf.get("bot-token");
			saveDir = new File(mainDir, botConf.get("save-dir"));
			Utils.mkdirsOrCrash(saveDir);
		} catch (IOException | ConfigParserException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ParserException e) {
			log.error("Fatal Error: " + e.getLocalizedMessage());
			System.exit(0);
		}
	}
	
	public void load() {
		try {
			ConfigParser parser = new ConfigParser();
			Config botConf = getBotConf(parser);
			File userDir = getUserDir(botConf);
			for (File f : userDir.listFiles()) {
				if (f.getName().endsWith("." + ext)) {
					try {
						BufferedReader fReader = new BufferedReader(new FileReader(f));
						Config fconf = parser.parse(fReader);
						fReader.close();
						if (fconf.exists("cmd-mode") && fconf.get("cmd-mode").equals("true"))
							commandMode.put(f.getName().substring(0, f.getName().length() - ("." + ext).length()), true);
					} catch (ParserException e) {
						log.error("Error: " + e.getLocalizedMessage());
					}
				}
			}
			BufferedReader br;
			
			if (!new File(saveDir, "money.txt").exists())
				new File(saveDir, "money.txt").createNewFile();
			br = new BufferedReader(new FileReader(new File(saveDir, "money.txt")));
			moneyManager.load(br);
			br.close();
			
			if (!new File(saveDir, "patterns.txt").exists())
				new File(saveDir, "patterns.txt").createNewFile();
			br = new BufferedReader(new FileReader(new File(saveDir, "patterns.txt")));
			String line;
			while ((line = br.readLine()) != null) {
				int index = 0;
				String[] data = new String[3];
				for (int i = 0; i < data.length; i++) {
					int prev = index;
					while (index < line.length() && line.charAt(index) >= '0' && line.charAt(index) <= '9')
						index++;
					int len = Integer.parseInt(line.substring(prev, index));
					index++; // ";"
					data[i] = Utils.decodeWrite(line.substring(index, index + len));
					index += len;
					index++; // ";"
				}
				patternPut(data[0], data[1], data[2]);
			}
			br.close();
		} catch (IOException | ConfigParserException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ParserException e) {
			log.error("Fatal Error: " + e.getLocalizedMessage());
			System.exit(0);
		}
	}
	
	private String formatWithLength(String s) {
		return s.length() + ";" + s + ";";
	}
	
	public void save() {
		try {
			ConfigParser parser = new ConfigParser();
			Config botConf = getBotConf(parser);
			File userDir = getUserDir(botConf);
			for (String id : commandMode.keySet()) {
				Writer w = new BufferedWriter(new FileWriter(new File(userDir, id + "." + ext)));
				new Config(new ConfigNodeMap().put("cmd-mode", new ConfigNodeStr(commandMode.get(id).toString()))).save(w);
				w.close();
			}
			BufferedWriter bw;
			
			bw = new BufferedWriter(new FileWriter(new File(saveDir, "money.txt")));
			moneyManager.save(bw);
			bw.close();
			
			bw = new BufferedWriter(new FileWriter(new File(saveDir, "patterns.txt")));
			for (String name : patterns.keySet()) {
				PatternCmd pc = patterns.get(name);
				String ps = pc.p.toString();
				bw.write(formatWithLength(Utils.encodeWrite(pc.name)) + formatWithLength(Utils.encodeWrite(ps)) + formatWithLength(Utils.encodeWrite(pc.v)) + "\n");
			}
			bw.close();
		} catch (IOException | ConfigParserException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ParserException e) {
			log.error("Fatal Error: " + e.getLocalizedMessage());
			System.exit(0);
		}
	}
	
	private void genBotFile(Writer w) throws IOException {
		w.write("# Bot config file\n");
		w.write("\n");
		w.write("# Uncomment and add root user's id between quotes:\n");
		w.write("# root-user-id: \"\"\n");
		w.write("# Uncomment and add bot token between quotes:\n");
		w.write("# bot-token: \"\"\n");
		w.write("save-dir: \"save\"\n");
		w.write("user-dir: \"user_dir\"\n");
	}
	
	private void genUserDir(File dir) {
	}
	
	public void checkRoot(String id) throws CommandException {
		if (!id.equals(rootUserID))
			throw new CommandException("This command can only be executed by the root user.");
	}
	
	private boolean _hasPermission(String id, Perm perm) {
		List<PermGroup> gs = permGroups.get(id);
		if (gs != null) {
			for (PermGroup g : gs)
				if (g.hasPermission(perm))
					return true;
		}
		List<Perm> ps = perms.get(id);
		if (ps == null) {
			ps = new ArrayList<Perm>();
			ps.add(Perms.all);
			perms.put(id, ps);
		}
		if (ps != null) {
			for (Perm p : ps)
				if (perm.isAtMost(p))
					return true;
		}
		return false;
	}
	
	public boolean hasPermission(String id, Perm perm) {
		return id.equals(rootUserID) || _hasPermission(id, perm);
	}
	
	public void checkPermission(String id, Perm perm) throws CommandException {
		if (!hasPermission(id, perm))
			throw new CommandException("You don't have permission to run this command.");
	}
	
	public String username(String id) {
		return client.getUserByID(id).getName();
	}
	
	public List<CommandThread> coGet(String eventName) {
		if (!coroutines.containsKey(eventName))
			coroutines.put(eventName, new ArrayList<CommandThread>());
		return coroutines.get(eventName);
	}
	
	public void resumeLoop(CommandThread ct, CommandVal[] resumeArgs) {
		try {
			IChannel channel = ct.val.getEnv().getEach().msg.getChannel();
			try {
				CommandVal res = ct.co_resume(new CommandValList(resumeArgs));
				if (ct.dead) {
					send(channel, CommandVal.checkStr(res));
				} else {
					CommandValList resLs = (CommandValList) res;
					if (resLs.ls.length == 0)
						throw new CommandException("Event name expected.");
					coGet(resLs.ls[0].runStr(Commands.global)).add(ct); // DIRTY
				}
			} catch (CommandException e2) {
				sendError(channel, e2.getLocalizedMessage());
			}
		} catch (CommandException e1) {
			e1.printStackTrace();
		}
	}
	
	private void _callEvent(List<CommandThread> ls, String id, CommandVal[] resumeArgs) {
		for (CommandThread ct : ls)
			resumeLoop(ct, resumeArgs);
	}
	
	public void callEvent(String id, CommandVal[] args) {
		List<CommandThread> _l1 = coGet(id),
							_l2 = coGet("");
		List<CommandThread> l1 = new ArrayList<CommandThread>(_l1),
							l2 = new ArrayList<CommandThread>(_l2);
		CommandVal[] resumeArgs = new CommandVal[args.length + 1];
		resumeArgs[0] = new CommandValStr(id);
		System.arraycopy(args, 0, resumeArgs, 1, args.length);
		_l1.clear();
		_l2.clear();
		_callEvent(l1, id, resumeArgs);
		_callEvent(l2, "", resumeArgs);
	}

	public PermGroup getGroupByName(String name) {
		for (PermGroup g : groups)
			if (g.name.equals(name))
				return g;
		return null;
	}
	
	public Perm getPermByName(String name) {
		int ind = 0;
		Perm p = null, np = null;
		name = name + ".";
		while (ind < name.length()) {
			int beg = ind;
			while (name.charAt(ind) != '.')
				ind++;
			String node = name.substring(beg, ind);
			if (p == null) {
				if (node.equals("all"))
					np = Perms.all;
			} else {
				np = p.getChildByName(node);
			}
			if (np == null)
				return null;
			p = np;
			ind++;
		}
		return p;
	}
	
	private final Pattern userIDPattern = Pattern.compile("<@(.*)>");
	public String readUserID(String user) {
		Matcher m = userIDPattern.matcher(user);
		if (!m.matches())
			return null;
		return m.group(1);
	}
	
	public String checkUser(String user) throws CommandException {
		String id = readUserID(user);
		if (id == null)
			throw new CommandException("User expected, got \"" + user + "\".");
		return id;
	}
}
