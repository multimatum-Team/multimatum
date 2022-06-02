# multimatum

[![Build Status](https://api.cirrus-ci.com/github/multimatum-Team/multimatum.svg)](https://cirrus-ci.com/github/multimatum-Team/multimatum)
[![Test Coverage](https://api.codeclimate.com/v1/badges/4b224cde0ea92d686d7a/test_coverage)](https://codeclimate.com/github/multimatum-Team/multimatum/test_coverage)
[![Maintainability](https://api.codeclimate.com/v1/badges/4b224cde0ea92d686d7a/maintainability)](https://codeclimate.com/github/multimatum-Team/multimatum/maintainability)

Multimatum is a deadlines manager application for Android, made for the
software development project course, spring semester 2022.
Its main purpose is to provide the user a easy tool to display, manage and even share all deadlines.

## Features

- Deadlines with title, description, date and time
- Date and time pre-selection based on title (e.g. entering "Meeting next
friday" as a title will automatically resolve the date)
- Upload files to associate them with deadlines
- Attach geolocation information to deadlines
- Notifications (optional)
- Procrastination fighter, which will harass you with notifications if you're
not working (based on device motion, optional)
- Sharing deadlines via QRCode
- Synchronization via Google accounts (optional)
- Creating groups of people working on common deadlines (only available when
signed-in with a Google account)
- Offline support
- Dark theme

## Building

To build the application, you'll need to add the `google-services.json` and
`credentials.json` in the `app` directory, containing the API keys to
Firebase.

Then, running the `./gradlew build` command will build the application and put
the APK in `build/intermediates/apk`, which you can then install on your
phone.

Alternatively, you can open the project with Android Studio and press the
'Run' button, which will run multimatum on the emulator or on your phone if
connected via USB.

## Screenshots

Here are a few screenshots to showcase a few of our app's features (hover for
description):

<p align="center">
  <img src="./screenshots/main-view.jpg" width="350" title="Main screen">
  <img src="./screenshots/add-deadline-menu.jpg" width="350" title="Adding a deadline">
  <img src="./screenshots/calendar-view.jpg" width="350" title="Calendar view">
  <img src="./screenshots/dark-mode.jpg" width="350" title="Dark mode">
  <img src="./screenshots/datetime-preselection.jpg" width="350" title="Date & time preselection">
  <img src="./screenshots/details-view.jpg" width="350" title="Details view">
  <img src="./screenshots/group-filters.jpg" width="350" title="Filtering deadlines by groups">
  <img src="./screenshots/group-view.jpg" width="350" title="Viewing and administrating groups">
  <img src="./screenshots/qrcode.jpg" width="350" title="Sharing deadlines via QR codes">
</p>
