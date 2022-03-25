Sprint 4 summary
================

### Florian

At the beginning of the sprint, I have updated the bootcamp in hope to made the CI correctly detect the coverage.
However, this fix wasn't enough so I had to find another way to correct it. Now the CI detects correctly the coverage,
which improve our coverage by almost 10% with all the tests previously not detected. Unfortunatly, after the fixes,
I hadn't the time to make all of what I have planned, so I only made an activity that show the details of a deadline and
wasn't able to implement the "Add" button for the deadline within my 8 hours, thus I will do it in the next sprint with the
"Modify" button for the deadlines.

### Joseph

This week I worked on the synchronization between Firebase and Florian's deadline list view
implemented last sprint. As I was the last one to merge my PR, I had to glue all the code from this
week as well as previous weeks, so that the application uses Firebase to get the list of deadlines
instead of a hardcoded list, therefore I had to dive into almost all parts of the codebase.
Thankfully Louis came to my rescue to help me integrate the view-model into the UI. After struggling
a little bit to put this all together, it was quite satisfying to see the app take shape. I'm glad
this is finally implemented, since it was blocking us from working on core features.

### Lenny

I tried to implement test for the notification. I struggled a lot, I only manage to have 1 test working and the coverage is really bad. I spend a lot of time on it. 
I also refactor notification as a separate class.

### Léo

For this week, I implemented a feature that allow the user to select a day using a calendar to add a deadline to it. A new activity containing a new calendar view
was implemented, including a text field for the title of the deadline and a button to add it. I also removed the "Action Bar" of the app. The corresponding tests were also done.
I am happy with this week's work, everything worked as expected and in the estimated time. Next week, I will continue to work on the calendar to potentially display the deadlines
on it, work on a better layout and if possible connect it to the database. More tests have to be made.

### Louis

This week I did the log out system and the profile page using fragment. The log out system was pretty easy but when i tried to test it i struggled a lot.
At it's imply some parts og the same mechanics as login i assume this is not really testable. The fragment part take me more time because I have to figure out 
how it's work. Finally I helped Joseph on the UI part of synchronizing app with the data base. I had to understand how the synchronization works and how to use the listView
it was not so simple. Unfortunately I didn't did the part on sorting deadlines by done attribute because it requires the synchronization works.  
I'll do it next sprint.

### Valentin

My task for this week was to start working on a movement detection system, the goal being that the app can detect when the user is not working
and give him/her a warning (i.e. this is the feature that Yugesh suggested to us on the first meeting because we must use a sensor). I cannot say
that it went well... On the good side, the app is now able to detect when the user moves his phone. But currently it only works when the app is
open. Furthermore, although I spent more time working on tests than on the feature, I did not manage to make working tests for this feature. I tried
to mock the sensor, but the classes that should be mocked are final. I tried several strategies to overcome that, but it did not work. As a result,
my time estimate was largely exceeded. So next week I will very probably have to work on these tests, and possibly on making the movement detection
work even when the app is not in use.

### Overall team

This sprint was quite productive, but all of us faced problems with testing, as usual. It seems that
we spend much more time on testing that working on the actual features. Nevertheless, we managed to
implement all the features that we decided to work on this sprint, the only things we didn't finish
are tests. Even if Florian managed to fix the CI and increase the coverage by 10%, our overall
coverage dropped by a significant amount. On a more positive note, since we managed to get Firebase
working, we can finally get working on new core features in the application for next sprint.
