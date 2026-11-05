package com.trackwise;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TrackController {

    private volatile String activeFault = "none";
    private volatile long slowMillis = 600;

    @GetMapping("/v1/track")
    public String track() throws InterruptedException {
        if ("error".equals(activeFault)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "simulated TrackWise failure");
        }
        if ("slow".equals(activeFault)) {
            Thread.sleep(slowMillis);
        }
        return "{\"trackingId\":\"DEMO-001\",\"status\":\"IN_TRANSIT\"}\n";
    }

    @GetMapping("/lab/fault")
    public String setFault(
            @RequestParam(defaultValue = "none") String mode,
            @RequestParam(defaultValue = "600") long ms) {

        if (!"none".equalsIgnoreCase(mode)
                && !"error".equalsIgnoreCase(mode)
                && !"slow".equalsIgnoreCase(mode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "mode must be none, error, or slow");
        }

        activeFault = mode.toLowerCase();
        slowMillis = Math.min(Math.max(ms, 0), 10000);
        return "fault=" + activeFault + " slowMillis=" + slowMillis + "\n";
    }
}