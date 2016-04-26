package com.amazonaws.s3.reader.service;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.s3.reader.client.S3ObjectCollector;

@RestController
@RequestMapping("/s3")
public class S3ReaderService {

    private static final String RESPONSE = "Full load from S3 initiated.";

    @Autowired
    private S3ObjectCollector s3ObjectCollector;

    /**
     * Trigger a full load of S3Objects from the S3 data store.
     *
     * @throws Exception
     */
    @RequestMapping(value = "/fullLoad", method = GET)
    public ResponseEntity<Result> s3FullLoad() throws Exception {
        s3ObjectCollector.performFullS3Load();
        return ok(new Result(RESPONSE));
    }

    public static final class Result {
        private final String message;

        public Result(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
