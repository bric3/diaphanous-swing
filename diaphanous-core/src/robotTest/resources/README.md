# Robot screenshot baselines

`robotTest` compares screenshots against files in this folder.

- Record/refresh baselines:
  - `./gradlew :diaphanous-core:robotTest -Ddiaphanous.robot.record=true`
- Compare against baselines:
  - `./gradlew :diaphanous-core:robotTest`

## Current status

Baseline comparison is currently unstable and may fail across runs on macOS even after re-recording baselines.

Temporary workaround for local runs:

- `./gradlew :diaphanous-core:robotTest -Ddiaphanous.robot.tolerance=0.02`

If a baseline image is missing, tests save a candidate image to:

- `diaphanous-core/build/reports/robotTest/candidate/...`

Committed baselines should be organized by API type:

- `decorations/macos/`
- `backdrop/macos/`
