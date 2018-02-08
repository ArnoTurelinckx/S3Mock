package com.adobe.testing.s3mock.its;

import com.adobe.testing.s3mock.S3MockRule;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class ListObjectsV2Test {

    @ClassRule
    public static S3MockRule S3_MOCK_RULE = new S3MockRule();

    private AmazonS3 s3Client = S3_MOCK_RULE.createS3Client();
    private final static String BUCKET_NAME = "test-bucket";

    @Before
    public void setUp() {
        s3Client.createBucket(BUCKET_NAME);
    }

    @After
    public void tearDown() {
        s3Client.deleteBucket(BUCKET_NAME);
    }

    @Test
    public void maxKeysShouldLimitNumberOfResults() {
        //Given:
        addObjectToBucket(3);

        //When:
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withMaxKeys(2));

        assertThat(actual.getObjectSummaries().size(), is(2));
    }

    @Test
    public void delimiterShouldResultInContentsNotContainingDelimiterAndCommonPrefixes() {
        //Given:
        s3Client.putObject(BUCKET_NAME, "sample.jpg", "");
        s3Client.putObject(BUCKET_NAME, "photos/2006/January/sample.jpg", "");
        s3Client.putObject(BUCKET_NAME, "photos/2006/February/sample2.jpg", "");

        //When:
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withDelimiter("/"));

        assertThat(actual.getDelimiter(), is("/"));
        assertThat(actual.getObjectSummaries().size(), is(1));
        assertThat(actual.getObjectSummaries().get(0).getKey(), is("sample.jpg"));
        assertThat(actual.getCommonPrefixes(), contains("photos/"));
    }

    private void addObjectToBucket(int amountOfObjects) {
        IntStream.rangeClosed(1, amountOfObjects).forEach(putObjectInBucket());
    }

    private IntConsumer putObjectInBucket() {
        return suffix -> s3Client.putObject(new PutObjectRequest(BUCKET_NAME, "file" + suffix, "content"));
    }
}
