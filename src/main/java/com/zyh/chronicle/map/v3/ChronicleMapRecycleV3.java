package com.zyh.chronicle.map.v3;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

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
public class ChronicleMapRecycleV3 {

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
		// exceed max entry will increase to 3.0 * max entry 
		ChronicleMapBuilder<String, LongValue> mapBuilder = ChronicleMapBuilder.of(String.class, LongValue.class)
				.entries(maxEntry).maxBloatFactor(3.0).averageKeySize(10);
		ChronicleMap<String, LongValue> map = mapBuilder.createPersistedTo(f);
		for (int i = 0; i < 20 * maxEntry; i++) {
			LongValue v = THREAD_LOCAL_LONGVALUE.get();
			v.setValue(i);
			map.put(String.valueOf(i), v);
			if (i % 1000 == 1) {
				System.out.format("count[%d], max entry[%d],current size[%d]\n", i, maxEntry, map.longSize());
			}
			// comment following if, will throw overflow exception
			if (i >= maxEntry - 1) {
				while (true) {
					String removeKey = String.valueOf(ThreadLocalRandom.current().nextInt(i));
					if (null != map.remove(removeKey) || map.size() == 0) {
						break;
					}
				}
			}
			
		}
		map.close();
	}

}
