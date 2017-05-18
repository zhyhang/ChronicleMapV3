package com.zyh.chronicle.map.v3;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.LongSummaryStatistics;
import java.util.UUID;

import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.values.Values;

/**
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapPersistentSize {

	private static final ThreadLocal<LongValue> THREAD_LOCAL_LONGVALUE = ThreadLocal
			.withInitial(() -> Values.newHeapInstance(LongValue.class));

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int maxEntry = 10000000;
		File f = Files.createTempFile("cmap3-size-test", ".dat").toFile();
		// create map
		ChronicleMapBuilder<byte[], LongValue> mapBuilder = ChronicleMapBuilder.of(byte[].class, LongValue.class)
				.entries(maxEntry).averageKeySize(16);
		ChronicleMap<byte[], LongValue> map = mapBuilder.createPersistedTo(f);
		LongSummaryStatistics md5Stat = new LongSummaryStatistics();
		LongSummaryStatistics putStat = new LongSummaryStatistics();
		LongSummaryStatistics getStat = new LongSummaryStatistics();
		for (int i = 0; i < maxEntry / 10; i++) {
			LongValue v = THREAD_LOCAL_LONGVALUE.get();
			v.setValue(i);
			byte[] bytes = UUID.randomUUID().toString().getBytes();
			long tsb = System.nanoTime();
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] md5Data = md.digest(bytes);
			md5Stat.accept(System.nanoTime() - tsb);
			tsb = System.nanoTime();
			map.put(md5Data, v);
			putStat.accept(System.nanoTime() - tsb);
			tsb = System.nanoTime();
			map.get(md5Data);
			getStat.accept(System.nanoTime() - tsb);
			if (i % 10000 == 1) {
				System.out.format("count[%d], max entry[%d],current size[%d]\n", i, maxEntry, map.longSize());
			}
		}
		map.close();
		System.out.format("md5 stat: %s.\n",md5Stat.toString());
		System.out.format("put stat: %s.\n",putStat.toString());
		System.out.format("get stat: %s.\n",getStat.toString());
	}

}
