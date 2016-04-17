package com.zyh.chronicle.map.v3;

import java.io.File;
import java.nio.file.Files;

import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.values.Values;

/**
 * chronicle map put remove put remove ... <br>
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapRecycle {

	private static final ThreadLocal<LongValue> THREAD_LOCAL_LONGVALUE = ThreadLocal
			.withInitial(() -> Values.newHeapInstance(LongValue.class));

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int maxEntry = 10000;
		File f = Files.createTempFile("cmap-recycle-test", ".dat").toFile();
		// create map
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntry).averageKeySize(10);
		ChronicleMap<String, LongValue> map = mapBuilder.createPersistedTo(f);
		for (int i = 0; i < 20 * maxEntry; i++) {
			LongValue v = THREAD_LOCAL_LONGVALUE.get();
			v.setValue(i);
			map.put(String.valueOf(i), v);
			if (i % 1000 == 0) {
				System.out.format("count[%d], max entry[%d],current size[%d]\n", i, maxEntry, map.longSize());
			}
			if (i >= maxEntry - 1) {
				String removeKey = map.keySet().iterator().next();
				map.remove(removeKey);
			}

		}
		map.close();
	}

}
