package org.mve;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class Main
{
	public static void main(String[] args)
	{
		String cookie;
		try (FileInputStream fin = new FileInputStream("cookie"))
		{
			Scanner sc = new Scanner(fin);
			cookie = sc.nextLine();
		}
		catch (IOException e)
		{
			Danmuji.logger(null, e);
			return;
		}
		Danmuji danmu = new Danmuji(cookie, 12451731);
		Runtime.getRuntime().addShutdownHook(new Thread(danmu::close));
	}
}
