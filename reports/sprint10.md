Sprint 10 summary
================

### Florian
This week I have added the possibility to select a group when you want to 
add a deadline if the user is the owner of it and made some modifications
in DeadlineDetailsActivity to show in wich group the deadline is and allow 
only the owner of the group of the deadline to modifiy it.
This was more complicated than expected but I have succeeded to do it in 8 
hours. I plan to modify the system of notification to add modularity and
add in the Calendar view the possibility to see in when all the deadline are.


### Joseph

This sprint I continued working on the groups feature, by adding a group details activity to enable
users to interact with groups (rename/delete/leave the groups, kick users, ...). Before doing this,
I needed to setup a new repository that stores public user information in Firebase (only their
names for now), because Firebase only lets us access user information when being logged in, while
I needed to show usernames of group members. I also helped Louis a little bit to finish the group
filter on the main view. My time estimates were quite accurate and it was satisfying to see the
group feature coming all together. Next week I will continue and possibly finish the groups, by
implementing group invites.

### Lenny
This week I started by adding a validation pop-up on the dateTime parsing when we add a deadline. Then I worked on uploading the pdf on firebase when a new deadline is added with a linked pdf. I did spend a good amount of time on understanding how dependencies providers worked to implement testing of this feature. Next week, I'll finish to implement the pdf feature.

### Léo
This week I worked on a new feature that enables the user to select a location 
when creating a new deadline. The Mapbox API has been used, and provide the user 
an interface to enter a location, and the user will then have the choice between 
the found locations. When selected, the location name is displayed on the add 
deadline screen. In the future, the coordinates will be extracted and stored on Firebase.
Everything worked fine on the emulator, but I encountered an "Inflating Excepetion" 
in the tests that I was notable to solve, even with a help. So I had no choice but
to add my work as a comment for this PR until solved. It was not possible to do 
a "light" PR because if there is code about the location search bar in the project,
the Inflate Excpetion occurs during the tests (even without new tests). Next week, 
I will work on this issue and if possible, store and process the extracted coordinates.

### Louis
This week I had 2 task, make a filter on the main activity and create an activity to add group. For the filter I had to use spinner, so I read a lot of documentation on it. THen Joseph and Lenny made a remark. At the begining I could filter if deadlines were mine or in a group, they suggested the possiblity to filter by individual group. It was a nice idea but it took me a lot of time to implements this way, so I didn't do the activity for group. I'll do it next week while helping Leo on the geolocalisation.

### Valentin
This week I had two main tasks. The first one was about the procrastination
fighter. I added a slider so that the user can adjust the sensitivity. It
was not too difficult in itself, even though it is not easy to interact with
the human behavior detected by the sensor, but testing took me more time than
expected because I tried complicated things before I noticed that a tiny
modification in the code would make testing much easier... My second task was
to add patterns to the parser. But when starting with this task I noticed that
the increasing number of patterns would make it easy to forget to include a
pattern into the patterns list, so I first had to create a system of
annotations to mark the patterns. Finally, during this sprint Lenny noticed
that a pattern that I wrote last week was not working, so I had to fix it.
Currently there is no task planned for me next week, we decided that we would
choose on Friday what I will be doing, once we have a clear view of what
remains to do after this sprint.

### Overall team

This week we achieved most of what we planned for this sprint, which was a lot of work. Except Léo
who had troubles with testing, we are starting to be more productive. Next week we can hope to
finish all of our app's core features and start fine-tuning our app's UI and UX, for it to be nice
looking for the final presentation.
