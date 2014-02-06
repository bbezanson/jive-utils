package com.jivesoftware.os.jive.utils.map.store.extractors;

import com.jivesoftware.os.jive.utils.map.store.MapChunk;

/**
 *
 * @author jonathan.colt
 */
public class ExtractKeyAndPayload implements Extractor<KeyAndPayload> {

    @Override
    public KeyAndPayload extract(int i, long _startIndex, int _keySize, int _payloadSize, MapChunk page) {
        byte[] k = new byte[_keySize];
        page.read((int) _startIndex + 1, k, 0, _keySize);
        byte[] p = new byte[_payloadSize];
        page.read((int) _startIndex + 1 + _keySize, p, 0, _payloadSize);
        return new KeyAndPayload(k, p);
    }

    @Override
    public KeyAndPayload ifNull() {
        return null;
    }

}
