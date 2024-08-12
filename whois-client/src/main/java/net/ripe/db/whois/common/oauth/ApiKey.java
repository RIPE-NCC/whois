package net.ripe.db.whois.common.oauth;

import com.google.common.collect.Lists;

import java.util.List;

public class ApiKey {

    private String accessKey;

    private String application;

    private final List<String> scopes = Lists.newArrayList();
}
