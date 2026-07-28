package com.alin.lin.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * UUID v7：前 48 bit 為毫秒時間，適合資料庫索引、CDC 與跨服務先產生 ID。
 * 唯一性仍由資料庫 PRIMARY KEY / UNIQUE 約束保證。
 */
public final class UuidV7 {
    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {
    }

    public static String next() {
        long unixMillis = System.currentTimeMillis() & 0xFFFFFFFFFFFFL;
        long randomA = RANDOM.nextLong() & 0xFFFL;
        long randomB = RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL;
        long mostSignificantBits = (unixMillis << 16) | 0x7000L | randomA;
        long leastSignificantBits = 0x8000000000000000L | randomB;
        return new UUID(mostSignificantBits, leastSignificantBits).toString();
    }
}
