package com.adobe.testing.s3mock.dto;

import com.adobe.testing.s3mock.domain.BucketContents;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("ListBucketResult")
@XStreamInclude({BucketContents.class})
public class ListObjectsV2Result implements Serializable {

    @XStreamImplicit(itemFieldName = "CommonPrefixes")
    private List<CommonPrefixes> commonPrefixes = new ArrayList();
    @XStreamAlias("IsTruncated")
    private boolean isTruncated;
    @XStreamAlias("Name")
    private String bucketName;
    @XStreamAlias("KeyCount")
    private int keyCount;
    @XStreamAlias("Prefix")
    private String prefix;
    @XStreamAlias("Delimiter")
    private String delimiter;
    @XStreamAlias("MaxKeys")
    private int maxKeys;
    @XStreamAlias("Encoding-Type")
    private String encodingType;
    @XStreamAlias("ContinuationToken")
    private String continuationToken;
    @XStreamAlias("StartAfter")
    private String startAfter;
    @XStreamImplicit(itemFieldName = "Contents")
    private List<BucketContents> contents = new ArrayList<>();

    public ListObjectsV2Result() {
    }

    public ListObjectsV2Result(List<CommonPrefixes> commonPrefixes,
                               boolean isTruncated,
                               String bucketName,
                               int keyCount,
                               String prefix,
                               String delimiter,
                               int maxKeys,
                               String encodingType,
                               String continuationToken,
                               String startAfter,
                               List<BucketContents> contents) {
        this.commonPrefixes.addAll(commonPrefixes);
        this.isTruncated = isTruncated;
        this.bucketName = bucketName;
        this.keyCount = keyCount;
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.maxKeys = maxKeys;
        this.encodingType = encodingType;
        this.continuationToken = continuationToken;
        this.startAfter = startAfter;
        this.contents.addAll(contents);
    }
    @XmlElement(name = "CommonPrefixes")
    public List<CommonPrefixes> getCommonPrefixes() {
        return commonPrefixes;
    }
    @XmlElement(name = "IsTruncated")
    public boolean isTruncated() {
        return isTruncated;
    }
    @XmlElement(name = "Name")
    public String getBucketName() {
        return bucketName;
    }
    @XmlElement(name = "KeyCount")
    public int getKeyCount() {
        return keyCount;
    }
    @XmlElement(name = "Prefix")
    public String getPrefix() {
        return prefix;
    }
    @XmlElement(name = "Delimiter")
    public String getDelimiter() {
        return delimiter;
    }
    @XmlElement(name = "MaxKeys")
    public int getMaxKeys() {
        return maxKeys;
    }
    @XmlElement(name = "Encoding-Type")
    public String getEncodingType() {
        return encodingType;
    }
    @XmlElement(name = "ContinuationToken")
    public String getContinuationToken() {
        return continuationToken;
    }
    @XmlElement(name = "StartAfter")
    public String getStartAfter() {
        return startAfter;
    }

    public List<BucketContents> getContents() {
        return contents;
    }

    public void setContents(List<BucketContents> contents) {
        this.contents = contents;
    }
}
