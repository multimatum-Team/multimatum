Sprint 7 summary
================

### Florian
During this week, I have worked on linking DeadlineDetailsActivity to the viewModel to allow the modifications
made in this activity to really change the data of the deadline in firebase. With the view model, I have also changed
the way to send the data to this activity by simply sending the id of the deadline instead of all the data of it,
by recuperating the data via the viewModel. However, I have realised that it was possible tha the viewModel in the activity
hadn't the time to fetch the deadlines, thus leading to nullPointerException and crashing the app.
I have tried many thing to solve this and read a lot of documentation before finding a solution and made my PR.
With this and a little work on trying to stabilise the CI, without success on my tries and thus leading to no commit for this,
I have largely underestimated my time and I wasn't able to make one of the issue assigned to me.
I will do it next time and I also try to refactor all the tests to work with robolectric, hoping to increase more
the stability of the CI.

### Joseph

After the users PR last week, I decided to do a lighter sprint and simply test
the only few functions introduced in my last PR that were testable (i.e. the
ones that don't depend on Firebase). I spent most of my time reading the
Firebase documentation to find out how to test Firebase-dependent code without
much success. Fortunately, all the reading was not in vain since I was
able to familiarize myself with the Firebase emulator suite, which let me
test Firestore rules to protect user data, which was my second assigned
task this sprint. Next week I will start working on user groups to enable
sharing synchronized deadlines.

### Lenny
During this week, I work on notifications (again...). With the addition of shared deadline and users, we figured out that it was too dangerous to have notifications shared with other deadline's users. So I rework notifications to make them local through a storage in shared preferences. Then I set up functions to ensure that notifications are listenning to modification/addition/deletion of the deadline and are updated accordingly. Time estimation were ok and we have now a functionnal, safe and reliable (regarding the non-exhaustive test I've done) notification system.
Yet notifications are set when creating a new deadline, next week I'll work on having a menu that allow user to modify those notifications.

### L??o
During this week, I implemented a feature that we have spoken about for few weeks: the dark mode. We can now choose
in the settings to enable the darkmode or not. The darkmode is basically a new theme that "inverts" almost all colors
into darker ones that are far more convenient to watch in a dark environnement. I also chaged the design of the settings
activity, using new colors and icons for the different options. It seemed not so complicated to implement but it was far
more complicated than I thought. To implement the darkmode, it was necessary to understand where the color attribute are defined for each view,
and make sure that it does not create a conflict with what we have in the code/xml. Some views require a theme color, like the text
for example (defined in the theme files) and some others (like the custom ones such as deadline views) require specific colors that must be defined
in another file (colors.xml). So now, every theme/color are centralized in two files and it will be easy to add/modifiy new views. For the next sprint,
I will work on a new feature that provide the user the ability to customize the app in the settings activity.


### Louis
during this week I decided to reorganize file structure with new package to make the project more clea. It was pretty simple but i did bad checkout with git so it takes more times than expected. Then I finnaly make the task to ordrered deadline. I had to refractor large part in the deadline adpater and add a new functino to sort ine the right order. I started to work on the system of recognizing QRCode deadline but it's not ready yet. I will continue this task in the next sprint.

### Valentin
My first task for this week was to make the procrastination detector service less sensitive. I introduced some latency between the time
when a first move is detected and the time when a toast is displayed. I also disabled the service when the app is running. To
be able to do this, I had to understand a bit more of the Android activity lifecycle, and I wanted to use logging for that
purpose (and for debugging as well), so I created an additional task that was to create a module with simplified logging methods.
These two tasks took me slightly more time than estimated. My second task was to improve the stability of the CI, because some
tests often fail for non obvious reasons. It turned out that I underestimated this issue. I initially thought that it was just an
issue with `Intents.init`/`Intents.release` in a few tests, but there seem to actually be several distinct bugs. As a result, this
took me much more time than my time estimate, but I decided to continue trying to make the CI work anyway because this issue is blocking
for everyone in the group and should therefore be fixed as soon as possible. The result seems to be that failures are less frequent,
but some tests still fail sometimes.

### Overall team

During this sprint, we continued working on the completion of the most important features of our app, both for usability and to match
the app requirements. We are also trying to make our app sounder (e.g. by working on the issues with the CI) and more reliable. While there is still some work to be done, the app
is now starting to look like a true Android app, and is becoming less and less prototypical.<p>
Concerning the performance of the team, at the beginning of the sprint we were a bit tired after our midterms and we were happy to have
an extra week to complete our tasks, thanks to the holidays. But overall we did our job.
