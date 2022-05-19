Sprint 11 summary
================

### Florian
This week, I was tasked to add custom notifications for the deadline and to show the deadline
in the calendar view. The two of them were complicated because it needed some libraries that are
difficult to test and finding a library for allowing the calendar to show the deadline was quite long
as there exist multiple of them but only a very few who can be coded in kotlin.
However, I have correctly managed my time and made just 1 hour more than expected.
Next week, I will make some tests to improve the coverage and maybe also improve the UI.

### Joseph

I worked on implementing group invites as planned. Most of my time was spent
on reading Firebase documentation but the implementation turned out to be
simpler than I expected. I also spotted a bug but it wasn't too hard to fix
either. My time estimates were quite accurate. Next week I will work on
refactoring parts of the codebase as indicated by the assistants' code
quality issue. I will try like everyone else to improve coverage and UX
to get a nice looking app for the demo.

### Lenny

### Léo
This week, I worked on the location feature that gives the user the ability to select a location for a particular deadline and the location name
and coordinates (GeoPoint) will be stored on Firebase. When the user looks at the deadline details, the location name will appear (if there is one). 
Some UI improvements/icon addition were also made (main screen and deadline details). The biggest part for me was to solve the Mapbox testing issue.
I had to create a new activity for it, so all the old tests can be executed without any problems. I also created new test to improve the coverage
of the (de)serialization activities (they had to be modified to support location). Next week, I will improve the Mapbox UI, add cateoriy searching and
work on the coverage of the search location activity.

### Louis
This week I was supposed to work on adding group and fixing a graphic bug i found but I finally only work on the feature. As the last week it was very easy to implement it took me like 2 hours. Then I spent 8 hours on only 1 test, I didn't need more but for some inexplicable reason the tests wasn't working. While this test was very similar to other test we already did with other feature. I tried lot of things changing the code, talk with Floriant that already tests this kinf of feature and other but nothing works so I just dropped it. Next week I'll check that the app works offline.
### Valentin
This week I had to help Léo with the tests of the location feature. I tried
several solutions, the first one being to force Robolectric to execute the tests
on a larger screen because we thought that the problem could be a lack of space
for widgets in `AddDeadlineActivity`. I also attempted to fix the initialization
of the library, and used logs and the debugger to try to find what was failing.
Unfortunately, after spending the planned time (4 hours) on it, I had still
not fixed anything... At that point Léo sent you the email asking for help,
and we decided that I should move to another task. Thus I had nothing to
push for this task. Then my next task was to write tests for the QR code reader.
This was more productive and for once I was able to stay within my time
estimate... Next week I will probably have to write tests to improve coverage
where we can, or to refine a feature, but the exact tasks remain to be defined.

### Overall team
It has been a quite tough week for the team, but it has paid. All the features
are almost finished and the test coverage is good. The calendar view has changed for
a new view that contains event indicators. But it was hard to find a library that is not
old and kotlin compatible. The PDF can now be stored with a deadline, and it has be done
without any particular problem. The group addition feature was quite hard to implement beacuse
of testing. The location feature was also hard to implement because of testing, but it has been
solved with class splitting. We also added a new system to custom the notification delays.
Some features created a small coverage drop (custom notifications and Mapbox), but are almost
compensated with new tests (serialization, QR-code reader). And we will continue to work on that
next week. We also will finish all the started features and improve the UI.
