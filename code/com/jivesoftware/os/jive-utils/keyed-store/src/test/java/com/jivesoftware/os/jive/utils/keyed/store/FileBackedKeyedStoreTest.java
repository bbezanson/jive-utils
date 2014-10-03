/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStoreInitializer;
import com.jivesoftware.os.jive.utils.chunk.store.MultiChunkStore;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import java.nio.file.Files;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author jonathan.colt
 */
public class FileBackedKeyedStoreTest {

    @Test
    public void keyedStoreTest() throws Exception {
        String[] mapDirs = new String[] {
            Files.createTempDirectory("map").toFile().getAbsolutePath(),
            Files.createTempDirectory("map").toFile().getAbsolutePath()
        };
        String[] swapDirs = new String[] {
            Files.createTempDirectory("swap").toFile().getAbsolutePath(),
            Files.createTempDirectory("swap").toFile().getAbsolutePath()
        };
        String[] chunkDirs = new String[] {
            Files.createTempDirectory("chunk").toFile().getAbsolutePath(),
            Files.createTempDirectory("chunk").toFile().getAbsolutePath()
        };

        ChunkStoreInitializer chunkStoreInitializer = new ChunkStoreInitializer();
        MultiChunkStore multChunkStore = chunkStoreInitializer.initializeMulti(chunkDirs, "data", 4, 4096, false);
        FileBackedKeyedStore fileBackedKeyedStore = new FileBackedKeyedStore(mapDirs, swapDirs,
            4, 100, multChunkStore, 512, 4);

        byte[] key = FilerIO.intBytes(1010);
        Filer filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            FilerIO.writeInt(filer, 10, "");
        }

        fileBackedKeyedStore = new FileBackedKeyedStore(mapDirs, swapDirs, 4, 100, multChunkStore, 512, 4);
        filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            filer.seek(0);
            int ten = FilerIO.readInt(filer, "");
            System.out.println("ten:" + ten);
            Assert.assertEquals(ten, 10);
        }
    }

    @Test
    public void swapTest() throws Exception {
        String[] mapDirs = new String[] {
            Files.createTempDirectory("map").toFile().getAbsolutePath(),
            Files.createTempDirectory("map").toFile().getAbsolutePath()
        };
        String[] swapDirs = new String[] {
            Files.createTempDirectory("swap").toFile().getAbsolutePath(),
            Files.createTempDirectory("swap").toFile().getAbsolutePath()
        };
        String[] chunkDirs = new String[] {
            Files.createTempDirectory("chunk").toFile().getAbsolutePath(),
            Files.createTempDirectory("chunk").toFile().getAbsolutePath()
        };

        ChunkStoreInitializer chunkStoreInitializer = new ChunkStoreInitializer();
        MultiChunkStore multChunkStore = chunkStoreInitializer.initializeMulti(chunkDirs, "data", 4, 4096, false);
        FileBackedKeyedStore fileBackedKeyedStore = new FileBackedKeyedStore(mapDirs, swapDirs,
            4, 100, multChunkStore, 512, 4);

        byte[] key = FilerIO.intBytes(1020);
        SwappableFiler filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            filer.sync();
            FilerIO.writeInt(filer, 10, "");

            SwappingFiler swappingFiler = filer.swap(4);
            FilerIO.writeInt(swappingFiler, 20, "");
            swappingFiler.commit();
        }

        fileBackedKeyedStore = new FileBackedKeyedStore(mapDirs, swapDirs,
            4, 100, multChunkStore, 512, 4);
        filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            filer.sync();
            filer.seek(0);
            int twenty = FilerIO.readInt(filer, "");
            System.out.println("twenty:" + twenty);
            Assert.assertEquals(twenty, 20);
        }
    }
}
