package com.amazonaws.s3.reader;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import(S3ReaderConfiguration.class)
public class S3LoaderAutoConfiguration {
}
