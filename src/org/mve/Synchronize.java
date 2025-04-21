package org.mve;

public abstract class Synchronize implements Runnable
{
	public long delay = 0;
	public long period = 1;
	public boolean cancelled = false;

	public void cancel()
	{
		this.cancelled = true;
	}
}
