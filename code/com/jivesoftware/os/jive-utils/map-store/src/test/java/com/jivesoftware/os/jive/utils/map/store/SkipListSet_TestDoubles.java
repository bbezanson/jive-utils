package com.jivesoftware.os.jive.utils.map.store;

import com.jivesoftware.os.jive.utils.io.ByteBufferFactory;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractorStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author jonathan
 */
public class SkipListSet_TestDoubles {

    /**
     *
     * @param _args
     * @throws Exception
     */
    public static void main(String[] _args) throws Exception {

        Random random = new Random(1_234);
        ByteBufferFactory factory = new ByteBufferFactory() {

            @Override
            public ByteBuffer allocate(long _size) {
                return ByteBuffer.allocate((int) _size);
            }
        };
        int keySize = 8;
        int payloadSize = 0;
        byte[] headKey = new byte[keySize];
        Arrays.fill(headKey, Byte.MIN_VALUE);
        SkipListSet sls = new SkipListSet();
        SkipListSetPage page = sls.slallocate(new byte[16], 0, 16, headKey, keySize, payloadSize, DoubleSkipListComparator.cSingleton, factory);
        for (int i = 0; i < 16; i++) {
            sls.sladd(page, FilerIO.doubleBytes(random.nextInt(32)), new byte[0]);
        }

        final SkipListSet.BytesToString toStringer = new SkipListSet.BytesToDoubleString();
        sls.sltoSysOut(page, toStringer);

        byte[] find = FilerIO.doubleBytes(random.nextInt(32));
        byte[] got = sls.slfindWouldInsertAfter(page, find);

        System.out.println(FilerIO.byteDouble(got) + " + 4 key=" + FilerIO.byteDouble(find));

        final AtomicInteger i = new AtomicInteger(1);
        sls.slgetSlice(page, find, null, 4, new ExtractorStream<KeyPayload, Exception>() {

            @Override
            public KeyPayload stream(KeyPayload v) throws Exception {
                if (v == null) {
                    return v;
                }
                System.out.println(i + ":" + toStringer.bytesToString(v.key));
                i.incrementAndGet();
                return v;
            }
        });

        i.set(1);
        double from = random.nextInt(32);
        double to = random.nextInt(6);
        System.out.println(from + "->" + (from + to));
        sls.slgetSlice(page, FilerIO.doubleBytes(from), FilerIO.doubleBytes(from + to), -1, new ExtractorStream<KeyPayload, Exception>() {

            @Override
            public KeyPayload stream(KeyPayload v) throws Exception {
                if (v == null) {
                    return v;
                }
                System.out.println(i + ":" + toStringer.bytesToString(v.key));
                i.incrementAndGet();
                return v;
            }
        });

        System.exit(0);
    }
}
