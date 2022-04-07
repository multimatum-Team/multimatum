Sprint 6 summary
================

### Florian
During this week, I have worked on the display of the time of the deadline in the ListView and in
DeadlineDetails activity as we have implemented it last sprint. I have also added the possibility
to select the time when you create a deadline and modify the deadline in DeadlineDetailsActivity.
For now, the modification of deadline is only on the UI so will link it the the viewModel next
sprint. I will also add a done option in the ListView in the main View next week.
In general, I have a underestimated the implementation of time in AddDeadlineActivity thus I have
made more hours than expected but otherwise it was fine.

### Joseph

This week I started working on the user feature. I spent most of my time figuring out the right way
to architecture the code. I wish I had split the task in several PRs but it was bit hard to
separate. Unfortunately, the feature required significant changes to the codebase, most of the files
required at least one or two changes. Also, since the feature focuses on Firebase, I couldn't
properly test the new code. However, my time estimates weren't too far off.

### Lenny
This week, I added an UI interface to set notification when creating a new deadline, I also make multiple notification for a single deadline possible. Those change doesn't set the notification yet but it's just a matter of 2 lines of code, I just need to figure out where to put them. I achieved a accurate time estimation for my Issue and keep coverage high.
Next week I'll make notification handle modification when a deadline is modified.

### Léo

### Louis
During this week I finalized the QRCode generator by cleaning the actual layout, make consistent the access to the activity and refractoring a significant part of the code. I create a new class QRGenerator used in the activity and make all associated test. It wasn't so simple to refractor this but finally it went fine.
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
During this sprint, multiple things were done in the UI and "in the shadow". The UI has a better design, we are finally starting to put the activities where they should really be, we have made major modifications in the viewModels to add user, we have added multiple fonctions when the app is not on view and all of this without reducing the coverage. It was in overall a good sprint and we will continue in this way next week.
However, we had some troubles with CodeClimate who was seeing issues everywhere and was very "harsh", we have thus decided to increase the threshold of similar character to reduce the issues who where just two similar line
