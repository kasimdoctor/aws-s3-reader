package com.amazonaws.s3.reader.client;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.s3.reader.configuration.S3ConfigurationProperties;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.S3Object;

@Component
public class S3ObjectCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ObjectCollector.class);

    private S3DataReader s3DataReader;
    private ArrayBlockingQueue<S3Object> objectQueue;
    private S3ObjectCollection s3ObjectIterable;
    private ExecutorService executor;

    @Autowired
    public S3ObjectCollector(final ExecutorService executor, final AmazonS3Client s3Client, final ListObjectsRequest listObjectsRequest,
                             final S3ConfigurationProperties s3ConfigurationProperties) {
        this.objectQueue = new ArrayBlockingQueue<>(s3ConfigurationProperties.getConfiguration().getMemoryObjectsLimit());
        this.s3ObjectIterable = new S3ObjectCollection();
        this.s3DataReader = new S3DataReader(s3Client, objectQueue, listObjectsRequest, s3ConfigurationProperties);
        this.executor = executor;
        this.executor.submit(s3DataReader);
    }

    /**
     * Returns an <code>Iterable</code> of S3Objects.
     *
     * @return an <code>Iterable<S3Object></code>
     */
    public Iterable<S3Object> s3ObjectsIterable() {
        return s3ObjectIterable;
    }

    /**
     * Used by a REST endpoint to re-trigger a full load from the S3 data store.
     */
    public void performFullS3Load() {
        this.executor.submit(s3DataReader);
    }

    private class S3ObjectCollection implements Iterable<S3Object> {

        @Override
        public Iterator<S3Object> iterator() {
            return new S3ObjectIterator();
        }
    }

    private class S3ObjectIterator implements Iterator<S3Object> {

        @Override
        public boolean hasNext() {
            return !objectQueue.isEmpty();
        }

        @Override
        public S3Object next() {
            S3Object s3Object = null;
            try {
                s3Object = objectQueue.take();
            } catch (InterruptedException e) {
                LOGGER.error("Error retrieving S3Object from the collection");
            }
            return s3Object;
        }
    }
}

