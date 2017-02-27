package fr.gaston147.dbot.command.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import fr.gaston147.dbot.Parser;
import fr.gaston147.dbot.ParserException;
import fr.gaston147.dbot.command.CommandVal;
import fr.gaston147.dbot.command.CommandValList;
import fr.gaston147.dbot.command.CommandValStr;

public class CommandParser extends Parser<CommandVal> {
	
	public CommandVal parse(Reader r) throws ParserException {
		try {
			super.parse(r);
			CommandValList root = new CommandValList(line, col, readArr());
			if (!out())
				throw new CommandParserException("Unexpected character \"" + c + "\" at " + (ind + 1) + ".");
			return root;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private CommandVal readVal() throws CommandParserException, IOException {
		int line = this.line, col = this.col;
		if (c == '"') {
			StringBuilder sb = new StringBuilder();
			nxt();
			boolean esc = false;
			while (!out()) {
				if (esc) {
					esc = false;
					switch (c) {
					case 't':
						c = '\t';
						break;
					case 'b':
						c = '\b';
						break;
					case 'n':
						c = '\n';
						break;
					case 'r':
						c = '\r';
						break;
					case 'f':
						c = '\f';
						break;
					case '"':
						c = '"';
						break;
					case '\\':
						c = '\\';
						break;
					default:
						throw new CommandParserException("Unexpected character \"" + c + "\".");
					}
				} else {
					if (c == '\\') {
						esc = true;
						nxt();
						continue;
					} else if (c == '"') {
						break;
					}
				}
				sb.append(c);
				nxt();
			}
			if (out())
				throw new CommandParserException("Unexpected end of file.");
			nxt();
			return new CommandValStr(line, col, sb.toString());
		} else if (c == '(') {
			nxt();
			CommandValList valList = new CommandValList(line, col, readArr());
			if (out())
				throw new CommandParserException("Unexpected end of file.");
			if (c != ')')
				throw new CommandParserException("Unexpected character \"" + c + "\" at " + (ind + 1) + ", expected \")\".");
			nxt();
			return valList;
		} else if (c == '[') {
			nxt();
			String id = readWrd();
			if (id.length() == 0)
				throw new CommandParserException("The id must be at least one character.");
			if (out())
				throw new CommandParserException("Unexpected end of file.");
			if (c != ']')
				throw new CommandParserException("Unexpected character \"" + c + "\" at " + (ind + 1) + ", expected \"]\".");
			nxt();
			if (out())
				throw new CommandParserException("Unexpected end of file.");
			if (c != '\n')
				throw new CommandParserException("Unexpected character \"" + c + "\" at " + (ind + 1) + ", expected line break.");
			nxt();
			StringBuilder sb = new StringBuilder();
			while (true) {
				if (out())
					throw new CommandParserException("Unexpected end of file.");
				if (c == '\n') {
					StringBuilder subsb = new StringBuilder();
					subsb.append(c);
					nxt();
					if (out())
						throw new CommandParserException("Unexpected end of file.");
					if (c == '[') {
						subsb.append(c);
						nxt();
						String curid = readWrd();
						subsb.append(curid);
						if (out())
							throw new CommandParserException("Unexpected end of file.");
						if (c == ']' && curid.equals(id)) {
							nxt();
							break;
						}
					}
					sb.append(subsb);
					continue;
				}
				sb.append(c);
				nxt();
			}
			return new CommandValStr(line, col, sb.toString());
		} else {
			String wrd = readWrd();
			if (wrd.length() == 0)
				return null;
			return new CommandValStr(line, col, wrd);
		}
	}
	
	public CommandVal[] readArr() throws CommandParserException, IOException {
		List<CommandVal> ls = new ArrayList<CommandVal>();
		while (!out()) {
			blk();
			CommandVal val = readVal();
			if (val == null)
				break;
			ls.add(val);
		}
		return ls.toArray(new CommandVal[ls.size()]);
	}
}
