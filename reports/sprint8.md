Sprint 8 summary
================

### Florian
This week I have implemented the option to modify the state of a deadline (TODO or DONE) from the main 
activity to increase the usability of our app. I had some problem with adding a button in the adapter
but I have found a solution after some research. I have also resolved the tests that checked permissions
who were causing instability in the CI by passing them in Robolectric. In overall, I have correctly 
estimated my time and I will work on adding description in deadline next week.

### Joseph

This week I started to work on user groups which will probably take a few
sprints to reach a minimal viable feature. This sprint I decided to start
the implementation by defining the groups on the Firebase side. Since I
have been working a lot on Firebase this semester my time estimates are
both getting lower and more accurate. Valentin suggested that I could test
Firebase-dependent code using mocking, which turned out to be indeed
possible, at the cost of basically reimplementing the entire Firebase logic
(or rather all the Firebase logic that we use). This let us finally reach the
80% coverage threshold, and we can increase it even more by applying the same
technique to currently untested Firebase code from previous weeks. Next
sprint, Louis will help me by tackling the UI side of the groups features,
while I will try to implement the viewmodel, and figure out how to use
Firebase email actions to send invites for people to join our group.

### Lenny

### Léo

### Louis

### Valentin
I had 3 tasks to do this week. The first one was to disable the logging methods of LogUtil.kt when the
app is built for release (while keping them enabled when it is built for debug). For this task, my time
estimate was accurate. The second task was to split the ProcrastinationDetectorService into 2 files, so
that it becomes more readable (CodeClimate was complaining about it). This took me less time than
expected because I thought that I would have to modify the tests, but it was not the case. The third
task was to start working on a system that is able to recognize dates and times in deadline titles. I
decided to implement a parser from scratch for that purpose, because I think that finding a library that
does exactly what we need would be difficult and would take more time than a scratch implementation. It
is currently able to recognize some expressions of a time (e.g. "8am"), but it cannot recognize dates yet,
so I will have to continue working on it next sprint. I spent more time than estimated on this task.

### Overall team
