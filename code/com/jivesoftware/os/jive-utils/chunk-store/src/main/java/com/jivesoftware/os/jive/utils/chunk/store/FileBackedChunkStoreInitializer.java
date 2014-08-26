package com.jivesoftware.os.jive.utils.chunk.store;

import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.RandomAccessFiler;
import com.jivesoftware.os.jive.utils.io.SubsetableFiler;
import java.io.File;

/**
 *
 */
public class FileBackedChunkStoreInitializer {

    private static final long referenceNumber = 1;

    public ChunkStore initialize(String chunkFile, boolean sync) throws Exception {
        File chunkStoreFile = new File(chunkFile);

        ChunkStore chunkStore;
        if (chunkStoreFile.exists() && chunkStoreFile.length() > 0) {
            Filer filer = new RandomAccessFiler(chunkStoreFile, "rw" + ((sync) ? "s" : ""));
            chunkStore = new ChunkStore(new SubsetableFiler(filer, 0, Long.MAX_VALUE, 0));
            chunkStore.open();
        } else {
            chunkStore = new ChunkStore();
            chunkStore.setup(referenceNumber);

            Filer filer = new RandomAccessFiler(chunkStoreFile, "rw" + ((sync) ? "s" : ""));
            chunkStore.open(new SubsetableFiler(filer, 0, Long.MAX_VALUE, 0));
        }
        return chunkStore;
    }
}
