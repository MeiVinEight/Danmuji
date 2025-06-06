package org.mve;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class SynchronizeNET implements Runnable
{
	public static final long PERIOD_MS = 50;
	private final Queue<Synchronize> queue = new ConcurrentLinkedQueue<>();
	private boolean running = true;

	@Override
	public void run()
	{
		long nextTime = System.nanoTime() + (SynchronizeNET.PERIOD_MS * 1_000_000);
		while (this.running)
		{
			this.synchronize();
			long now = System.nanoTime();
			if (now < nextTime)  LockSupport.parkNanos(nextTime - now);
			nextTime += (SynchronizeNET.PERIOD_MS * 1_000_000);
		}
	}

	private void synchronize()
	{
		int count = this.queue.size();
		while (count --> 0)
		{
			Synchronize task = this.queue.poll();
			if (task == null) continue;
			if (task.cancelled) continue;

			if (task.delay == 0)
			{
				try
				{
					task.run();
				}
				catch (Throwable ignored)
				{
				}
				task.delay = task.period;
			}
			if (task.delay == 0)
				continue;
			task.delay--;
			this.queue.offer(task);
		}
	}

	public void offer(Synchronize task)
	{
		this.queue.offer(task);
	}

	public void close()
	{
		this.running = false;
	}
}
