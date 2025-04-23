package org.mve;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Main
{
	public static void main(String[] args)
	{
		int shortId = Integer.parseInt(args[0]);
		String cookie;
		try (FileInputStream fin = new FileInputStream("cookie"))
		{
			Scanner sc = new Scanner(fin);
			cookie = sc.nextLine();
		}
		catch (FileNotFoundException e)
		{
			Wednesday.message("Cookie file not found, input cookie: ");
			Scanner sc = new Scanner(System.in);
			cookie = sc.nextLine();
		}
		catch (IOException e)
		{
			Wednesday.message(null, e);
			return;
		}
		Wednesday danmu = new Wednesday(cookie, shortId);
		Runtime.getRuntime().addShutdownHook(new Thread(danmu::close));
	}
}
