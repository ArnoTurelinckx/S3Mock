package com.adobe.testing.s3mock.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("CommonPrefixes")
public class CommonPrefixes {

    @XStreamAlias("Prefix")
    private String prefix;

    public CommonPrefixes() {
    }

    public CommonPrefixes(String prefix) {
        this.prefix = prefix;
    }
}
