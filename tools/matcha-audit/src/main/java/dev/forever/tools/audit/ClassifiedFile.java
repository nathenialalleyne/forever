package dev.forever.tools.audit;

/** Classification of one observed file, retaining both archive and logical paths. */
public record ClassifiedFile(
        InputFile input,
        String logicalPath,
        String side,
        String namespace,
        String category,
        boolean jsonCandidate,
        String parseStatus,
        String parseError) {

    public long size() {
        return input.size();
    }
}
