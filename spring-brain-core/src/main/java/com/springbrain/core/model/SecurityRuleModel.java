package com.springbrain.core.model;

import java.nio.file.Path;

public final class SecurityRuleModel {

    private final String pathPattern;
    private final String accessType;
    private final String source;
    private final String detail;
    private final Path file;
    private final int line;

    public SecurityRuleModel(String pathPattern,
                             String accessType,
                             String source,
                             String detail,
                             Path file,
                             int line) {
        this.pathPattern = pathPattern;
        this.accessType = accessType;
        this.source = source;
        this.detail = detail;
        this.file = file;
        this.line = line;
    }

    public String getPathPattern() { return pathPattern; }
    public String getAccessType() { return accessType; }
    public String getSource() { return source; }
    public String getDetail() { return detail; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
}
