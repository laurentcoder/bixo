package bixo.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import bixo.datum.ContentBytes;
import bixo.datum.FetchedDatum;
import bixo.datum.HttpHeaders;
import bixo.datum.Payload;

public class DiskQueueTest {

    @Test
    public void testQueue() {
        DiskQueue<String> queue = new DiskQueue<String>(1);
        
        assertTrue(queue.offer("one"));
        assertTrue(queue.offer("two"));
        
        assertEquals("one", queue.remove());
        assertEquals("two", queue.remove());
        assertNull(queue.poll());
        
        assertTrue(queue.offer("three"));
        assertEquals("three", queue.remove());
        assertNull(queue.poll());
    }
    
    @Test
    public void testReadAndWriteFromDiskQueue() {
        DiskQueue<String> queue = new DiskQueue<String>(1);
        
        assertTrue(queue.offer("one"));
        assertTrue(queue.offer("two"));
        assertTrue(queue.offer("three"));
        
        assertEquals("one", queue.remove());
        assertTrue(queue.offer("four"));
        assertEquals("two", queue.remove());
        assertEquals("three", queue.remove());
        assertEquals("four", queue.remove());
        assertNull(queue.poll());
    }
    
    @Test
    public void testLotsOfOperations() {
        DiskQueue<Integer> queue = new DiskQueue<Integer>(10);
        int numInQueue = 0;
        int readIndex = 0;
        int writeIndex = 0;
        
        Random rand = new Random(137);
        
        for (int i = 0; i < 10000; i++) {
            if (rand.nextInt(10) < 4) {
                assertEquals(readIndex, queue.remove().intValue());
                readIndex += 1;
                numInQueue -= 1;
            } else {
                assertTrue(queue.offer(new Integer(writeIndex)));
                writeIndex += 1;
                numInQueue += 1;
            }
        }
        
        while (numInQueue > 0) {
            assertEquals(readIndex, queue.remove().intValue());
            readIndex += 1;
            numInQueue -= 1;
        }
    }
    
    @Test
    public void testLotsOfFileDeleteAndCreate() {
        DiskQueue<Integer> queue = new DiskQueue<Integer>(1);

        // We'll repeatedly overflow to disk, and then drain the queue,
        // thus forcing file creation/deletion.
        for (int i = 0; i < 100; i++) {
            assertTrue(queue.offer(new Integer(666)));
            assertTrue(queue.offer(new Integer(333)));
            assertEquals(666, queue.remove().intValue());
            assertEquals(333, queue.remove().intValue());
            assertNull(queue.poll());
        }
    }
    
    @Test
    public void testInvalidQueueSize() {
        try {
            new DiskQueue<String>(0);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // valid
        }
    }
    
    @Test
    public void testClearingQueue() {
        DiskQueue<Integer> queue = new DiskQueue<Integer>(1);
        assertTrue(queue.offer(new Integer(666)));
        queue.clear();
        
        assertEquals(0, queue.size());
        assertNull(queue.poll());
    }
    
    @Test
    public void testSizeWithCachedInternalEntry() {
        // Tricky test to expose subtle bug. If we have a cached entry (read from
        // disk, but not in queue) this needs to get accounted for in the size
        // calculation.
        
        DiskQueue<Integer> queue = new DiskQueue<Integer>(1);
        assertTrue(queue.offer(new Integer(1)));
        assertTrue(queue.offer(new Integer(2)));
        assertTrue(queue.offer(new Integer(3)));
        
        assertEquals(1, queue.remove().intValue());
        assertEquals(2, queue.remove().intValue());
        assertEquals(1, queue.size());
    }
    
    @Test
    public void testReleasingMemory() {
        DiskQueue<String> queue = new DiskQueue<String>(1);

        assertTrue(queue.offer("first"));
        assertTrue(queue.offer("second"));
        try {
            for (int i = 0; i < 30; i++) {
                System.out.println(i);
                byte data[] = new byte[1000000];
                assertTrue(queue.offer(new String(data)));
                assertNotNull(queue.poll());
            }
        } finally {
            queue.clear();
        }
    }
    
    @Test
    public void testGettingBackWhatWasWritten() {
        final int numElements = 100;
        DiskQueue<FetchedDatum> queue = new DiskQueue<FetchedDatum>(numElements/10);
        
        FetchedDatum datums[] = new FetchedDatum[numElements];
        for (int i = 0; i < numElements; i++) {
            String baseUrl = "http://domain-" + i + "+.com/index.html";
            String redirectedUrl = "http://www.domain-" + i + ".com/index.html";
            long fetchTime = System.currentTimeMillis();
            HttpHeaders headers = new HttpHeaders();
            headers.add("key", "value-" + i);
            ContentBytes content = new ContentBytes(new String("content-" + i).getBytes());
            String contentType = "text/plain";
            int responseRate = (i + 1) * 1000;
            Payload payload = new Payload();
            payload.put("key", "value-" + i);
            FetchedDatum datum = new FetchedDatum(baseUrl, redirectedUrl, fetchTime, headers, content, contentType, responseRate);
            datum.setPayload(payload);
            datums[i] = datum;
            assertTrue(queue.offer(datum));
        }
        
        for (int i = 0; i < numElements; i++) {
            FetchedDatum datum = queue.poll();
            assertNotNull(datum);
            assertEquals(datums[i], datum);
        }
        
        assertNull(queue.poll());
    }

    @Test
    public void testAddingNullElement() {
        DiskQueue<Integer> queue = new DiskQueue<Integer>(1);

        try {
            queue.offer(null);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // valid
        }
        
        // Now test when adding null that gets written to disk.
        assertTrue(queue.offer(new Integer(666)));
        assertTrue(queue.offer(new Integer(666)));
        
        try {
            queue.offer(null);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // valid
        }
    }
    
    @Test
    public void testPeekingAndGetting() throws Exception {
        DiskQueue<Integer> queue = new DiskQueue<Integer>(2);
        
        assertTrue(queue.offer(new Integer(666)));
        assertTrue(queue.offer(new Integer(999)));
        assertTrue(queue.offer(new Integer(333)));
        assertEquals(666, queue.peek().intValue());
        assertEquals(666, queue.peek(0).intValue());
        assertEquals(999, queue.peek(1).intValue());
        
        // We only have space for two items in memory, so
        // indexes past that will throw exceptions.
        try {
            queue.peek(2);
            fail("Should have throw exception");
        } catch (IndexOutOfBoundsException e) {
            // valid
        }
        
        assertEquals(999, queue.remove(1).intValue());
        assertEquals(2, queue.size());
        assertNull(queue.peek(1));

        assertEquals(666, queue.remove(0).intValue());
        assertEquals(1, queue.size());
    }
    
}
