package com.bbn.immortals.archmodel.example;

/**
 * Created by awellman@bbn.com on 12/11/15.
 */
public class AppendableString {

    private String innerString;

    public AppendableString() {
        innerString = "";
    }

    public AppendableString(String initialValue) {
        innerString = initialValue;
    }

    public String getValue() {
        return innerString;
    }

    public void appendValue(String value) {
        innerString = innerString + value;
    }
}
