#!/usr/bin/env bash
#
# Convenience wrapper for the Matcha audit CLI.
#
# Why this exists: the audit tool is compiled for Java 25 (class file version 69).
# The Gradle `installDist` launcher resolves the JVM from JAVA_HOME or whatever
# `java` is first on PATH. On a machine whose default JDK is older, running the
# launcher directly fails with a bare UnsupportedClassVersionError, which is a
# confusing message for something that is really just a misconfigured JAVA_HOME.
#
# This wrapper checks the JVM version first and prints an actionable error, per
# the project rule that errors must be actionable rather than raw stack traces.
#
# Usage:
#   ./scripts/run-matcha-audit.sh --input vendor/matcha/<file>.zip --output generated/matcha/<version>
#
# Any arguments are passed through unchanged to the CLI.

set -euo pipefail

REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
TOOL_DIR="$REPO_ROOT/tools/matcha-audit"
LAUNCHER="$TOOL_DIR/build/install/matcha-audit/bin/matcha-audit"

readonly REQUIRED_MAJOR=25

# Resolve the java binary the launcher would actually use.
if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
	JAVA_BIN="${JAVA_HOME}/bin/java"
elif command -v java >/dev/null 2>&1; then
	JAVA_BIN="$(command -v java)"
else
	echo "run-matcha-audit: error: no Java runtime found." >&2
	echo "  Install JDK ${REQUIRED_MAJOR} and set JAVA_HOME, then retry." >&2
	exit 3
fi

# `java -version` prints "25.0.4.1" or "21.0.12"; take the leading major number.
JAVA_MAJOR="$("$JAVA_BIN" -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+).*/\1/')"

if ! [ "$JAVA_MAJOR" -ge "$REQUIRED_MAJOR" ] 2>/dev/null; then
	echo "run-matcha-audit: error: Java ${REQUIRED_MAJOR} or newer is required, found major version ${JAVA_MAJOR}." >&2
	echo "  Resolved java: ${JAVA_BIN}" >&2
	echo "  Set JAVA_HOME to a JDK ${REQUIRED_MAJOR} installation and retry, for example:" >&2
	echo "    export JAVA_HOME=/path/to/jdk-${REQUIRED_MAJOR}" >&2
	echo "  See docs/dependency-baseline.md for the pinned toolchain." >&2
	exit 3
fi

if [ ! -x "$LAUNCHER" ]; then
	echo "run-matcha-audit: the CLI is not built yet. Building it now..." >&2
	(cd "$TOOL_DIR" && ./gradlew --quiet installDist)
fi

exec "$LAUNCHER" "$@"
