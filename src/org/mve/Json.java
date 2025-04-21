package org.mve;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Json
{
	public static final int TYPE_OBJECT  = 0;
	public static final int TYPE_ARRAY   = 1;
	public static final int TYPE_STRING  = 2;
	public static final int TYPE_NUMBER  = 3;
	public static final int TYPE_BOOLEAN = 4;

	public static final byte[] HEXD = "0123456789abcdef".getBytes();
	public static final byte[] ESCAPE = {
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		'b' , 't' , 'n' , 0x00, 'f' , 'r' , 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, '\"', 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, '\\', 0x00, 0x00, 0x00,
	};

	public final int type;
	private Object value = null;

	public Json(int type)
	{
		if (type < Json.TYPE_OBJECT || type > Json.TYPE_BOOLEAN)
			throw new IllegalArgumentException("Unknown type " + type);

		this.type = type;
		if (this.type == TYPE_OBJECT)
			this.value = new HashMap<String, Json>();
		else if (this.type == TYPE_ARRAY)
			this.value = new Json[0];
	}

	public Json()
	{
		this(Json.TYPE_OBJECT);
	}

	public Json(String s)
	{
		this(Json.TYPE_STRING);
		this.value = s;
	}

	public Json(Number n)
	{
		this(Json.TYPE_NUMBER);
		this.value = n;
	}

	public Json(boolean b)
	{
		this(Json.TYPE_BOOLEAN);
		this.value = b;
	}

	public Json get(String str)
	{
		if (this.type != Json.TYPE_OBJECT)
			throw new IllegalArgumentException("Json not component");
		return (Json) ((HashMap<?, ?>) this.value).get(str);
	}

	public String string(String str)
	{
		Json json = this.get(str);
		if (json == null)
			return null;
		return json.string();
	}

	public Number number(String str)
	{
		return this.get(str).number();
	}

	public boolean bool(String str)
	{
		return this.get(str).bool();
	}

	@SuppressWarnings("all")
	public void set(String key, Json value)
	{
		if (this.type != Json.TYPE_OBJECT)
			throw new IllegalArgumentException("Json not component");
		((HashMap<Object, Object>) this.value).put(key, value);
	}

	public void set(String key, String value)
	{
		this.set(key, new Json(value));
	}

	public void set(String key, Number value)
	{
		this.set(key, new Json(value));
	}

	public void set(String key, boolean value)
	{
		this.set(key, new Json(value));
	}

	public void remove(String key)
	{
		if (this.type != Json.TYPE_OBJECT)
			throw new IllegalArgumentException("Json not component");
		((HashMap<?, ?>) this.value).remove(key, value);
	}

	public Json get(int idx)
	{
		if (this.type != Json.TYPE_ARRAY)
			throw new IllegalArgumentException("Json not array");
		return ((Json[]) this.value)[idx];
	}

	public String string(int idx)
	{
		return this.get(idx).string();
	}

	public Number number(int idx)
	{
		return this.get(idx).number();
	}

	public boolean bool(int idx)
	{
		return this.get(idx).bool();
	}

	public void set(int idx, Json value)
	{
		if (this.type != Json.TYPE_ARRAY)
			throw new IllegalArgumentException("Json not array");
		((Json[]) this.value)[idx] = value;
	}

	public void set(int idx, String value)
	{
		this.set(idx, new Json(value));
	}

	public void set(int idx, Number value)
	{
		this.set(idx, new Json(value));
	}

	public void set(int idx, boolean value)
	{
		this.set(idx, new Json(value));
	}

	public void add(Json value)
	{
		if (this.type != Json.TYPE_ARRAY)
			throw new IllegalArgumentException("Json not array");
		Json[] oldArr = (Json[]) this.value;
		Json[] newArr = new Json[oldArr.length + 1];
		System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
		newArr[oldArr.length] = value;
		this.value = newArr;
	}

	public void add(String value)
	{
		this.add(new Json(value));
	}

	public void add(Number value)
	{
		this.add(new Json(value));
	}

	public void add(boolean value)
	{
		this.add(new Json(value));
	}

	public void remove(int idx)
	{
		if (this.type != Json.TYPE_ARRAY)
			throw new IllegalArgumentException("Json not array");
		Json[] oldArr = (Json[]) this.value;
		Json[] newArr = new Json[oldArr.length - 1];
		oldArr[idx] = null;
		System.arraycopy(oldArr, 0, newArr, 0, idx);
		System.arraycopy(oldArr, idx + 1, newArr, idx, oldArr.length - idx - 1);
		this.value = newArr;
	}

	public String string()
	{
		if (this.type != Json.TYPE_STRING)
			throw new IllegalArgumentException("Json not string");
		return (String) this.value;
	}

	public void set(String val)
	{
		if (this.type != Json.TYPE_STRING)
			throw new IllegalArgumentException("Json not string");
		this.value = val;
	}

	public Number number()
	{
		if (this.type != Json.TYPE_NUMBER)
			throw new IllegalArgumentException("Json not number");
		return (Number) this.value;
	}

	public void set(Number val)
	{
		if (this.type != Json.TYPE_NUMBER)
			throw new IllegalArgumentException("Json not number");
		this.value = val;
	}

	public boolean bool()
	{
		if (this.type != Json.TYPE_BOOLEAN)
			throw new IllegalArgumentException("Json not boolean");
		return (Boolean) this.value;
	}

	public void set(boolean val)
	{
		if (this.type != Json.TYPE_BOOLEAN)
			throw new IllegalArgumentException("Json not boolean");
		this.value = val;
	}

	public int length()
	{
		return switch (this.type)
		{
			case Json.TYPE_OBJECT -> ((Map<?, ?>) this.value).size();
			case Json.TYPE_ARRAY -> ((Json[]) this.value).length;
			case Json.TYPE_STRING , Json.TYPE_NUMBER, Json.TYPE_BOOLEAN -> 1;
			default -> throw new IllegalArgumentException("Unknown type " + this.type);
		};
	}

	@SuppressWarnings("unchecked")
	public String stringify()
	{
		switch (this.type)
		{
			case Json.TYPE_OBJECT:
			{
				Array buf = new Array(64);
				buf.put('{');
				boolean dot = false;
				for (Map.Entry<String, Json> entry : ((Map<String, Json>) this.value).entrySet())
				{
					if (dot)
						buf.put(',');
					buf.put(Json.stringify(entry.getKey()).getBytes(StandardCharsets.UTF_8));
					buf.put(':');
					buf.put(Json.stringify(entry.getValue()).getBytes(StandardCharsets.UTF_8));
					dot = true;
				}
				buf.put('}');
				byte[] bytes = new byte[buf.length()];
				buf.get(bytes);
				return new String(bytes, StandardCharsets.UTF_8);
			}
			case Json.TYPE_ARRAY:
			{
				Array buf = new Array(64);
				buf.put('[');
				boolean dot = false;
				for (Json obj : ((Json[]) this.value))
				{
					if (dot)
						buf.put(',');
					buf.put(Json.stringify(obj).getBytes(StandardCharsets.UTF_8));
					dot = true;
				}
				buf.put(']');
				byte[] bytes = new byte[buf.length()];
				buf.get(bytes);
				return new String(bytes, StandardCharsets.UTF_8);
			}
			case Json.TYPE_STRING:
			{
				return Json.stringify((String) this.value);
			}
			case Json.TYPE_NUMBER:
			case Json.TYPE_BOOLEAN:
				return this.value.toString();
		}
		throw new IllegalArgumentException("Unknown type " + this.type);
	}

	public static String stringify(Json object)
	{
		if (object == null)
			return "null";
		return object.stringify();
	}

	public static String stringify(String str)
	{
		if (str == null)
			return "null";
		byte[] bytes = str.getBytes();
		Array buf = new Array(str.length() << 1);
		buf.put('"');
		for (byte ch : bytes)
		{
			if (ch >= 0 && ch < (8 * 12) && ESCAPE[ch] != 0)
			{
				buf.put('\\');
				buf.put(ESCAPE[ch]);
			}
			else if (ch >= 0 && ch < 0x20)
			{
				buf.put('\\');
				buf.put('u');
				buf.put('0');
				buf.put('0');
				buf.put(Json.HEXD[ch >>> 4]);
				buf.put(Json.HEXD[ch & 0xf]);
			}
			else
			{
				buf.put(ch);
			}
		}
		buf.put('"');
		bytes = new byte[buf.length()];
		buf.get(bytes);
		return new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
	}

	public static Json resolve(String json)
	{
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		Array array = new Array(bytes.length);
		array.put(bytes);
		return Json.resolve(array);
	}

	private static Json resolve(Array array)
	{
		Json obj = null;
		whitespace(array);
		if (array.length() <= 0)
			throw new IllegalArgumentException("Expected any object at position " + array.position());
		switch (array.front())
		{
			case '{':
			{
				array.get();
				obj = new Json();
				whitespace(array);
				if (array.length() <= 0)
					throw new IllegalArgumentException("Expected any object or '}' " + array.position());
				boolean continu = array.front() != '}';
				if (array.front() == '}')
					array.get();

				while (continu)
				{
					whitespace(array);
					String name = Json.token(array);
					whitespace(array);
					if (array.length() <= 0 || array.front() != ':')
						throw new IllegalArgumentException("Expected ':' at position " + array.position());
					array.get();
					obj.set(name, Json.resolve(array));
					whitespace(array);
					if (array.length() <= 0 || (array.front() != ',' && array.front() != '}'))
						throw new IllegalArgumentException("Expected ',' or '}' at position " + array.position());
					int ch = array.get();
					continu = ch == ',';
				}
				break;
			}
			case '[':
			{
				array.get();
				obj = new Json(Json.TYPE_ARRAY);
				whitespace(array);
				if (array.length() <= 0)
					throw new IllegalArgumentException("Expected any object or '}' " + array.position());
				boolean continu = array.front() != ']';
				if (array.front() == ']')
					array.get();
				while (continu)
				{
					whitespace(array);
					obj.add(Json.resolve(array));
					whitespace(array);
					if (array.length() <= 0 || (array.front() != ',' && array.front() != ']'))
						throw new IllegalArgumentException("Expected ',' or ']' at position " + array.position());
					int ch = array.get();
					continu = ch == ',';
				}
				break;
			}
			case '"':
			{
				obj = new Json(Json.token(array));
				break;
			}
			default:
			{
				whitespace(array);
				boolean digit = true;
				int len = 0;
				while (len < array.length())
				{
					int ch = array.get(len);
					if (ch == ',' || ch == '}' || ch == ']' || ch < 0x20)
						break;

					digit &= (ch >= '0' && ch <= '9');
					len++;
				}
				if (len == 0)
					throw new IllegalArgumentException("Expected any object at position " + array.position());
				byte[] data = new byte[len];
				array.get(data);
				String val = new String(data);
				switch (val)
				{
					case "null" ->
					{
					}
					case "false" -> obj = new Json(false);
					case "true" -> obj = new Json(true);
					default ->
					{
						try
						{
							if (digit)
								obj = new Json(Long.parseLong(val));
							else
								obj = new Json(Double.parseDouble(val));
						}
						catch (NumberFormatException e)
						{
							throw new IllegalArgumentException("Unknown object " + val);
						}
					}
				}
			}
		}
		return obj;
	}

	private static void whitespace(Array array)
	{
		while (array.length() > 0 && array.front() <= 0x20)
			array.get();
	}

	private static String token(Array array)
	{
		if (array.length() <= 0 || array.get() != '"')
			throw new IllegalArgumentException("Expected taken at position " + array.position());

		Array buf = new Array(16);
		while (true)
		{
			int pos = array.position();
			int ch = array.get();
			if (ch == '"')
				break;

			boolean utf = false;
			if (ch == '\\')
			{
				int esc = array.get();
				switch (esc)
				{
					case '\\':
					case '\'':
					case '"':
						ch = esc;
						break;
					case 'n':
						ch = '\n';
						break;
					case 't':
						ch = '\t';
						break;
					case 'f':
						ch = '\f';
						break;
					case 'r':
						ch = '\r';
						break;
					case 'b':
						ch = '\b';
						break;
					case 'u':
						utf = true;
						ch = 0;
						for (int i = 0; i < 4; i++)
						{
							ch <<= 4;
							int c = array.get();
							if (c == -1)
								throw new IllegalArgumentException("Unterminated escape sequence at position " + pos);
							if (c >= '0' && c <= '9')
								ch |= (c - '0');
							else if (c >= 'a' && c <= 'f')
								ch |= (c - 'a' + 10);
							else if (c >= 'A' && c <= 'F')
								ch |= (c - 'A' + 10);
							else throw new IllegalArgumentException("Unrecognized escape sequence at position " + pos);
						}
						break;
					case -1:
						throw new IllegalArgumentException("Unterminated escape sequence at position " + pos);
					default:
						throw new IllegalArgumentException("Unrecognized escape sequence at position " + pos);
				};
			}

			if (utf)
				buf.put(UTF8.encode(ch));
			else
				buf.put(ch);
		}
		byte[] bytes = new byte[buf.length()];
		buf.get(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
}
