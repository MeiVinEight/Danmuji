package org.mve;

/**
 * UTF-8 encoding</br>
 * &nbsp;7 bits 0XXXXXXX</br>
 * 11 bits 110XXXXX 10XXXXXX</br>
 * 16 bits 1110XXXX 10XXXXXX 10XXXXXX</br>
 * 21 bits 11110XXX 10XXXXXX 10XXXXXX 10XXXXXX
 */
public class UTF8
{
	public static byte[] encode(int codepoint)
	{
		int size = 0;
		if (codepoint >= (1 << 21))
			throw new IllegalArgumentException("Unrecognized UTF-8 code point " + codepoint);
		if (codepoint < (1 << 7)) size = 1;
		else if (codepoint < (1 << 11)) size = 2;
		else if (codepoint < (1 << 16)) size = 3;
		else size = 4;

		byte[] result = new byte[size];
		result[0] = (byte) ((size > 1) ? ((((1 << size) - 1) << (8 - size)) | (codepoint >> (6 * (size - 1)))) : codepoint);
		while (--size > 0)
		{
			result[size] = (byte) (0x80 | (codepoint & 0x3F));
			codepoint >>>= 6;
		}
		return result;
	}
}
