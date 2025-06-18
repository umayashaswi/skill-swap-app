package com.example.skill_swap;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class ConnectionRequest {

    private String fromUid;
    private String toUid;
    private String status;    // Pending, Accepted, Rejected

    public ConnectionRequest() { }

    public ConnectionRequest(String fromUid, String toUid, String status) {
        this.fromUid = fromUid;
        this.toUid   = toUid;
        this.status  = status;
    }

    /** Convert to Firestore‑ready map including the `uids` array */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fromUid", fromUid);
        map.put("toUid",   toUid);
        map.put("status",  status);
        // 👇 NEW: both UIDs so we can query with array‑contains
        map.put("uids", Arrays.asList(fromUid, toUid));

        return map;
    }

    /* getters if you need them */
}
