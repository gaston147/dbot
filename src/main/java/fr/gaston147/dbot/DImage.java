package fr.gaston147.dbot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import fr.gaston147.dbot.command.CommandException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class DImage {
	
	public static DImage loadFromUrl(URL url) throws IOException {
		InputStream bis = new BufferedInputStream(url.openStream());
		DImage dimg = new DImage(ImageIO.read(bis));
		bis.close();
		return dimg;
	}
	
	public final int w, h;
	private BufferedImage bi;
	private Graphics2D g;
	public Font nativeFont;
	
	private DImage(int w, int h, BufferedImage bi) {
		this.w = w;
		this.h = h;
		this.bi = bi;
		nativeFont = new Font("Comic Sans MS", Font.PLAIN, 40);
		g = bi.createGraphics();
		g.setColor(Color.RED);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	public DImage(int w, int h) {
		this(w, h, new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				bi.setRGB(x, y, 0xffffffff);
	}
	
	public DImage(BufferedImage bi) {
		this(bi.getWidth(), bi.getHeight(), bi);
	}
	
	public void save(OutputStream out) throws IOException {
		ImageIO.write(bi, "png", out);
	}
	
	public void drawText(DFont font, int x, int y, String text) {
		g.setFont(nativeFont);
		g.drawString(text, x, y);
	}
	
	public void setRGBA(int x, int y, int c) {
		bi.setRGB(x, y, c);
	}
	
	public int getRGBA(int x, int y) {
		return bi.getRGB(x, y);
	}

	public void show(IChannel channel) throws CommandException {
		try {
				File ftemp = new File(Main.instance.saveDir, "temp.png");
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(ftemp));
				save(fos);
				fos.close();
				channel.sendFile(ftemp);
		} catch (IOException | MissingPermissionsException | RateLimitException | DiscordException e) {
			throw new CommandException(e.getLocalizedMessage());
		}
	}
	
	public int fontWidth(String line) {
		g.setFont(nativeFont);
		return g.getFontMetrics().stringWidth(line);
	}
}
