package com.example.lh.okhttp;

/**
 * Created by lihang on 18-3-29.
 */

public class BuilderMode {
    private String name;
    private String sex;
    private int age;

    private BuilderMode(Builder builder) {
        name = builder.name;
        age = builder.age;
    }

    public static final class Builder {
        private String name;
        private int age;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder age(int val) {
            age = val;
            return this;
        }

        public BuilderMode build() {
            return new BuilderMode(this);
        }
    }
}
