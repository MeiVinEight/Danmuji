package org.mve;

import org.fusesource.jansi.Ansi;

import java.util.LinkedList;
import java.util.Queue;

public class Watching extends Synchronize
{
	public static final long JOIN_DELAY = 20;
	private long timing = 0;
	private final Queue<Json> jointness = new LinkedList<>();
	private long delay = Watching.JOIN_DELAY;
	private long joiner = this.delay;
	public long watching = 0;
	public long liking = 0;

	public void join(Json joiner)
	{
		if (this.jointness.isEmpty())
			this.joiner = Watching.JOIN_DELAY;
		this.jointness.add(joiner);
	}

	@Override
	public void run()
	{
		if (this.joiner == 0)
		{
			this.jointness.poll();
			if (this.delay > 1 && this.jointness.size() > (Watching.JOIN_DELAY - this.delay + 1))
				this.delay--;
			else if (this.delay < Watching.JOIN_DELAY)
				this.delay++;
			this.joiner = this.delay;
		}
		this.joiner--;

		Ansi ansi = Ansi
			.ansi()
			.bold()
			.fgBright(Ansi.Color.BLACK);

		if (((timing / 100) & 1) != 0)
			ansi.a(this.watching + " 观看");
		else
			ansi.a(this.liking + " 点赞");
		ansi.reset();

		Json joiner = this.jointness.peek();
		if (joiner != null)
		{
			ansi.a(" ");
			Json medal = joiner.get("uinfo").get("medal");
			if (medal != null)
				ansi.a(Wednesday.medal(medal)).a(" ");
			ansi.bold()
				.fg(Ansi.Color.CYAN)
				.a(joiner.string("uname"))
				.fgBright(Ansi.Color.BLACK)
				.a(" 进入直播间")
				.reset();
		}

		Wednesday.special(ansi.toString());
		this.timing++;
	}
}
