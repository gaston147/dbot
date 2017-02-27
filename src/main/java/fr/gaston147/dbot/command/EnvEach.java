package fr.gaston147.dbot.command;

import fr.gaston147.dbot.DImage;
import sx.blah.discord.handle.obj.IMessage;

public class EnvEach extends Env {
	public static final long MAX_IMAGES = 16;
	
	public final IMessage msg;
	private final DImage[] imgs;
	
	public EnvEach(EnvGlobal global, IMessage msg) {
		super(global);
		this.msg = msg;
		imgs = new DImage[(int) MAX_IMAGES];
	}
	
	public EnvEach getEach() {
		return this;
	}
	
	public int newImageID() throws CommandException {
		for (int id = 0; id < MAX_IMAGES; id++)
			if (imgs[id] == null)
				return id;
		throw new CommandException("Can not create more than " + MAX_IMAGES + " images.");
	}
	
	public String addImage(DImage dimg) throws CommandException {
		int id = newImageID();
		imgs[id] = dimg;
		return Long.toString(id);
	}
	
	public String createImage(int w, int h) throws CommandException {
		int id = newImageID();
		imgs[id] = new DImage(w, h);
		return Long.toString(id);
	}
	
	private void imgDoesntExists(String sid) throws CommandException {
		throw new CommandException("Image with id \"" + sid + "\" doesn't exists.");
	}
	
	private int nativeId(String sid) throws CommandException {
		try {
			int id = Integer.parseInt(sid);
			if (id < 0 || id >= MAX_IMAGES || imgs[id] == null)
				imgDoesntExists(sid);
			return id;
		} catch (NumberFormatException e) {
			imgDoesntExists(sid);
		}
		return -1;
	}
	
	public void deleteImage(String sid) throws CommandException {
		imgs[nativeId(sid)] = null;
	}
	
	public DImage getImage(String sid) throws CommandException {
		return imgs[nativeId(sid)];
	}
}
