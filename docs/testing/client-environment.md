# Client verification environment: the blocker was wrong

Several lab results state that client verification is impossible here because the build
host has no display. **That is incorrect and this document corrects it.** A working
graphical client was launched from this environment on 2026-08-28.

The mistake matters beyond convenience. Because the constraint was assumed rather than
tested, every client-side question was deferred to a manual checklist for the user, and
several lab decisions were recorded as "server verified, client pending" when they could
have been finished automatically.

## What was actually verified

`./gradlew runClient` was run with no special arguments and reached the main menu.

| Observation | Evidence from the run |
|---|---|
| A display exists | `DISPLAY=:0`, one screen at 2560x1440, X11 socket connectable |
| Java can open windows | An AWT frame opened and `Robot` screen capture succeeded; `isHeadless=false` |
| Hardware-accelerated OpenGL works | `Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8` |
| The window system initialised | `Backend library: LWJGL version 3.4.1-snapshot` |
| Rendering genuinely ran | 13 texture atlases stitched, including `blocks.png-atlas` at 2048x2048x4 |
| The mod loaded client-side | Client initialisers logged, for example `(forever/guide)` and `(forever/equipment)` |

The client was launched, observed, and stopped cleanly.

## What still does not work

Two limitations are real, and neither prevents client verification.

- **Audio is unavailable.** The log shows `Error starting SoundSystem. Turning off sounds
  & music`. Sound-related checks cannot be performed here.
- **Screenshots of the client window fail.** Root-window capture returns black because
  the compositor presents windows separately. Verification must therefore read the log
  and mod state rather than compare images. Any check whose only possible evidence is a
  screenshot still needs a human.
- **The narrator is absent** (`libflite` missing), so accessibility narration cannot be
  checked here. Text-based accessibility cues still can.

## Why the original conclusion was wrong

The environment reports itself as headless-looking in the usual quick checks: there is no
`/dev/dri`, and `glxinfo` and `Xvfb` are not installed. The earlier conclusion stopped
there. In fact the platform supplies a compositing X server and a software or
paravirtualised Mesa driver, so OpenGL 4.5 is available without a GPU device node and
without installing anything.

The general lesson is the one this project already applies to gates and tests: an
environment limit should be **demonstrated by attempting the operation**, not inferred
from the absence of a tool. Trying `runClient` once would have settled it at any point.

## What this changes

- Client-side checks may be attempted automatically. Prefer that over deferring to the
  manual checklist.
- `docs/testing/client-verification.md` remains useful for what genuinely needs human
  eyes: visual appearance, colour and contrast judgement, narration, sound, and
  multiplayer with two real players.
- Lab results that recorded a display limitation are annotated rather than rewritten,
  because their conclusions were correct on the evidence available at the time. The
  correction is recorded here so the next agent does not inherit the false constraint.

## Diagnosing a stray client

`runClient` never exits on its own, so a client can outlive the command that started it
and then hold the display and the Gradle lock.

Check for one with `pgrep -af devlaunchinjector`, `ps`, or `/proc/<pid>`. **Do not use
bare `pgrep devlaunchinjector`**: that matches the process *name*, which is `java`, so it
reports zero even while a client is running. The `-f` flag matches the full command line,
which is where the launcher class appears.

Before killing a process you did not start, confirm it is actually live via `/proc/<pid>`
and consider that another agent may own it.

## How to reproduce

```sh
export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1   # adjust for the local JDK 25 install
./gradlew runClient
```

Confirm `Using graphics backend OpenGL` and the texture-atlas lines in the output, then
stop the client. Note that `runClient` does not exit on its own, so run it with a timeout
or stop it explicitly.
