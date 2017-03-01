package fr.gaston147.dbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils {
	
	public static boolean containsOnly(String s, String chars) {
		for (int i = 0; i < s.length(); i++)
			if (chars.indexOf(s.charAt(i)) == -1)
				return false;
		return true;
	}
	
	public static String renderImage(DImage img, int h) {
		int w = img.w * h * 2 / img.h;
		StringBuilder sb = new StringBuilder();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++)
				sb.append((img.getRGBA(x * img.w / w, y * img.h / h) == 0xffffffff ? " " : "\u2588"));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static int clamp(int n, int min, int max) {
		return (n < min ? min : (n > max ? max : n));
	}
	
	public static String addZero(String s, int len) {
		char[] newStr = new char[Math.max(s.length(), 2)];
		int diff = newStr.length - s.length();
		System.arraycopy(s.toCharArray(), 0, newStr, diff, s.length());
		for (int i = 0; i < diff; i++)
			newStr[i] = '0';
		return new String(newStr);
	}
	
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String urlDecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readFile(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		StringBuilder sb = new StringBuilder();
		while (sc.hasNextLine())
			sb.append(sc.nextLine());
		sc.close();
		return sb.toString();
	}
	
	public static void mkdirsOrCrash(File d) {
		if (!d.exists())
			d.mkdirs();
		if (!d.isDirectory())
			throw new RuntimeException("\"" + d.getAbsolutePath() + "\" must be a directory. Aborting.");
	}
	
	public static void touchOrCrash(File f) throws IOException {
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		if (!f.isFile())
			throw new RuntimeException("\"" + f.getAbsolutePath() + "\" must be a file. Aborting.");
	}
	
	public static byte[] byteArraySlice(byte[] chars, int ind, int len) {
		byte[] res = new byte[len];
		System.arraycopy(chars, ind, res, 0, len);
		return res;
	}
	
	public static void addToByteList(List<Byte> bytes, byte[] arr) {
		for (byte b : arr)
			bytes.add(b);
	}
	
	public static byte[] toByteArray(List<Byte> bytes) {
		byte[] res = new byte[bytes.size()];
		int ind = 0;
		for (Byte b : bytes)
			res[ind++] = b;
		return res;
	}
	
	public static String encodeWrite(String s) {
		try {
			byte[] chars = s.getBytes("UTF-8");
			List<Byte> res = new ArrayList<Byte>();
			int index = 0;
			while (index < chars.length) {
				int beg = index;
				while (index < chars.length && chars[index] >= 32 && chars[index] <= 126 && chars[index] != '%' && chars[index] != ';')
					index++;
				if (index - beg > 0) {
					addToByteList(res, byteArraySlice(chars, beg, index - beg));
				} else {
					res.add((byte) '%');
					addToByteList(res, Utils.addZero(Integer.toString(chars[index] & 0xFF, 16).toUpperCase(), 2).getBytes());
					index++;
				}
			}
			return new String(toByteArray(res), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String decodeWrite(String s) {
		try {
			List<Byte> res = new ArrayList<Byte>();
			int index = 0;
			while (index < s.length()) {
				int beg = index;
				while (index < s.length() && s.charAt(index) != '%')
					index++;
				if (index - beg > 0) {
					addToByteList(res, s.substring(beg, index).getBytes("UTF-8"));
				} else {
					addToByteList(res, new byte[] { (byte) Integer.parseInt(s.substring(index + 1, index + 3), 16) });
					index += 3;
				}
			}
			return new String(toByteArray(res), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
