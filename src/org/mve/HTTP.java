package org.mve;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTP
{
	public static byte[] request(String url, String cookie)
	{
		Array buf = new Array(4096);
		try
		{
			URL uurl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) uurl.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0");
			conn.setRequestProperty("Cookie", cookie);
			byte[] bbb = new byte[4096];
			InputStream in = conn.getInputStream();
			int read = in.read(bbb);
			while (read != -1)
			{
				buf.put(bbb, 0, read);
				read = in.read(bbb);
			}
		}
		catch (Throwable e)
		{
			JavaVM.exception(e);
		}
		byte[] data = new byte[buf.length()];
		buf.get(data);
		return data;
	}
}
