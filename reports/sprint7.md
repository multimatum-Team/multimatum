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

### Lenny

### Léo

### Louis

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
