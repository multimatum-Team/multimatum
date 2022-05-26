Sprint 12 summary
================

### Florian
This week, I have implemented a listView in the calendarViewActivity to show the deadlines 
if the user select a date in it. I have also solved a problem of drakmode in this activity.
I was also supposed to make tests for `FirebaseGroupReopository` but I didn't succeed as
multiple incomprehensible errors appear at every tests I made, so I have decided to make a
cleaning of the code in it globality to remove every warning I could. With all of this, I have
made my 8 hours and I think I will work on test or in cleaning the last existing warnings 

### Joseph

This week I worked on making the app more fault tolerant by emitting warnings
instead of crashing when remote data is incorrectly formatted, but I couldn't
find a clean way to do this. I also added a dialog to prevent anonymous
users from using the groups feature, which could make the app crash. Next
week I will try to clean up the code and improve the UI for the final demo.

### Lenny

### Léo
This week, my job was to implement a map that gives the user the ability to visualize the deadline
location on a map, if he/she provides it. I have done that using Mapbox and it can be accessed from
DeadlineDetailsActivity. I also added a category search feature. When the user search a place using
the Mapbox view, he can click a category (hotel, cafe, ...) and Mapbox will select the closest one and
add it to the deadline. I also made new tests for these activites, but it was limited because I encountered
the same testing issue (with Mapbox) than the previous weeks. I also made minor UI improvements over the
project. Next week, I will work on the offline mode of Mapbox, improve its UI and try to improve the coverage.

### Louis
My task this week was to search for bug with offline mod I spotted 4 or 5 including log out without connection or non displaying of pdf when offline. I manage to fix the log out. At begining I tried to let the user log out and making very complicated stuff with false user and so on but it leads to multiple problem like not saving deadlines and making false login. So finnaly I choose to just make the user don't log out and display toast. 

### Valentin
My tasks for this week were all related to testing. Firstly I had to rename
tests (following the advice given in the code review) because they were using
different naming conventions. Then I wrote new tests for `GroupsActivity`,
`DisplayLocationActivity` and `GroupMemberAdapter`. I did not see any way to
do end-to-end tests (except maybe after spending hours reading documentation,
but the risk that the tests never work would have been very high), so I
decided to test this code in isolation. I had to use weird tricks to get
coverage on these classes, like injecting functions using Hilt, and some tests also
required the use of reflection to call private methods. The resulting tests do
not really test the correctness of the current implementation of the app, as
their specification is based on the implementation of the classes that they
test, but they can be very useful as regression tests, which is the main goal
because we know from manual testing that the current implementation works. My
time estimates were not very accurate when taken individually, but the estimated
times and actual times sum up to the same duration. I do not know yet what I
will have to do next week, but it is likely to be some testing again and/or work
on the video demo.

### Overall team
