Sprint 5 summary
================

### Florian
During this week, I was assigned to make an Add, Delete and Modify option for the Deadline.
In overall, it went well for the Add implementation. However, I had much more problems
for the Delete one, as I wanted to remove the deadline by swiping them left or right,
thus I have underestimated the time for this option and I didn't have the time to
do the Modify implementation. I have also discovered some errors in the DeadlineAdapter
during this sprint and thus I will, in the next sprint, do what I wasn't able to do in
addition to fix the adapter and improve it.

### Joseph

This week I worked on refactoring the repository to allow referring to
deadlines by ID, so that Florian and Léo could continue working on the UI
side. On the way, I realized that our tests were not reproducible because our
code depended on the time (system clock). I added a simple refactor that
solves this issue by using our dependency injection infrastructure that
Valentin worked on the previous weeks. My time estimates were quite accurate
this week, though I worked an hour less than the recommended 7h. Next week,
I will either work on adding multi-user support or help Louis setup the
Firebase emulator suite which will allow us to test the actual Firebase
repository and sign-in, thus greatly increasing our coverage.

### Lenny
I started by finishing fixing the PR of last week, I manage to get 100% coverage on my notifications functions. Then I refactor the code in order to switch from localDate to LocalDateTime for the deadline due date. At the same time I also generalise the usage of the homemade clockService to ensure reliable testing by mocking the `now()` function. Then I adjust notifications to make them work with fireBase unique ID assigned to deadlines (this is not merged yet since we still have discussion on some detail in the PR (mostly with timeZone management)). I also add a function to remove notification. At this point I still have to add some modification for handling multiple notifications for a single deadline.
Next week I will finish to merge my PR and I'll finish notifications and add a menu to set notification when creating new deadlines.

### Léo
During this week, my job was to expand the calendar feature in many ways (that have changed a bit). First, I had some bugs
to solve with the calendar. After that, the feature to add deadline using the calendar had to be adapted, beacause of the
new ViewModel structure. Then, I added new intuitive features to the calendar. The user can now exit the text input field
by simply touching outside, and can easily add a deadline by pressing enter instead of pressing the "ADD" button. Finally,
more tests have been added. I also wanted to display the deadlines on the calendar, but it appears that it is far more
difficult than I thought, so I will see. Next week, I want to convert the calendar activity to fragment.

### Louis
I slightly improve the coverage of the account activity but I am struggling on testing some result.
If it's not finish before friday I will just let it as it is.
Edit: I did wrong manipulation with git branching, I break all what I've done.
Next week I planned to work on generating QRCode from deadline.

### Valentin
My task for this sprint was to test the ProcrastinationDetectorService that I have implemented last sprint. At first I
didn't really know how to test a service, but I finally came across Robolectric ServiceController on the Internet. Also
on the net, I found a way of mocking final classes with Mockito, which simplified the tests. So overall it went well and,
unlike usual, I stayed within my time estimate. Next week I will still be working on ProcrastinationDetectorService, the
goal being this time to make it work even when the app is not running.

### Overall team
The main look of the app has not changed a lot, but there was a lot of work that has be done "in
the shadow" by the team. We corrected bugs, refactored the app in a lot of different ways and significantly increased the test coverage. Some new "intuitive" features were added. Now that it
is done, the team can focus again on more "concrete" stuff on the app.
