Sprint 6 summary
================

### Florian
During this week, I have worked on the display of the time of the deadline in the ListView and in
DeadlineDetails activity as we have implemented it last sprint. I have also added the possibility
to select the time when you create a deadline and to modify the deadline in DeadlineDetailsActivity.
For now, the modification of deadline is only on the UI so will link it the the viewModel next
sprint. I will also add a done option in the ListView in the main next week.
In general, I have a underestimated the implementation of time in AddDeadlineActivity thus I have
made more hours than expected but otherwise it was fine.
have
### Joseph

This week I started working on the user feature. I spent most of my time figuring out the right way
to architecture the code. I wish I had split the task in several PRs but it was bit hard to
separate. Unfortunately, the feature required significant changes to the codebase, most of the files
required at least one or two changes. Also, since the feature focuses on Firebase, I couldn't
properly test the new code. However, my time estimates weren't too far off.

### Lenny

### Léo

### Louis
During this week I finalize the QRCode generator by cleaning the actual layout, make consistent the access to the activity and refractoring a significant part of the code. I create a new class QRGenerator used in the activity and make all associated test. It wasn't so simple to refractor this but finnaly it went fine.
I start working on the deadline sort and I finished it next week.

### Valentin
My task for this week was to make the procrastination detector service work even when the app is not in use. At first I had no idea
of how to do that, so I read documentation and carried experiments. I tried to use schedulers (e.g. AlarmManager) to wake the
service up once every few seconds, but that did not work because these schedulers have a minimum time interval that is at the very
least 1 minute. I finally came across a tutorial explaining how to do a service that never stops running, using a foreground service, so I took the code of this
tutorial and adapted it to our service. Then, once it was working, I refactored it to make it clearer. For the documentation reading
and the experimentation part my time estimates were pretty accurate, but I underestimated the time that I would need for the actual
implementation. Next sprint I will continue working on the procrastination detector, and I will try to make it less sensitive.

### Overall team
