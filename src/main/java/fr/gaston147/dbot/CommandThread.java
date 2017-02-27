package fr.gaston147.dbot;

import fr.gaston147.dbot.command.CommandException;
import fr.gaston147.dbot.command.CommandVal;

public class CommandThread extends Thread {
	public boolean dead;
	public CommandVal val, resumeArgs, yieldArgs;
	public final Object selfLock;
	public final Object otherLock;
	private CommandException e;

	public CommandThread(CommandVal val) {
		dead = false;
		this.val = val;
		resumeArgs = null;
		yieldArgs = null;
		selfLock = new Object();
		otherLock = new Object();
	}

	public CommandVal co_resume(CommandVal args) throws CommandException {
		if (dead)
			throw new CommandException("Coroutine can not be resumed when it's dead.");
		resumeArgs = args;
		synchronized (otherLock) {
			synchronized (selfLock) {
				selfLock.notifyAll();
			}
			try {
				otherLock.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (e != null)
				throw e;
		}
		return yieldArgs;
	}
	
	public CommandVal co_yield(CommandVal args) {
		yieldArgs = args;
		synchronized (selfLock) {
			synchronized (otherLock) {
				otherLock.notifyAll();
			}
			if (!dead) {
				try {
					selfLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return resumeArgs;
	}
	
	public void run() {
		co_yield(CommandVal.EMPTY_STR);
		CommandVal res = null;
		try {
			res = val.run(val.getEnv());
			e = null;
		} catch (CommandException e) {
			res = CommandVal.EMPTY_STR;
			this.e = e;
		}
		dead = true;
		co_yield(res);
	}
	
	public void send(String s) {
	}
}
