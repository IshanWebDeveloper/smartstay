package com.smartstay;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Smoke test confirming the build, the test runner and the entry point work. */
class SmartStayAppTest {

    @Test
    void bannerShowsApplicationName() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        SmartStayApp.printBanner(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        String output = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains(SmartStayApp.APP_NAME), "banner should contain the application name");
    }
}
