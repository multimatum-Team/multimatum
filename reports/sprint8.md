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
This week I finish to implement notifications by adding a modification menu to the deadline modification activity. I also make notification enable/disable in setting functionnal.
Next week I'll work on the user story about adding additionnal data to deadline such as pdf or images.

### Léo
I refactored and finished the dark mode feature. I centralized every
color and style attribute of the entire code in the corresponding theme/
colors file according to the darkmode. I also had to refactor the deadline 
details activity in order to support the dark mode (which require dynamic 
changes in this class). Next week I will work on additional attachements 
in the deadline.

### Louis
I had one task this week that was finnally split into 2 task. To make the QRReader functionning I had to refractor the QRGenerator I made to make it contain all necessary data to create a deadline. I used the Gson lib for this but the LocaldateTime type wasn't taked in charge by Gson so i had to write a serializer and a deserializer to make it work. And after that I got thread problem with the view model before everything work. Next sprint I'll do the UI for the group feature

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
During the week, everyone worked well, we completed the login, the notifications and the dark mode features. We keep fixing bug and working on having a good test coverage. Some of us also started working on new features.
During our meeting, we went trhough the product backlog and we conclude that we had a good rythm that should allow us to complete all our users stories at the end of the project, with some additionnal time to fix bug and fine tuning the application.
