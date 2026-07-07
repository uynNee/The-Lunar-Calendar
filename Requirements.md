# Android Lunar Calendar App (Lich Am + Google Calendar sync)

## Context

User loves the iOS app "Lunar Calendar and Widget" (id6446422065) — clean, minimal Vietnamese lunar calendar with widgets. No Android version exists; Play Store alternatives are ad-heavy and poorly designed. Goal: build an Android app inspired by its clarity (not a pixel copy), adapted to general calendar-app expectations, with Google Calendar read+write integration.

User profile: CS graduate, designer, understands code concepts — Claude does the heavy lifting on Kotlin. Distribution undecided: build sideload-first, keep Play-Store-ready (own design identity, minimal permissions, no ads/tracking).

## Decisions (confirmed with user)

- **Calendar sync**: read + write, via Android `CalendarProvider` (device-synced Google accounts). No OAuth, no backend, works offline.
- **Widgets**: full set in v1 — today lunar date, mini month grid, moon phase / rằm–mùng 1 countdown.
- **Distribution**: sideload first, Play-ready practices throughout.

## Tech stack

- Kotlin, Jetpack Compose (app UI), Jetpack Glance (widgets)
- Room — cache only if needed (likely skip in MVP; CalendarProvider queries are fast)
- WorkManager + AlarmManager (widget refresh: midnight rollover + periodic)
- No network, no backend, no third-party deps beyond AndroidX
- New project directory (e.g. `~/Projects/lunar-calendar-android`), git repo from day 1

## Milestones

### M0 — Environment (half a day)
- Install Android Studio (macOS), SDK, emulator image
- Scaffold project: empty Compose activity, min SDK 26 (Android 8, covers ~97% of devices), target latest
- Verify build runs on emulator; connect physical Android device via USB for real testing

### M1 — Lunar engine (the foundation, ~1 week)
- Port Hồ Ngọc Đức's Vietnamese lunar calendar algorithm to Kotlin (`LunarCalendar.kt`): solar↔lunar conversion at UTC+7, leap months (tháng nhuận), Can Chi (year/month/day), moon phase, good/bad days (ngày hoàng đạo/hắc đạo) if desired
- Pure Kotlin module, zero Android dependencies → fully unit-testable
- **Unit tests against published conversion tables 1900–2100**, especially leap-month years (e.g. 2023 nhuận tháng 2, 2025 nhuận tháng 6). This is the correctness core — everything else displays its output.

### M2 — App UI (~1–2 weeks)
- Month grid: solar dates large, lunar sub-dates small; highlight mùng 1/rằm; leap-month notation ("1/2" style)
- Day detail view: full lunar date, Can Chi, moon phase, events for that day
- Vietnamese holidays + lunar events (Tết, Giỗ tổ Hùng Vương, rằm tháng Giêng, …) as built-in data
- Design language: keep the iOS app's clarity (typography, whitespace, cards) but own identity + Material 3 conventions (predictive back, dynamic color optional). User designs, Claude implements — Figma or sketches welcome as input.
- Light + dark theme from the start

### M3 — Google Calendar read + write (~1 week)
- `READ_CALENDAR` / `WRITE_CALENDAR` runtime permissions with graceful denial state
- Read: query `CalendarContract.Instances` for visible date range, merge into month grid + day view; calendar-account picker (show/hide calendars)
- Write: create/edit/delete events from day view → inserts into chosen Google calendar via `CalendarContract.Events`; Google's own sync engine pushes to cloud
- Recurring events: read via Instances (handles expansion); for MVP, **create simple + yearly-recurring events only** (yearly covers giỗ/anniversary use case); full RRULE editing is post-MVP
- Optional flagship feature: "lunar-recurring" events (e.g. giỗ on lunar date each year) — since lunar dates map to different solar dates yearly, implement as: app computes next N solar occurrences and writes them as individual events tagged in description. Post-MVP if timeline slips.

### M4 — Widgets (~1–2 weeks)
- Glance widgets: (a) today lunar date + Can Chi, (b) mini month grid with lunar sub-dates, (c) moon phase / countdown to next rằm–mùng 1
- Refresh strategy: AlarmManager exact-ish alarm at local midnight for date rollover + WorkManager periodic (≥15 min interval) as backstop; update on `ACTION_DATE_CHANGED`/`TIMEZONE_CHANGED`/`TIME_SET` broadcasts
- Widget configuration screen (size/theme options)
- Test on physical device incl. aggressive-battery OEM if available (Samsung/Xiaomi) — biggest reliability risk on Android

### M5 — Polish & release prep (~1 week)
- App icon, splash, empty/error states, Vietnamese + English strings
- Sideload: build signed release APK, install on user's phone
- If Play Store later: $25 one-time dev account; new personal accounts need 12-tester closed test for 14 days before production; privacy policy (trivial — no data collected); ensure design is distinct from the iOS app to avoid copycat rejection

## What to expect (the honest version)

- **Total: roughly 5–8 weeks** of iterative sessions at this pace; MVP-without-widgets usable on phone in ~3 weeks
- Lunar math and CalendarProvider are well-trodden — low risk
- Widget reliability across OEMs is the main pain; plan for iteration after real-device use
- Cost: $0 to sideload; $25 once if publishing; no server costs ever

## Verification

- M1: JUnit tests vs Hồ Ngọc Đức reference tables (1900–2100 spot checks + all leap months)
- M2–M3: run on emulator + physical device; create event in app → confirm it appears in Google Calendar app/web; create in Google Calendar web → confirm it appears in app after sync
- M4: add each widget to launcher, cross midnight (emulator time manipulation) and confirm rollover; screenshot set light/dark
- Manual regression around DST-less UTC+7 assumption when device timezone ≠ Vietnam (decide: lunar date follows device TZ or fixed VN TZ — default fixed VN, setting later)

## First concrete step after approval

Scaffold project + implement `LunarCalendar.kt` with tests (M0+M1 start) — foundation everything else builds on.
