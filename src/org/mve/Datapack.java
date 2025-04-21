package org.mve;

public class Datapack
{
	public static final int PROTO_UNCOMPRESSED = 0;
	public static final int PROTO_PING         = 1;
	public static final int PROTO_BRCOMPRESSED = 2;
	public static final int TYPE_PING = 2; // C2S
	public static final int TYPE_PONG = 3; // S2C
	public static final int TYPE_MSG  = 5; // S2C
	public static final int TYPE_AUTH = 7; // C2S
	public static final int TYPE_FIN  = 8; // S2C

	public final int proto;
	public final int type;
	public final byte[] data;

	public Datapack(int proto, int type, byte[] data)
	{
		this.proto = proto;
		this.type = type;
		this.data = data.clone();
	}

	public byte[] array()
	{
		Array buf = new Array(this.data.length + 16);
		buf.integer(this.data.length + 16, 4);
		buf.integer(16, 2);
		buf.integer(this.proto, 2);
		buf.integer(this.type, 4);
		buf.integer(1, 4);
		buf.put(this.data);
		byte[] data = new byte[buf.length()];
		buf.get(data);
		return data;
	}

	public static Datapack resolve(byte[] data)
	{
		Array buf = new Array(data.length);
		buf.put(data);
		int len = (int) buf.integer(4);
		int hlen = (int) buf.integer(2);
		int proto = (int) buf.integer(2);
		int type = (int) buf.integer(4);
		int constVal = (int) buf.integer(4);
		byte[] payload = new byte[len - 16];
		buf.get(payload);
		return new Datapack(proto, type, payload);
	}
}
