package org.mve;

import org.mve.ws.WebSocket;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.zip.Inflater;

public class Danmuji extends Synchronize
{
	public static final int PING_PERIOD = 20;
	public static final int STAT_OVERED  = 0;
	public static final int STAT_LENGTH  = 1;
	public static final int STAT_PAYLOAD = 2;
	public static final String[] FGROUND = {
		"\u001B[30m",
		"\u001B[31m",
		"\u001B[32m",
		"\u001B[33m",
		"\u001B[34m",
		"\u001B[35m",
		"\u001B[36m",
		"\u001B[37m",
		"\u001B[90m",
		"\u001B[91m",
		"\u001B[92m",
		"\u001B[93m",
		"\u001B[94m",
		"\u001B[95m",
		"\u001B[96m",
		"\u001B[97m",
	};
	public static final String[] BGROUND = {
		"\u001B[40m",
		"\u001B[41m",
		"\u001B[42m",
		"\u001B[43m",
		"\u001B[44m",
		"\u001B[45m",
		"\u001B[46m",
		"\u001B[47m",
		"\u001B[100m",
		"\u001B[101m",
		"\u001B[102m",
		"\u001B[103m",
		"\u001B[104m",
		"\u001B[105m",
		"\u001B[106m",
		"\u001B[107m",
	};
	public static final String COLOR_CLEAR = "\u001B[0m";
	public final int SID;
	public final int RID;
	public final WebSocket socket;
	private long ping = 0;
	private final Array array = new Array(4096);
	private final byte[] buffer = new byte[4096];
	private int length = 0;
	private int status = 0;
	private final SynchronizeNET synchronize = new SynchronizeNET();

	public Danmuji(String cookie, int shortId)
	{
		Cookie ck = new Cookie(cookie);
		this.SID = shortId;
		String str = new String(HTTP.request("https://api.live.bilibili.com/room/v1/Room/room_init?id=" + shortId, cookie));
		Json roominit = Json.resolve(str);
		this.RID = roominit.get("data").get("room_id").number().intValue();
		str = new String(HTTP.request("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo?id=" + this.RID, cookie));
		Json roominfo = Json.resolve(str);
		String token = roominfo.get("data").get("token").string();
		Json auth = Json.resolve("{\"protover\":2,\"platform\":\"web\",\"type\":2}");
		auth.set("uid", Long.valueOf(ck.get("DedeUserID")));
		 auth.set("buvid", ck.get("buvid3"));
		auth.set("roomid", this.RID);
		auth.set("key", token);
		Json hosts = roominfo.get("data").get("host_list").get(0);
		String host = hosts.get("host").string();
		int port = hosts.get("ws_port").number().intValue();
		String addr = "ws://" + host + ":" + port + "/sub";
		this.socket = new WebSocket(addr);
		this.socket.finish();
		Danmuji.logger("Auth: " + auth.stringify());
		Message datapack = new Message(Message.PROTO_UNCOMPRESSED, Message.TYPE_AUTH, auth.stringify().getBytes(StandardCharsets.UTF_8));
		byte[] data = datapack.array();
		this.socket.write(data, 0, data.length);
		this.socket.blocking(false);
		this.synchronize.offer(this);
		new Thread(this.synchronize).start();
	}

	public void ping()
	{
		byte[] data = new Message(Message.PROTO_PING, Message.TYPE_PING, new byte[0]).array();
		this.socket.write(data, 0, data.length);
	}

	public void close()
	{
		this.cancel();
		this.synchronize.close();
		this.socket.blocking(true);
		this.socket.close();
	}

	@Override
	public void run()
	{
		try
		{
			if (System.currentTimeMillis() - (Danmuji.PING_PERIOD * 1000) > this.ping)
			{
				this.ping();
				this.ping = System.currentTimeMillis();
			}

			switch (this.status)
			{
				case Danmuji.STAT_OVERED:
				{
					this.length = 4;
					this.status = Danmuji.STAT_LENGTH;
				}
				case Danmuji.STAT_LENGTH:
				{
					int read = this.socket.read(this.buffer, 0, this.length);
					if (read == -1)
						this.close();
					this.array.put(this.buffer, 0, read);
					this.length -= read;
					if (this.length > 0)
						return;
					this.length = (int) this.array.integer(4);
					this.array.integer(this.length, 4);
					this.length -= 4;
					this.status = Danmuji.STAT_PAYLOAD;
				}
				case Danmuji.STAT_PAYLOAD:
				{
					int read = this.socket.read(this.buffer, 0, Math.min(this.length, this.buffer.length));
					this.array.put(this.buffer, 0, read);
					this.length -= read;
					if (this.length > 0)
						return;
					this.status = Danmuji.STAT_OVERED;
					byte[] buf = new byte[this.array.length()];
					this.array.get(buf);
					Danmuji.message(Message.resolve(buf));
				}
			}
		}
		catch (Throwable t)
		{
			Danmuji.logger(null, t);
			this.close();
		}
	}

	public static void message(Json object)
	{
		if ("DANMU_MSG".equals(object.string("cmd")))
		{
			Json info = object.get("info").get(0).get(15);
			Json extra = Json.resolve(info.string("extra"));
			Json user = info.get("user");
			Json medal = user.get("medal");
			Json base = user.get("base");
			String msg = "\u001B[1m" + base.string("name") + ": \u001B[0m" + extra.string("content");
			if (medal != null)
				msg = Danmuji.medal(medal) + " " + msg;
			Danmuji.logger(msg);
		}
		else if ("INTERACT_WORD".equals(object.string("cmd")))
		{
			Json data = object.get("data");
			Json medal = data.get("uinfo").get("medal");
			String msg = "\u001B[1m" + FGROUND[6] + data.string("uname") + FGROUND[8] + " 进入直播间";
			if (medal != null)
				msg = Danmuji.medal(medal) + " " + msg;
			Danmuji.logger(msg);
		}
		else if ("SEND_GIFT".equals(object.string("cmd")))
		{
			Json data = object.get("data");
			int count = data.number("num").intValue();
			String action = data.string("action");
			String giftName = data.string("giftName");
			String uname = data.string("uname");
			Json sender = data.get("sender_uinfo");
			Json medal = sender.get("medal");
			String msg = "\u001B[1m" + FGROUND[6] + uname + COLOR_CLEAR + " \u001B[1m" + FGROUND[8] + action + COLOR_CLEAR;
			if (medal != null)
				msg = Danmuji.medal(medal) + " " + msg;
			Json blindGift = data.get("blind_gift");
			if (blindGift != null)
			{
				action = blindGift.string("gift_action");
				String originGift = blindGift.string("original_gift_name");
				msg += FGROUND[3] + "\u001B[1m " + originGift + COLOR_CLEAR + " \u001B[1m" + FGROUND[8] + action + COLOR_CLEAR
					+ FGROUND[3] + "\u001B[1m " + giftName;
			}
			else
				msg += FGROUND[3] + "\u001B[1m " + giftName;
			msg += COLOR_CLEAR;
			if (count > 1)
				msg += FGROUND[3] + "\u001B[1m ×" + count + COLOR_CLEAR;
			Danmuji.logger(msg);
		}
		else
		{
			String cmd = object.string("cmd");
			String msg = object.stringify();
			if (cmd != null)
				msg = cmd + ": " + msg;
			Danmuji.logger(msg);
		}
	}

	public static void message(Message datapack)
	{
		if (datapack.proto == Message.PROTO_PING)
		{
			if (datapack.type == Message.TYPE_PING)
				Danmuji.logger("SERVER PING");
			return;
		}
		if (datapack.proto == Message.PROTO_BRCOMPRESSED)
		{
			// Uncompressing data
			Inflater inflater = new Inflater();
			inflater.reset();
			inflater.setInput(datapack.data);
			Array buf = new Array(inflater.getRemaining());
			byte[] buf2 = new byte[4096];
			try
			{
				while (!inflater.finished())
				{
					int count = inflater.inflate(buf2);
					buf.put(buf2, 0, count);
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace(System.out);
			}
			inflater.end();
			Array arr = new Array(4);
			while (buf.length() > 0)
			{
				int len = (int) buf.integer(4);
				arr.integer(len, 4);
				byte[] data = new byte[len];
				arr.get(data, 0, 4);
				buf.get(data, 4, len - 4);
				Message pack = Message.resolve(data);
				message(pack);
			}
			return;
		}
		String json = new String(datapack.data, StandardCharsets.UTF_8);
		// Danmuji.logger(json);
		Danmuji.message(Json.resolve(json));
	}

	public static String medal(Json medal)
	{
		String fg = Danmuji.FGROUND[8];
		String bg = Danmuji.BGROUND[8];
		int level = medal.number("level").intValue();
		if (level >= 20)
		{
			fg = Danmuji.FGROUND[6];
			bg = Danmuji.BGROUND[6];
		}
		else if (level >= 17)
		{
			fg = Danmuji.FGROUND[9];
			bg = Danmuji.BGROUND[9];
		}
		else if (level >= 13)
		{
			fg = Danmuji.FGROUND[5];
			bg = Danmuji.BGROUND[5];
		}
		else if (level >= 9)
		{
			fg = Danmuji.FGROUND[12];
			bg = Danmuji.BGROUND[12];
		}
		else if (level >= 5)
		{
			fg = Danmuji.FGROUND[4];
			bg = Danmuji.BGROUND[4];
		}
		return bg + "\u001B[1m" + Danmuji.FGROUND[15] + " " + medal.string("name") + " " +
			fg + Danmuji.BGROUND[15] + " " + medal.number("level") + " " +
			Danmuji.COLOR_CLEAR;
	}

	public static void logger(String msg, Throwable t)
	{
		LocalDateTime time = LocalDateTime.now();
		long ss = time.getSecond();
		long mm = time.getMinute();
		long hh = time.getHour();
		char[] buf = {
			'[',
			(char) ((hh / 10) + '0'),
			(char) ((hh % 10) + '0'),
			':',
			(char) ((mm / 10) + '0'),
			(char) ((mm % 10) + '0'),
			':',
			(char) ((ss / 10) + '0'),
			(char) ((ss % 10) + '0'),
			']',
			' '
		};
		System.out.print(buf);
		if (msg != null)
			System.out.println(msg + COLOR_CLEAR);
		if (t != null)
			t.printStackTrace(System.out);
		if (msg == null && t == null)
			System.out.println();
	}

	public static void logger(String msg)
	{
		Danmuji.logger(msg, null);
	}
}
