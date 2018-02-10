package com.adobe.testing.s3mock.its;

import com.adobe.testing.s3mock.S3MockRule;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.assertj.core.condition.DoesNotHave;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    public void maxKeysResult_shouldNotBeTruncatedAndShouldNotContainNextContinuationToken_WhenFewerObjectsThanMaxKeys() {
        //Given:
        addObjectToBucket(2);

        //When:
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withMaxKeys(3));

        assertThat(actual.getObjectSummaries().size(), is(2));
        assertThat(actual.isTruncated(), is(false));
        assertThat(actual.getNextContinuationToken(), is(nullValue()));
    }

    @Test
    public void maxKeysResult_shouldBeTruncatedAndContainNextContinuationToken_WhenMoreObjectsThanMaxKeys() {
        //Given:
        addObjectToBucket(3);

        //When:
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withMaxKeys(2));

        assertThat(actual.isTruncated(), is(true));
        assertThat(actual.getNextContinuationToken(), is(lastObjectSummaryOf(actual.getObjectSummaries()).getKey()));
    }

    private S3ObjectSummary lastObjectSummaryOf(List<S3ObjectSummary> objectSummaries) {
        return objectSummaries.get(objectSummaries.size() - 1);
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
    public void listObjectsV2_providingContinuationToken_willReturnNextSequenceOfObjectsAndGivenContinuationToken() {
        //Given:
        addObjectToBucket(4);
        ListObjectsV2Result firstResult = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withMaxKeys(2));
        String continuationToken = firstResult.getNextContinuationToken();

        //When:
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withContinuationToken(continuationToken));

        //Then:
        assertThat(actual.getContinuationToken(), is(continuationToken));
        assertThat(actual.getObjectSummaries().size(), is(2));
        assertThat(actual.getObjectSummaries(), not(contains(firstResult.getObjectSummaries())));
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

    @Test
    public void prefix_onlyReturnsResultStartingWithPrefix() throws Exception {
        //Given:
        s3Client.putObject(BUCKET_NAME, "sample1.jpg", "");
        s3Client.putObject(BUCKET_NAME, "sample2.jpg", "");
        s3Client.putObject(BUCKET_NAME, "other-sample.jpg", "");

        //When
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix("sample"));

        assertThat(actual.getObjectSummaries().size(), is(2));
        assertThat(actual.getPrefix(), is("sample"));
        assertThat(contentKeysWithin(actual), contains("sample1.jpg", "sample2.jpg"));
    }

    @Test
    public void prefixAndDelimiter_resultsInPrefixedCommonPrefixes() {
        //Given:
        s3Client.putObject(BUCKET_NAME, "sample.jpg", "");
        s3Client.putObject(BUCKET_NAME, "photos/2006/January/sample.jpg", "");
        s3Client.putObject(BUCKET_NAME, "photos/2006/February/sample2.jpg", "");
        s3Client.putObject(BUCKET_NAME, "photos/2006/February/sample3.jpg", "");
        s3Client.putObject(BUCKET_NAME, "photos/2006/February/sample4.jpg", "");

        //When
        ListObjectsV2Result actual = s3Client.listObjectsV2(
                new ListObjectsV2Request().withBucketName(BUCKET_NAME)
                        .withPrefix("photos/2006/")
                        .withDelimiter("/")
        );

        //Then:
        assertThat(actual.getCommonPrefixes(), containsInAnyOrder("photos/2006/January/","photos/2006/February/"));
        //TODO: What about the content?
    }

    @Test
    public void ownerIsNotReturned_whenFetchOwnerIsSetToFalse() {
        //Given:
        addObjectToBucket(1);

        //When:
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withFetchOwner(false));

        //Then:
        assertThat(actual.getObjectSummaries().get(0).getOwner(), is(nullValue()));
    }

    @Test
    public void ownerIsReturned_whenFetchOwnerIsSetToTrue() {
        //Given:
        addObjectToBucket(1);

        //When:
        ListObjectsV2Result actual = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withFetchOwner(true));

        //Then:
        assertThat(actual.getObjectSummaries().get(0).getOwner(), notNullValue());
    }

    @Test
    public void startAfter() throws Exception {
        //does what it name suggests (lexicographical order)
    }

    @Test
    public void keyCount() throws Exception {
        //sum of result's contents and commonPrefixes
    }

    private List<String> contentKeysWithin(ListObjectsV2Result actual) {
        return actual.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    private void addObjectToBucket(int amountOfObjects) {
        IntStream.rangeClosed(1, amountOfObjects).forEach(putObjectInBucket());
    }

    private IntConsumer putObjectInBucket() {
        return suffix -> s3Client.putObject(new PutObjectRequest(BUCKET_NAME, "file" + suffix, "content"));
    }
}
