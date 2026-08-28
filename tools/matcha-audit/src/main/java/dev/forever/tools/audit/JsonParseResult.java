package dev.forever.tools.audit;

import com.google.gson.JsonElement;

/** Retains either parsed JSON or a visible parse error for inventory reporting. */
public record JsonParseResult(JsonElement element, String status, String error) {
    public boolean parsed() {
        return element != null && "parsed".equals(status);
    }
}
