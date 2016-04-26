package com.amazonaws.s3.reader.configuration;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public class S3ConfigurationProperties {

    @NotBlank
    private String bucketName;

    private String accessKey;

    private String secretKey;

    @NotNull
    private Configuration configuration;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public static class Configuration {
        private String delimiter;
        private int memoryObjectsLimit;

        public String getDelimiter() {
            return delimiter;
        }

        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }

        public int getMemoryObjectsLimit() {
            return memoryObjectsLimit;
        }

        public void setMemoryObjectsLimit(int memoryObjectsLimit) {
            this.memoryObjectsLimit = memoryObjectsLimit;
        }
    }

}
