package com.jivesoftware.os.jive.utils.map.store.extractors;

import com.jivesoftware.os.jive.utils.map.store.MapPage;

/**
 *
 * @author jonathan.colt
 */
public class ExtractIndexAndPayload implements Extractor<Object[]> {

    @Override
    public Object[] extract(int i, long _startIndex, int _keySize, int _payloadSize, MapPage page) {
        byte[] p = new byte[_payloadSize];
        page.read((int) _startIndex + 1 + _keySize, p, 0, _payloadSize);
        return new Object[]{ i, p };
    }

    @Override
    public Object[] ifNull() {
        return null;
    }

}
