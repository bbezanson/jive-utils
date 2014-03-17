package com.jivesoftware.os.jive.utils.map.store;

import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractIndex;
import java.nio.ByteBuffer;

/**
 *
 * @author jonathan
 */
public class SkipListSetPage {

    /**
     *
     */
    final MapChunk map;
    byte maxHeight;
    SkipListComparator valueComparator;
    int headIndex;
    byte[] headKey;

    /**
     *
     * @param page
     * @param headKey
     * @param _valueComparator
     */
    public SkipListSetPage(MapChunk page, byte[] headKey, SkipListComparator _valueComparator) {
        this.map = page;
        this.headKey = headKey;
        valueComparator = _valueComparator;
    }

    /**
     *
     */
    public void init() {
        map.init();
        maxHeight = heightFit(map.capacity);
        headIndex = map.mapStore.get(map, headKey, new ExtractIndex());
        if (headIndex == -1) {
            throw new RuntimeException("SkipListSetPage:Invalid Page!");
        }
    }

    /**
     *
     * @param bytes
     * @return
     */
    public boolean isHeadKey(byte[] bytes) {
        if (bytes.length != headKey.length) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != headKey[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param startOfAKey
     * @param startOfBKey
     * @return
     */
    public int compare(int startOfAKey, int startOfBKey) {
        return valueComparator.compare(map, startOfAKey, map, startOfBKey, map.keySize);
    }

    /**
     *
     * @param startOfAKey
     * @param _key
     * @return
     */
    public int compare(int startOfAKey, byte[] _key) {
        return valueComparator.compare(map, startOfAKey, new MapChunk(map.mapStore, ByteBuffer.wrap(_key)), 0, map.keySize);
    }

    /**
     *
     * @param _length
     * @return
     */
    public static byte heightFit(long _length) {
        byte h = (byte) 1; // room for back links
        for (byte i = 1; i < 65; i++) { // 2^64 == long so why go anyfuther
            if (_length < Math.pow(2, i)) {
                return (byte) (h + i);
            }
        }
        return 64;
    }
}
