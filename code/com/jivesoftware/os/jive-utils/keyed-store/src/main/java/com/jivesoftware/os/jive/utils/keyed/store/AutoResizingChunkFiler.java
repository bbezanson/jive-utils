package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import com.jivesoftware.os.jive.utils.map.store.FileBackMapStore;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 *
 */
public class AutoResizingChunkFiler implements Filer {
    private final AtomicReference<Filer> filerReference;
    private final ChunkStore chunkStore;
    private final IBA key;
    private final FileBackMapStore<IBA, IBA> mapStore;

    public AutoResizingChunkFiler(FileBackMapStore<IBA, IBA> mapStore, IBA key, ChunkStore chunkStore) {
        this.chunkStore = chunkStore;
        this.mapStore = mapStore;
        this.key = key;
        this.filerReference = new AtomicReference<>();
    }

    public void init(long initialChunkSize) throws Exception {
        IBA value = mapStore.get(key);
        Filer filer = null;
        if (value != null) {
            filer = chunkStore.getFiler(FilerIO.bytesLong(value.getBytes()));
        }
        if (filer == null) {
            filer = createNewFiler(initialChunkSize);
        }
        filerReference.set(filer);
    }

    public void reinit() throws Exception {
        IBA value = mapStore.get(key);
        Filer filer = null;
        if (value != null) {
            filer = chunkStore.getFiler(FilerIO.bytesLong(value.getBytes()));
        }
        if (filer == null) {
            throw new IllegalStateException("Attempted reinit without a chunk");
        }
        filerReference.set(filer);
    }

    public boolean exists() throws Exception {
        IBA value = mapStore.get(key);
        Filer filer = null;
        if (value != null) {
            filer = chunkStore.getFiler(FilerIO.bytesLong(value.getBytes()));
        }
        return filer != null;
    }

    private Filer createNewFiler(long initialChunkSize) throws Exception {
        long chunkId = chunkStore.newChunk(initialChunkSize);
        mapStore.add(key, new IBA(FilerIO.longBytes(chunkId)));
        return chunkStore.getFiler(chunkId);
    }

    @Override
    public Object lock() {
        return filerReference.get().lock();
    }

    @Override
    public void seek(long offset) throws IOException {
        growChunkIfNeeded(filerReference.get(), offset).seek(offset);
    }

    @Override
    public long skip(long offset) throws IOException {
        Filer filer = filerReference.get();
        return growChunkIfNeeded(filer, filer.getFilePointer() + offset).skip(offset);
    }

    @Override
    public long length() throws IOException {
        return filerReference.get().length();
    }

    @Override
    public void setLength(long length) throws IOException {
        growChunkIfNeeded(filerReference.get(), length); // TODO add shrinking support
    }

    @Override
    public long getFilePointer() throws IOException {
        return filerReference.get().getFilePointer();
    }

    @Override
    public void eof() throws IOException {
        filerReference.get().eof(); // TODO add shrinking support
    }

    @Override
    public void flush() throws IOException {
        filerReference.get().flush();
    }

    @Override
    public int read() throws IOException {
        return filerReference.get().read();
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return filerReference.get().read(bytes);
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        return filerReference.get().read(bytes, offset, length);
    }

    @Override
    public void write(int i) throws IOException {
        Filer filer = filerReference.get();
        growChunkIfNeeded(filer, filer.getFilePointer() + 1).write(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        Filer filer = filerReference.get();
        growChunkIfNeeded(filer, filer.getFilePointer() + bytes.length).write(bytes);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        Filer filer = filerReference.get();
        growChunkIfNeeded(filer, filer.getFilePointer() + length).write(bytes, offset, length);
    }

    @Override
    public void close() throws IOException {
        filerReference.get().close();
    }

    private Filer growChunkIfNeeded(Filer currentFiler, long capacity) throws IOException {
        Filer newFiler = currentFiler;
        if (capacity >= currentFiler.length()) {
            try {
                long currentOffset = currentFiler.getFilePointer();
                long newChunkId = chunkStore.newChunk(capacity);
                newFiler = chunkStore.getFiler(newChunkId);
                copy(currentFiler, newFiler, -1);
                long chunkId = FilerIO.bytesLong(mapStore.get(key).getBytes());
                mapStore.add(key, new IBA(FilerIO.longBytes(newChunkId)));
                filerReference.set(newFiler);
                chunkStore.remove(chunkId);
                // copying and chunkStore removal each manipulate the pointer, so restore pointer afterward
                newFiler.seek(currentOffset);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return newFiler;
    }

    private long copy(Filer _from, Filer _to, long _bufferSize) throws Exception {
        long byteCount = _bufferSize;
        if (_bufferSize < 1) {
            byteCount = 1024 * 1024; //1MB
        }
        byte[] chunk = new byte[(int) byteCount];
        int bytesRead;
        long size = 0;
        synchronized (_from.lock()) {
            synchronized (_to.lock()) {
                _from.seek(0);
                while ((bytesRead = _from.read(chunk)) > -1) {
                    _to.seek(size);
                    _to.write(chunk, 0, bytesRead);
                    size += bytesRead;
                    _from.seek(size);
                }
                return size;
            }
        }
    }
}