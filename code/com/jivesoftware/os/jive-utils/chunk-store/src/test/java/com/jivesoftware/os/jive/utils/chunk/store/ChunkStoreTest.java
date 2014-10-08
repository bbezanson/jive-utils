/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.chunk.store;

import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import com.jivesoftware.os.jive.utils.io.RandomAccessFiler;
import com.jivesoftware.os.jive.utils.io.SubsetableFiler;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author jonathan.colt
 */
public class ChunkStoreTest {

    @Test(enabled = false)
    public void testChunkStorePerformance() throws Exception {
        final File chunkFile = File.createTempFile("chunk", "test");
        final int numThreads = 16;
        final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        final int numLoops = 100;
        final int numIterations = 100_000;
        final long filerSize = 200_000_000;
        final int chunkLength = 1_000;

        for (int loop = 0; loop < numLoops; loop++) {
            chunkFile.createNewFile();

            /*
            ChunkStore chunkStore = new ChunkStore(new SubsetableFiler(
                new ByteBufferBackedFiler(new Object(), ByteBuffer.allocate((int) filerSize)),
                0, filerSize, filerSize));
            */
            final ChunkStore chunkStore = new ChunkStore(new SubsetableFiler(new RandomAccessFiler(chunkFile, "rw"), 0, Long.MAX_VALUE, 0));

            long start = System.currentTimeMillis();
            List<Future<?>> futures = new ArrayList<>(numThreads);
            for (int n = 0; n < numThreads; n++) {
                futures.add(executorService.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (int i = 0; i < numIterations / numThreads; i++) {
                            long chunkFP = chunkStore.newChunk(chunkLength);
                        }
                        return null;
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            System.out.println("Finished in " + (System.currentTimeMillis() - start));

            chunkFile.delete();
        }
    }

    @Test
    public void testNewChunkStore() throws Exception {
        int size = 1024 * 10;
        String chunkPath = Files.createTempDirectory("testNewChunkStore").toFile().getAbsolutePath();
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunkPath, "data", size, false);

        long chunk10 = chunkStore.newChunk(10);
        System.out.println("chunkId:" + chunk10);
        Filer filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            FilerIO.writeInt(filer, 10, "");
        }

        filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            filer.seek(0);
            int ten = FilerIO.readInt(filer, "");
            System.out.println("ten:" + ten);
            assertEquals(ten, 10);
        }
    }

    @Test
    public void testExistingChunkStore() throws Exception {
        int size = 1024 * 10;
        String chunkPath = Files.createTempDirectory("testExistingChunkStore").toFile().getAbsolutePath();
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunkPath, "data", size, false);

        long chunk10 = chunkStore.newChunk(10);
        System.out.println("chunkId:" + chunk10);
        Filer filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            FilerIO.writeInt(filer, 10, "");
        }
        filer.close();

        long expectedReferenceNumber = chunkStore.getReferenceNumber();

        chunkStore = new ChunkStoreInitializer().initialize(chunkPath, "data", size, false);
        assertEquals(chunkStore.getReferenceNumber(), expectedReferenceNumber);

        filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            filer.seek(0);
            int ten = FilerIO.readInt(filer, "");
            System.out.println("ten:" + ten);
            assertEquals(ten, 10);
        }
    }

    @Test
    public void testResizingChunkStore() throws Exception {
        int size = 512;
        String chunkPath = Files.createTempDirectory("testResizingChunkStore").toFile().getAbsolutePath();
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunkPath, "data", size, true);

        long chunk10 = chunkStore.newChunk(size * 4);
        System.out.println("chunkId:" + chunk10);
        Filer filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            byte[] bytes = new byte[size * 4];
            bytes[0] = 1;
            bytes[bytes.length - 1] = 1;
            FilerIO.write(filer, bytes);
        }

        filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            filer.seek(0);
            byte[] bytes = new byte[size * 4];
            FilerIO.read(filer, bytes);
            assertEquals(bytes[0], 1);
            assertEquals(bytes[bytes.length - 1], 1);
        }
    }
}
