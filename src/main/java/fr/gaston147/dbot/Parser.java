package fr.gaston147.dbot;

import java.io.IOException;
import java.io.Reader;

public abstract class Parser<T> {
	protected Reader r;
	protected int ind, line, col;
	protected boolean eof;
	protected char c;
	
	public T parse(Reader r) throws ParserException {
		try {
			this.r = r;
			ind = -1;
			line = -1;
			col = 0;
			eof = false;
			c = '\n';
			nxt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected void nxt() throws IOException {
		if (!eof) {
			if (c == '\n') {
				line++;
				col = 0;
			} else {
				col++;
			}
			int ic = r.read();
			ind++;
			if (ic == -1)
				eof = true;
			else
				c = (char) ic;
		}
	}
	
	protected boolean out() {
		return eof;
	}
	
	protected boolean isBlk(char c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}
	
	protected boolean isLetter(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}
	
	protected boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	protected boolean isWrd(char c) {
		return isLetter(c) || isDigit(c) || c == '_' || c == '+' || c == '-' || c == '*' || c == '/' || c == '.' || c == '<' || c == '@' || c == '>' || c == '=' || c == '!' || c == '&' || c == '|';
	}
	
	protected String blk() throws IOException {
		StringBuilder sb = new StringBuilder();
		while (!out() && isBlk(c)) {
			sb.append(c);
			nxt();
		}
		return sb.toString();
	}
	
	protected String readWrd() throws IOException {
		StringBuilder sb = new StringBuilder();
		while (!out() && isWrd(c)) {
			sb.append(c);
			nxt();
		}
		return sb.toString();
	}
}
