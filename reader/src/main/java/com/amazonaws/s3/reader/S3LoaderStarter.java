package com.amazonaws.s3.reader;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.s3.reader.client.S3ObjectCollector;
import com.amazonaws.services.s3.model.S3Object;

@SpringBootApplication
public class S3LoaderStarter implements CommandLineRunner {

    @Autowired
    private S3ObjectCollector s3ObjectCollector;

    protected S3LoaderStarter() {

    }

    public static void main(String[] args) {
        SpringApplication.run(S3LoaderStarter.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        Iterable<S3Object> s3Objects = s3ObjectCollector.s3ObjectsIterable();
        Iterator<S3Object> s3ObjectIterator = s3Objects.iterator();

        while (true) {
            while (s3ObjectIterator.hasNext()) {
                System.err.println(s3ObjectIterator.next());
            }
        }
    }
}
