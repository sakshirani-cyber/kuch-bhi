package com.example.AuthProject.debug;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Temporary NDJSON debug logger for Cursor debug mode. Do not use for secrets.
 */
public final class AgentDebugLog {
    private static final Path LOG_PATH =
            Path.of("/home/rakhi-kumai/Downloads/trial project/.cursor/debug-6b17df.log");

    private AgentDebugLog() {
    }

    public static void log(String hypothesisId, String location, String message, Map<String, ?> data) {
        // #region agent log
        try {
            StringBuilder sb = new StringBuilder(256);
            sb.append("{\"sessionId\":\"6b17df\",\"runId\":\"pre-fix\",\"hypothesisId\":\"")
                    .append(escape(hypothesisId))
                    .append("\",\"location\":\"")
                    .append(escape(location))
                    .append("\",\"message\":\"")
                    .append(escape(message))
                    .append("\",\"timestamp\":")
                    .append(System.currentTimeMillis())
                    .append(",\"data\":{");
            boolean first = true;
            if (data != null) {
                for (Map.Entry<String, ?> e : data.entrySet()) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    sb.append('"').append(escape(e.getKey())).append("\":\"")
                            .append(escape(String.valueOf(e.getValue()))).append('"');
                }
            }
            sb.append("}}\n");
            Files.writeString(
                    LOG_PATH,
                    sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
            // never break app for debug logging
        }
        // #endregion
    }

    private static String escape(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
