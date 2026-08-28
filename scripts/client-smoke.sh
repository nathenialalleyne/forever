#!/usr/bin/env bash
# Launch the development client, confirm it reaches a rendering main menu, and stop it.
#
# This exists because the client half of several decisions was deferred on the belief
# that no client could run on a build host. That belief was untested and false. See
# docs/testing/client-environment.md. This script makes the check repeatable so the
# false constraint cannot quietly return.
#
# It verifies that the client renders, not that it looks correct. Visual judgement,
# sound, narration, and multiplayer still need a human: see
# docs/testing/client-verification.md.
set -euo pipefail
IFS=$'\n\t'

readonly ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
# The client never exits on its own, so it is always run under a timeout. Startup is
# dominated by asset loading, and the default is generous enough for a cold cache.
readonly TIMEOUT_SECONDS="${CLIENT_SMOKE_TIMEOUT:-420}"
readonly LOG_FILE="${CLIENT_SMOKE_LOG:-${TMPDIR:-/tmp}/mrh-client-smoke.log}"
failures=0

fail() { echo "client-smoke: FAIL: $*" >&2; failures=$((failures + 1)); }
pass() { echo "client-smoke: ok: $*"; }

cd "${ROOT_DIR}"

if [[ -z "${DISPLAY:-}" && -z "${WAYLAND_DISPLAY:-}" ]]; then
    echo "client-smoke: SKIP: no DISPLAY or WAYLAND_DISPLAY is set." >&2
    echo "client-smoke: this is a property of the machine, not of the pack." >&2
    exit 0
fi

echo "client-smoke: launching the client for up to ${TIMEOUT_SECONDS}s"
echo "client-smoke: log: ${LOG_FILE}"

# The client is expected to be killed by the timeout, so a non-zero status here is
# normal and must not fail the script. The log contents are the actual evidence.
set +e
timeout "${TIMEOUT_SECONDS}" ./gradlew runClient >"${LOG_FILE}" 2>&1
set -e

# Never leave a client running: a stray process would hold the Gradle lock and the
# display, and would silently corrupt a later run's evidence.
pkill -f "devlaunchinjector" 2>/dev/null || true

if [[ ! -s "${LOG_FILE}" ]]; then
    fail "the client produced no output at all"
    exit 1
fi

# A rendering backend proves more than "the process started": it proves a real GL
# context was created, which is exactly what the false blocker denied was possible.
if grep -qE "Using graphics backend" "${LOG_FILE}"; then
    pass "$(grep -oE 'Using graphics backend.*' "${LOG_FILE}" | head -1)"
else
    fail "no graphics backend was initialised"
fi

# Texture atlases are stitched late in startup and only when rendering genuinely
# works, so they are a stronger signal of reaching the menu than any single log line.
atlas_count="$(grep -cE "Created: .*-atlas" "${LOG_FILE}" || true)"
if [[ "${atlas_count}" -ge 5 ]]; then
    pass "stitched ${atlas_count} texture atlases"
else
    fail "only ${atlas_count} texture atlases were stitched; the client did not reach the menu"
fi

if grep -qE "\(forever" "${LOG_FILE}"; then
    pass "the companion mod initialised on the client"
else
    fail "the companion mod did not log any client initialisation"
fi

# Known environment gaps are excluded deliberately and named, so that a real new
# warning is not lost in noise this host will always produce. Classpath lines are
# excluded because a single missing-native stack trace prints the entire classpath.
unexpected="$(grep -E "WARN|ERROR" "${LOG_FILE}" \
    | grep -viE "narrator|flite|soundsystem|openal|text2speech" \
    | grep -viE "\.jar|/home/|classpath" || true)"
if [[ -n "${unexpected}" ]]; then
    echo "client-smoke: note: unexpected warnings or errors were logged:" >&2
    printf '  %s\n' "${unexpected}" | head -20 >&2
    fail "the client logged warnings beyond the known environment gaps"
else
    pass "no warnings beyond the known audio and narrator gaps"
fi

if [[ "${failures}" -gt 0 ]]; then
    echo "client-smoke: ${failures} check(s) failed; see ${LOG_FILE}" >&2
    exit 1
fi

echo "client-smoke: all checks passed"
