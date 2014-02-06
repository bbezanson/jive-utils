/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.map.store.extractors;

import com.jivesoftware.os.jive.utils.map.store.MapChunk;

/**
 *
 * @author jonathan.colt
 */
public class ExtractKey implements Extractor<byte[]> {

    @Override
    public byte[] extract(int i, long _startIndex, int _keySize, int _payloadSize, MapChunk page) {
        byte[] k = new byte[_keySize];
        page.read((int) _startIndex + 1, k, 0, _keySize);
        //System.arraycopy(_page,(int)_startIndex+1, k, 0, _keySize);
        return k;
    }

    @Override
    public byte[] ifNull() {
        return null;
    }

}
