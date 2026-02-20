# Technical Note 002: macOS AppKit Thread Crash During NSWindow Styling

## Context

The `translucency-core` library applies macOS `NSWindow` style changes from Java via FFM + Objective-C runtime calls.

## Bug

Running the demo (`./gradlew :demo-swing:run`) crashed with:

- exit code `133` (`SIGTRAP`)
- crash thread: `Java: AWT-EventQueue-0`
- AppKit assertion: `Must only be used from the main thread`

Observed crash path included `-[NSWindow setStyleMask:]`.

Crash excerpt:

```text
Cocoa AWT: Not running on AppKit thread 0 when expected.
Thread 25 Crashed:: Java: AWT-EventQueue-0
...
AppKit  -[NSWindow setStyleMask:]
...
libsystem_c.dylib: Must only be used from the main thread
```

## Root Cause

`NSWindow` mutation calls were executed on the AWT event dispatch thread, not the true AppKit main thread.

UI thread affinity is a general rule across desktop platforms (Swing EDT, JavaFX Application Thread, Win32 UI thread, GTK main loop, Cocoa main thread).  
The difference here is enforcement behavior: AppKit is strict and can trap/crash immediately when main-thread-only window APIs are called from another thread.  
Other toolkits are also thread-affine, but violations more often surface as undefined behavior, race conditions, deadlocks, or intermittent rendering issues instead of an immediate hard failure.

## Attempted fix (and why it was incorrect)

The first remediation attempt used `LWCToolkit.invokeAndWait(...)`.

Why this was attempted:

- It is an existing JDK macOS helper used to marshal work in the AWT/LWAWT stack.
- It appeared to provide a synchronous handoff, matching the `apply(...)` API semantics.

Why it was insufficient:

- On this runtime, it still triggered AppKit thread assertions (`Not running on AppKit thread 0 when expected`).
- It introduced problematic re-entrancy in the AWT/macOS run-loop bridge for this code path.

## Final fix

Implemented a dedicated thread bridge in `MacThreading`:

1. Dispatch to AppKit main thread using `LWCToolkit.performOnMainThreadAfterDelay(Runnable, long)`.
2. Wrap execution with `CountDownLatch` to preserve synchronous `apply(...)` behavior.
3. Capture and rethrow errors from the dispatched block.

`MacWindowStyler.apply(...)` now delegates native style mutation through this bridge.

## Runtime Requirement

The demo run task includes:

- `--enable-native-access=ALL-UNNAMED`

This avoids restricted-method failures/warnings for FFM native calls.

## Verification

After the final fix:

- repeated `./gradlew :demo-swing:run` executions completed successfully
- style changes are applied on button click without crashing

## Guardrail for Future Changes

Any call that mutates `NSWindow` state must run through `MacThreading.runOnAppKitThread(...)`.
Do not call AppKit setters directly from the AWT event thread.

> [!TIP]
> You can inspect macOS Java crash reports directly from the command line.
>
> ```bash
> # Latest Java crash reports (bypass aliases like eza->ls)
> command ls -lt ~/Library/Logs/DiagnosticReports/java*
>
> # Pretty-print latest .ips report (JSON format)
> jq . "$(command ls -t ~/Library/Logs/DiagnosticReports/java*.ips | head -n1)"
>
> # Extract a concise summary from the latest .ips report
> jq '{timestamp, bug_type, incident, procName, exception, termination, faultingThread}' \
>   "$(command ls -t ~/Library/Logs/DiagnosticReports/java*.ips | head -n1)"
>
> # Show frames from the triggered/crashing thread
> jq '.threads[] | select(.triggered == true) | {id, name, frames}' \
>   "$(command ls -t ~/Library/Logs/DiagnosticReports/java*.ips | head -n1)"
> ```
