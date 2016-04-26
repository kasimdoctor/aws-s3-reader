package com.amazonaws.s3.reader.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.s3.reader.configuration.S3ConfigurationProperties;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

class S3DataReader implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3DataReader.class);

    private S3ConfigurationProperties s3ConfigProperties;
    private ListObjectsRequest listObjectsRequest;
    private AmazonS3Client s3Client;
    private ArrayBlockingQueue<S3Object> objectQueue;
    private int count;

    public S3DataReader(AmazonS3Client s3Client, ArrayBlockingQueue<S3Object> objectQueue, ListObjectsRequest listObjectsRequest,
                        S3ConfigurationProperties s3ConfigProperties) {
        this.s3Client = s3Client;
        this.objectQueue = objectQueue;
        this.listObjectsRequest = listObjectsRequest;
        this.s3ConfigProperties = s3ConfigProperties;
    }

    @Override
    public void run() {
        this.parseS3TreeDepthFirst(listObjectsRequest);
    }

    private void parseS3TreeDepthFirst(ListObjectsRequest listObjects) {
        if (listObjects == null) {
            return;
        }

        try {
            List<S3ObjectSummary> keyList = new ArrayList<S3ObjectSummary>();
            ObjectListing objectListing = s3Client.listObjects(listObjects);
            LOGGER.debug("IsTruncated:" + objectListing.getPrefix() + ": " + objectListing.isTruncated() + " delimiter: " + objectListing.getDelimiter()
                    + " commonPrefixes: " + objectListing.getCommonPrefixes());

            List<String> commonPrefixes = objectListing.getCommonPrefixes();
            if (commonPrefixes != null && commonPrefixes.size() != 0) {
                for (String commonPrefix : commonPrefixes) {
                    parseS3TreeDepthFirst(new ListObjectsRequest()
                            .withBucketName(s3ConfigProperties.getBucketName()).withPrefix(commonPrefix)
                            .withDelimiter(s3ConfigProperties.getConfiguration().getDelimiter()));
                }
            }

            while (objectListing.isTruncated()) {
                keyList.addAll(objectListing.getObjectSummaries());
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            }
            keyList.addAll(objectListing.getObjectSummaries());

            for (S3ObjectSummary objectSummary : keyList) {
                LOGGER.info(" - " + objectSummary.getKey() + " " + "(size = " + objectSummary.getSize() + "). Count = " + ++count);
                objectQueue.put(s3Client.getObject(s3ConfigProperties.getBucketName(), objectSummary.getKey()));
            }

        } catch (AmazonServiceException ase) {
            LOGGER.error("Exception encountered while parsing S3 data store. Error Message = {}, AWS Error Code = {}, Error Type = {}.", ase.getMessage(),
                    ase.getErrorCode(), ase.getErrorType());
        } catch (AmazonClientException ace) {
            LOGGER.error("Client exception encountered while parsing S3 data store. Error Message = {}.", ace.getMessage());
        } catch (InterruptedException ie) {
            LOGGER.error("Error encountered while adding the S3Object to the queue.");
        }
    }

}
