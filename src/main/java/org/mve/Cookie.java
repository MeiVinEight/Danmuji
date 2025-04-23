package org.mve;

import java.util.HashMap;
import java.util.Map;

public class Cookie
{
	private final Map<String, String> cookie = new HashMap<>();

	public Cookie(String cookie)
	{
		String[] split = cookie.split(";");
		for (String kv : split)
		{
			String[] keyValue = kv.split("=");
			this.cookie.put(keyValue[0].trim(), keyValue[1].trim());
		}
	}

	public String get(String key)
	{
		return this.cookie.get(key);
	}

	public String cookie()
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : cookie.entrySet())
		{
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
		}
		return sb.toString();
	}
}
// #001F99