package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.MultiChunkStore;
import com.jivesoftware.os.jive.utils.map.store.FileBackMapStore;
import java.util.Arrays;

/**
 * @author jonathan
 */
public class FileBackedKeyedStore implements KeyedFilerStore {

    private final FileBackMapStore<IBA, IBA> mapStore;
    private final FileBackMapStore<IBA, IBA> swapStore;
    private final MultiChunkStore chunkStore;
    private final long newFilerInitialCapacity;
    private final String[] partitions;

    public FileBackedKeyedStore(String mapDirectory, String swapDirectory, int mapKeySize, long initialMapKeyCapacity,
        MultiChunkStore chunkStore, long newFilerInitialCapacity, int numPartitions) throws Exception
    {
        this.mapStore = initializeMapStore(mapDirectory, mapKeySize, initialMapKeyCapacity);
        this.swapStore = initializeMapStore(swapDirectory, mapKeySize, initialMapKeyCapacity);
        this.chunkStore = chunkStore;
        this.newFilerInitialCapacity = newFilerInitialCapacity;
        this.partitions = new String[numPartitions];
        for (int i = 0; i < numPartitions; i++) {
            partitions[i] = String.valueOf(i).intern();
        }
    }

    private FileBackMapStore<IBA, IBA> initializeMapStore(String mapDirectory, int mapKeySize, long initialMapKeyCapacity) throws Exception {
        return new FileBackMapStore<IBA, IBA>(mapDirectory, mapKeySize, 8, (int) initialMapKeyCapacity, 100, null) {
            @Override
            public String keyPartition(IBA key) {
                return partitions[Math.abs(key.hashCode()) % partitions.length];
            }

            @Override
            public Iterable<String> keyPartitions() {
                return Arrays.asList(partitions);
            }

            @Override
            public byte[] keyBytes(IBA key) {
                return key.getBytes();
            }

            @Override
            public byte[] valueBytes(IBA value) {
                return value.getBytes();
            }

            @Override
            public IBA bytesKey(byte[] keyBytes, int offset) {
                return new IBA(keyBytes);
            }

            @Override
            public IBA bytesValue(IBA key, byte[] value, int valueOffset) {
                return new IBA(value);
            }
        };
    }

    @Override
    public SwappableFiler get(byte[] key) throws Exception {
        return get(key, true);
    }

    @Override
    public SwappableFiler get(byte[] keyBytes, boolean autoCreate) throws Exception {
        IBA key = new IBA(keyBytes);
        AutoResizingChunkFiler filer = new AutoResizingChunkFiler(mapStore, key, chunkStore);
        if (!autoCreate && !filer.exists()) {
            return null;
        }
        filer.init(newFilerInitialCapacity);
        return new AutoResizingChunkSwappableFiler(filer, chunkStore, key, mapStore, swapStore);
    }

    @Override
    public long sizeInBytes() throws Exception {
        return mapStore.estimateSizeInBytes() + chunkStore.sizeInBytes();
    }

    public long mapStoreSizeInBytes() throws Exception {
        return mapStore.estimateSizeInBytes();
    }

    @Override
    public void close() {
        // TODO
    }
}
