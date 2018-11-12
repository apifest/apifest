package com.apifest.oauth20;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonPropertyOrder({ "requests", "per_seconds"})
@JsonSerialize(include = Inclusion.NON_EMPTY)
public class RateLimit implements Serializable {

    private static final long serialVersionUID = -5642263289815851515L;

    @JsonProperty("requests")
    private Integer requests;

    @JsonProperty("per_seconds")
    private Integer perSeconds;

    public Integer getRequests() {
        return requests;
    }

    public void setRequests(Integer requests) {
        this.requests = requests;
    }

    public Integer getPerSeconds() {
        return perSeconds;
    }

    public void setPerSeconds(Integer perSeconds) {
        this.perSeconds = perSeconds;
    }
}
