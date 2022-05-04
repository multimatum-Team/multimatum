Sprint 9 summary
================

### Florian
This week, my main focus was adding on AddDeadlineActivity and DeadlineDetailsActivity 
the option to add and modify descriptions for the deadline. As the area in the UI 
for the desciption take space, I have decided to refactor the selection and the 
editions of the notification to free space and allowing to add more options in 
the future. The add of the description didn't take much time, but the refactoring
of the notifications what much more difficult than expected, I suppose because
it wasn't me that have wrote this part during last sprints. Next week, I think I
willwork on the UI to add the possibility to select a group when adding deadline
or to create one.

### Joseph

This week I worked on implementing the grou viewmodel which required a bunch
of refactoring in other repositories and activities. While refactoring, I spot
a few bugs which I managed to fix. Yet again, I am working on Firebase code so
my PR had a small diff-coverage (under the 50% threshold), so I chose to test
the `FirebaseDeadlineRepository` (#190) to reach the threshold. I know making
a PR this big is not a good idea but I saw no other way around the coverage
threshold, apologies to my team mates which are going to review it. Next week,
I will work on Firebase action links to enable users to invite people to their
group, and I might try to work on the UI as well.

### Lenny

### Léo

### Louis

### Valentin

My task for this week was to (almost) finish the parser for dates and times.
There were 3 scrum board tasks to implement this, but they are strongly
connected so I was working on all three at the same time and thus it is a
bit difficult to estimate the time that each of them took, but generally
speaking it was slightly above my time estimates. During the sprint, we also
decided that I should include a basic use of the parser in the app (which was
initially not planned because I thought that it would be more complicated than
it actually is), so that we
have something to show during the demo. Next sprint, I will have to add more
"parsing patterns" to the parser (i.e. make it able to recognize more ways of
writing dates and times), make a popup that gets displayed to the user to ask
whether s-he want to take the date/time found by the parser when a deadline is
created and add a slider to modifiy the sensitivity of the procrastination
detector.

### Overall team
