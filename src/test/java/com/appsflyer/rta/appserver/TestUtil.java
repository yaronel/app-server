package com.appsflyer.rta.appserver;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;

import static java.nio.charset.StandardCharsets.UTF_16;

public final class TestUtil
{
  public static final byte[] EMPTY_BYTES = new byte[0];
  public static final byte[] EMOJIS = "\uD83D\uDE00 \uD83D\uDE03 \uD83D\uDE04".getBytes(UTF_16);
  public static final byte[] UTF_16_CHARACTERS = ("ᚠᛇᚻ᛫ᛒᛦᚦ᛫ᚠᚱᚩᚠᚢᚱ᛫ᚠᛁᚱᚪ᛫ᚷᛖᚻᚹᛦᛚᚳᚢᛗ" +
      "ᛋᚳᛖᚪᛚ᛫ᚦᛖᚪᚻ᛫ᛗᚪᚾᚾᚪ᛫ᚷᛖᚻᚹᛦᛚᚳ᛫ᛗᛁᚳᛚᚢᚾ᛫ᚻᛦᛏ᛫ᛞᚫᛚᚪᚾ" +
      "ᚷᛁᚠ᛫ᚻᛖ᛫ᚹᛁᛚᛖ᛫ᚠᚩᚱ᛫ᛞᚱᛁᚻᛏᚾᛖ᛫ᛞᚩᛗᛖᛋ᛫ᚻᛚᛇᛏᚪᚾ᛬").getBytes(UTF_16);
  
  private TestUtil() {}
  
  public static byte[] intToBytes(int n)
  {
    return new UnpooledHeapByteBuf(
        ByteBufAllocator.DEFAULT, 4, 4)
        .writeInt(n)
        .array();
  }
}
