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

### Léo
During this week, my job was to expand the calendar feature in many ways (that have changed a bit). First, I had some bugs
to solve with the calendar. After that, the feature to add deadline using the calendar had to be adapted, beacause of the
new ViewModel structure. Then, I added new intuitive features to the calendar. The user can now exit the text input field
by simply touching outside, and can easily add a deadline by pressing enter instead of pressing the "ADD" button. Finally,
more tests have been added. I also wanted to display the deadlines on the calendar, but it appears that it is far more
difficult than I thought, so I will see. Next week, I want to convert the calendar activity to fragment.

### Louis

### Valentin

### Overall team
