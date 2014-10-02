package com.jivesoftware.os.jive.utils.map.store;

import com.jivesoftware.os.jive.utils.io.FilerIO;
import com.jivesoftware.os.jive.utils.io.HeapByteBufferFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class ByteBufferValueArrayMapStoreTest {

    @Test (enabled = false)
    public void testPrimitiveArrays() throws Exception {
        final int numFields = 10;
        final int numTerms = 100_000;
        final int numIterations = 20;
        int[] values = new int[numFields * numTerms];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }

        for (int i = 0; i < numIterations; i++) {
            System.out.println("---------------------- " + i + " ----------------------");

            // bytebuffer mapstore setup
            ByteBufferValueArrayMapStore<Long, Integer> mapStore =
                new ByteBufferValueArrayMapStore<Long, Integer>(8, 4, 10, null, new HeapByteBufferFactory()) {

                    @Override
                    public byte[] keyBytes(Long key) {
                        return FilerIO.longBytes(key);
                    }

                    @Override
                    public Long bytesKey(byte[] bytes, int offset) {
                        return FilerIO.bytesLong(bytes, offset);
                    }

                    @Override
                    public byte[] valueBytes(Integer value) {
                        return FilerIO.intBytes(value);
                    }

                    @Override
                    public Integer bytesValue(Long key, byte[] bytes, int offset) {
                        return FilerIO.bytesInt(bytes, offset);
                    }
                };

            // bytebuffer mapstore insert
            long start = System.currentTimeMillis();
            for (int fieldId = 0; fieldId < numFields; fieldId++) {
                for (int termId = 0; termId < numTerms; termId++) {
                    long key = (long) termId << 32 | fieldId & 0xFFFF_FFFFL;
                    mapStore.add(key, values[fieldId * numFields + termId]);
                }
            }
            System.out.println(
                "ByteBufferValueArrayMapStore: Inserted " + mapStore.estimatedMaxNumberOfKeys() + " in " + (System.currentTimeMillis() - start) + "ms");

            // bytebuffer mapstore retrieve
            start = System.currentTimeMillis();
            for (int fieldId = 0; fieldId < numFields; fieldId++) {
                for (int termId = 0; termId < numTerms; termId++) {
                    long key = (long) termId << 32 | fieldId & 0xFFFF_FFFFL;
                    Integer retrieved = mapStore.get(key);
                    assertTrue(retrieved == values[fieldId * numFields + termId], "Failed at " + fieldId + ", " + termId);
                }
            }

            if (i == numIterations - 1) {
                Thread.sleep(600_000);
            }

            System.out.println(
                "ByteBufferValueArrayMapStore: Retrieved " + mapStore.estimatedMaxNumberOfKeys() + " in " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
