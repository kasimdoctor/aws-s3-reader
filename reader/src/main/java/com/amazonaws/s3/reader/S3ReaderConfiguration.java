package com.amazonaws.s3.reader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.s3.reader.configuration.S3ConfigurationProperties;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;

@Configuration
@EnableConfigurationProperties(S3ConfigurationProperties.class)
public class S3ReaderConfiguration {

    private static final int CONNECTION_POOL_BUFFER = 10;
    private static final String AWS_ACCESS_KEY = "aws.accessKeyId";
    private static final String AWS_SECRET_KEY = "aws.secretKey";

    @Autowired
    private S3ConfigurationProperties s3ConfigProperties;

    @Bean
    @ConditionalOnMissingBean
    public AWSCredentialsProvider awsCredentialsProvider() {
        if (StringUtils.isNoneBlank(s3ConfigProperties.getAccessKey(), s3ConfigProperties.getSecretKey())) {
            System.setProperty(AWS_ACCESS_KEY, s3ConfigProperties.getAccessKey());
            System.setProperty(AWS_SECRET_KEY, s3ConfigProperties.getSecretKey());
        }

        return new DefaultAWSCredentialsProviderChain();
    }

    @Bean
    @ConditionalOnMissingBean
    public AmazonS3Client s3Client(AWSCredentialsProvider awsCredentialsProvider) {
        ClientConfiguration clientConfiguration =
                new ClientConfiguration().withMaxConnections(s3ConfigProperties.getConfiguration().getMemoryObjectsLimit() + CONNECTION_POOL_BUFFER)
                        .withMaxErrorRetry(5);
        return new AmazonS3Client(awsCredentialsProvider, clientConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public ListObjectsRequest listObjectsRequest() {
        return new ListObjectsRequest().withBucketName(s3ConfigProperties.getBucketName())
                .withDelimiter(s3ConfigProperties.getConfiguration().getDelimiter());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }
}
