package org.mve;

import org.fusesource.jansi.Ansi;
import org.mve.ws.WebSocket;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.zip.Inflater;

public class Wednesday extends Synchronize
{
	public static final int PING_PERIOD = 20;
	public static final int STAT_OVERED  = 0;
	public static final int STAT_LENGTH  = 1;
	public static final int STAT_PAYLOAD = 2;
	private static boolean special = false;
	public final int SID;
	public final int RID;
	public final WebSocket socket;
	private long ping = 0;
	private final Array array = new Array(4096);
	private final byte[] buffer = new byte[4096];
	private int length = 0;
	private int status = 0;
	private final SynchronizeNET synchronize = new SynchronizeNET();

	public Wednesday(String cookie, int shortId)
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
			if (System.currentTimeMillis() - (Wednesday.PING_PERIOD * 1000) > this.ping)
			{
				this.ping();
				this.ping = System.currentTimeMillis();
			}

			switch (this.status)
			{
				case Wednesday.STAT_OVERED:
				{
					this.length = 4;
					this.status = Wednesday.STAT_LENGTH;
				}
				case Wednesday.STAT_LENGTH:
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
					this.status = Wednesday.STAT_PAYLOAD;
				}
				case Wednesday.STAT_PAYLOAD:
				{
					int read = this.socket.read(this.buffer, 0, Math.min(this.length, this.buffer.length));
					this.array.put(this.buffer, 0, read);
					this.length -= read;
					if (this.length > 0)
						return;
					this.status = Wednesday.STAT_OVERED;
					byte[] buf = new byte[this.array.length()];
					this.array.get(buf);
					Wednesday.message(Message.resolve(buf));
				}
			}
		}
		catch (Throwable t)
		{
			Wednesday.message(null, t);
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
			String msg = Ansi.ansi()
				.bold()
				.a(base.string("name") + ": ")
				.reset()
				.a(extra.string("content"))
				.reset()
				.toString();
			if (medal != null)
				msg = Wednesday.medal(medal) + " " + msg;
			Wednesday.message(msg);
		}
		else if ("INTERACT_WORD".equals(object.string("cmd")))
		{
			Json data = object.get("data");
			Json medal = data.get("uinfo").get("medal");
			String msg = Ansi.ansi()
				.bold()
				.fg(Ansi.Color.CYAN)
				.a(data.string("uname"))
				.fgBright(Ansi.Color.BLACK)
				.a(" 进入直播间")
				.reset()
				.toString();
			if (medal != null)
				msg = Wednesday.medal(medal) + " " + msg;
			Wednesday.special(msg);
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
			String msg = Ansi.ansi()
				.bold()
				.fg(Ansi.Color.CYAN)
				.a(uname)
				.reset()
				.a(" ")
				.bold()
				.fgBright(Ansi.Color.BLACK)
				.a(action)
				.reset()
				.a(" ")
				.toString();
			if (medal != null)
				msg = Wednesday.medal(medal) + " " + msg;
			Json blindGift = data.get("blind_gift");
			if (blindGift != null)
			{
				action = blindGift.string("gift_action");
				String originGift = blindGift.string("original_gift_name");
				msg += Ansi.ansi()
					.bold()
					.fg(Ansi.Color.YELLOW)
					.a(originGift)
					.fgBright(Ansi.Color.BLACK)
					.a(" " + action + " ")
					.fg(Ansi.Color.YELLOW)
					.a(giftName)
					.reset();
			}
			else
				msg += Ansi.ansi()
					.bold()
					.fg(Ansi.Color.YELLOW)
					.a(giftName)
					.reset();
			if (count > 1)
				msg += Ansi.ansi()
					.bold()
					.fg(Ansi.Color.YELLOW)
					.a(count)
					.reset();
			Wednesday.message(msg);
		}
		else if ("WATCHED_CHANGE".equals(object.string("cmd")))
		{
			Json data = object.get("data");
			long num = data.number("num").longValue();
			String msg = Ansi.ansi()
				.bold()
				.fgBright(Ansi.Color.BLACK)
				.a(num + " Watch")
				.reset()
				.toString();
			Wednesday.special(msg);
		}
		else if ("LIKE_INFO_V3_UPDATE".equals(object.string("cmd")))
		{
			Json data = object.get("data");
			long clickCount = data.number("click_count").longValue();
			String msg = Ansi.ansi()
				.bold()
				.fgBright(Ansi.Color.BLACK)
				.a(clickCount + " Like")
				.reset()
				.toString();
			Wednesday.special(msg);
		}
		/*
		else
		{
			String cmd = object.string("cmd");
			String msg = object.stringify();
			if (cmd != null)
				msg = cmd + ": " + msg;
			Danmuji.message(msg);
		}
		*/
	}

	public static void message(Message datapack)
	{
		if (datapack.proto == Message.PROTO_PING)
		{
			if (datapack.type == Message.TYPE_PING)
				Wednesday.message("SERVER PING");
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
		Wednesday.message(Json.resolve(json));
	}

	public static String medal(Json medal)
	{
		boolean bright = true;
		Ansi.Color color = Ansi.Color.BLACK;
		int level = medal.number("level").intValue();
		if (level >= 20)
		{
			bright = false;
			color = Ansi.Color.CYAN;
		}
		else if (level >= 17)
		{
			color = Ansi.Color.RED;
		}
		else if (level >= 13)
		{
			bright = false;
			color = Ansi.Color.MAGENTA;
		}
		else if (level >= 9)
		{
			color = Ansi.Color.BLUE;
		}
		else if (level >= 5)
		{
			bright = false;
			color = Ansi.Color.BLUE;
		}
		String fg = bright ? Ansi.ansi().fgBright(color).toString() : Ansi.ansi().fg(color).toString();
		String bg = bright ? Ansi.ansi().bgBright(color).toString() : Ansi.ansi().bg(color).toString();
		return Ansi.ansi()
			.bold()
			.a(bg)
			.fgBright(Ansi.Color.WHITE)
			.a(" " + medal.string("name") + " ")
			.a(fg)
			.bgBright(Ansi.Color.WHITE)
			.a(" " + medal.number("level") + " ")
			.reset()
			.toString();
	}

	public static void message(String msg, Throwable t)
	{
		if (Wednesday.special)
		{
			Wednesday.special = false;
			System.out.print(Ansi.ansi().reset().cursorToColumn(0).eraseLine());
		}
		System.out.print(Wednesday.timestamp());
		if (msg != null)
			System.out.println(msg);
		if (t != null)
			t.printStackTrace(System.out);
		if (msg == null && t == null)
			System.out.println();
	}

	public static void message(String msg)
	{
		Wednesday.message(msg, null);
	}

	public static void special(String msg)
	{
		special = true;
		msg = Wednesday.timestamp() + msg;
		System.out.print(Ansi.ansi().cursorToColumn(0).eraseLine().a(msg));
	}

	public static String timestamp()
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
		return new String(buf);
	}
}
