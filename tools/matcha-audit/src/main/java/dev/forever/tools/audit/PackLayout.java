package dev.forever.tools.audit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Locates the logical Minecraft pack root without changing observed archive paths. */
public record PackLayout(String prefix, String detection) {
    public static PackLayout detect(List<InputFile> files) {
        Set<String> firstSegments = new HashSet<>();
        boolean directPackContent = false;
        for (InputFile file : files) {
            String path = file.path();
            int slash = path.indexOf('/');
            String first = slash < 0 ? path : path.substring(0, slash);
            firstSegments.add(first);
            if (path.equals("pack.mcmeta") || path.startsWith("data/") || path.startsWith("assets/")) {
                directPackContent = true;
            }
        }
        if (directPackContent) {
            return new PackLayout("", "direct-pack-root");
        }
        if (firstSegments.size() == 1) {
            String candidate = firstSegments.iterator().next();
            String prefix = candidate + "/";
            boolean wrappedPack = files.stream().anyMatch(file -> file.path().startsWith(prefix + "data/")
                    || file.path().startsWith(prefix + "assets/")
                    || file.path().equals(prefix + "pack.mcmeta"));
            if (wrappedPack) {
                return new PackLayout(prefix, "heuristic-single-directory-prefix");
            }
        }
        return new PackLayout("", "no-pack-root-detected");
    }

    public String logicalPath(String observedPath) {
        if (!prefix.isEmpty() && observedPath.startsWith(prefix)) {
            return observedPath.substring(prefix.length());
        }
        return observedPath;
    }

    public boolean isHeuristic() {
        return detection.startsWith("heuristic-");
    }
}
