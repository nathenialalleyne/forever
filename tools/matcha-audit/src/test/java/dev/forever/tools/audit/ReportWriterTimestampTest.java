package dev.forever.tools.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@code SOURCE_DATE_EPOCH} handling that makes audit reports reproducible.
 *
 * <p>Every other field in a report is a pure function of the input archive. The generation
 * timestamp was the sole reason two audits of the same pinned archive produced different
 * bytes, which meant the committed reports could not be verified by regenerating them and
 * comparing. These tests exist so that guarantee cannot regress silently.
 */
class ReportWriterTimestampTest {

    private static final Instant FALLBACK = Instant.parse("2000-01-01T00:00:00Z");

    @Test
    @DisplayName("an unset SOURCE_DATE_EPOCH keeps the real generation time")
    void unsetUsesFallback() {
        // The default must stay the true time: a human reading a one-off report is better
        // served by when it was actually produced than by a fixed placeholder.
        assertSame(FALLBACK, ReportWriter.generatedAt(null, () -> FALLBACK));
    }

    @Test
    @DisplayName("a blank SOURCE_DATE_EPOCH is treated as unset rather than as an error")
    void blankUsesFallback() {
        // Shells routinely export empty variables. Treating that as a hard failure would
        // break ordinary local runs for no benefit.
        assertSame(FALLBACK, ReportWriter.generatedAt("", () -> FALLBACK));
        assertSame(FALLBACK, ReportWriter.generatedAt("   ", () -> FALLBACK));
    }

    @Test
    @DisplayName("a set SOURCE_DATE_EPOCH pins the timestamp exactly")
    void setPinsTheTimestamp() {
        Instant pinned = ReportWriter.generatedAt("1787885453", () -> FALLBACK);

        assertEquals(Instant.ofEpochSecond(1787885453L), pinned);
        // The serialised form is what actually lands in metadata.json and therefore what
        // determines whether two runs are byte-identical.
        assertEquals("2026-08-28T02:50:53Z", pinned.toString());
    }

    @Test
    @DisplayName("surrounding whitespace is tolerated")
    void whitespaceIsTrimmed() {
        assertEquals(
                Instant.ofEpochSecond(1787885453L),
                ReportWriter.generatedAt("  1787885453\n", () -> FALLBACK));
    }

    @Test
    @DisplayName("the same epoch always yields the same instant")
    void repeatedCallsAreStable() {
        // This is the property the whole mechanism exists to provide, asserted directly
        // rather than inferred from the parsing tests above.
        assertEquals(
                ReportWriter.generatedAt("1787885453", Instant::now),
                ReportWriter.generatedAt("1787885453", Instant::now));
    }

    @Test
    @DisplayName("a non-numeric SOURCE_DATE_EPOCH fails loudly instead of silently falling back")
    void nonNumericIsRejected() {
        // Silently ignoring a bad value would hand non-reproducible output to a caller who
        // explicitly asked for reproducibility, which is the worst possible outcome here.
        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> ReportWriter.generatedAt("yesterday", () -> FALLBACK));

        assertEquals(true, failure.getMessage().contains("yesterday"));
    }

    @Test
    @DisplayName("a negative SOURCE_DATE_EPOCH is rejected")
    void negativeIsRejected() {
        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> ReportWriter.generatedAt("-1", () -> FALLBACK));

        assertEquals(true, failure.getMessage().contains("negative"));
    }

    @Test
    @DisplayName("only the file name is reported, never the absolute path")
    void reportsFileNameNotAbsolutePath() {
        // The absolute path leaked the operator's home directory and username into a
        // committed report, and made output depend on where the input happened to sit.
        assertEquals(
                "Matcha_Flavoured_1_12.zip",
                ReportWriter.inputFileName(
                        Path.of("/home/someone/repos/forever/vendor/matcha/Matcha_Flavoured_1_12.zip")));
    }

    @Test
    @DisplayName("the same archive in different directories yields the same reported name")
    void fileNameIsIndependentOfDirectory() {
        // This is the property that makes two audits of one archive byte-identical
        // regardless of where each was run from.
        assertEquals(
                ReportWriter.inputFileName(Path.of("/one/place/Matcha_Flavoured_1_12.zip")),
                ReportWriter.inputFileName(Path.of("/somewhere/else/Matcha_Flavoured_1_12.zip")));
    }

    @Test
    @DisplayName("a path with no file name is reported explicitly rather than as null")
    void rootPathIsHandled() {
        // Path.getFileName() returns null for a root. A null JSON property would be harder
        // to diagnose than an obviously odd value.
        assertEquals("(unnamed)", ReportWriter.inputFileName(Path.of("/")));
        assertEquals("(unnamed)", ReportWriter.inputFileName(null));
    }
}
