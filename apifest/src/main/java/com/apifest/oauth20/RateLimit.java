package com.apifest.oauth20;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonPropertyOrder({ "requests", "per_seconds"})
@JsonSerialize(include = Inclusion.NON_EMPTY)
public class RateLimit implements Serializable {

    private static final long serialVersionUID = -5642263289815851515L;

    @JsonProperty("requests")
    private Long requests;

    @JsonProperty("per_seconds")
    private Long perSeconds;

    public Long getRequests() {
        return requests;
    }

    public void setRequests(Long requests) {
        this.requests = requests;
    }

    public Long getPerSeconds() {
        return perSeconds;
    }

    public void setPerSeconds(Long perSeconds) {
        this.perSeconds = perSeconds;
    }

    @JsonIgnore
    public boolean isEmpty() {
        if (requests == null || perSeconds == null) {
            return true;
        }
        return false;
    }
}
