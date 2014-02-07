/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.io;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author jonathan.colt
 */
public class ByteBufferBackedFiler implements Filer {

    private final Object lock;
    private final ByteBuffer buffer;

    public ByteBufferBackedFiler(Object lock, ByteBuffer buffer) {
        this.lock = lock;
        this.buffer = buffer;
    }

    @Override
    public Object lock() {
        return lock;
    }

    @Override
    public void seek(long position) throws IOException {
        buffer.position((int) position); // what a pain! limited to an int!
    }

    @Override
    public long skip(long position) throws IOException {
        int p = buffer.position();
        p += position;
        buffer.position(p);
        return p;
    }

    @Override
    public long length() throws IOException {
        return buffer.capacity();
    }

    @Override
    public void setLength(long len) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public long getFilePointer() throws IOException {
        return buffer.position();
    }

    @Override
    public void eof() throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public int read() throws IOException {
        return buffer.get();
    }

    @Override
    public int read(byte[] b) throws IOException {
        buffer.get(b);
        return b.length;
    }

    @Override
    public int read(byte[] b, int _offset, int _len) throws IOException {
        buffer.get(b, _offset, _len);
        return b.length;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void write(int b) throws IOException {
        buffer.put((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        buffer.put(b);
    }

    @Override
    public void write(byte[] b, int _offset, int _len) throws IOException {
        buffer.put(b, _offset, _len);
    }

}
