Sprint 2 summary
================

### Florian
I had for Sprint tast to allow the user of the app to generate QR Code. 
Using the zxing library, the activity was implemented in 3 hours, which was a little longer than expected. 
However, it was more challenging to do the tests of it than I though. I had to find a matcher
allowing me to test if the QR Code was correct. Also, I tried to use Roboelectric which give a bigger
freedom to the tests and thus lead to a better coverage. Unfortunatly, for now, Cirrus CI doesn't
detect the tests done with Roboelectric and thus I had to remove some of them, reducing the coverage.
As Roboelectric is very useful, I will try to find a way so that Cirrus CI detect it.
Adding Roboelectric and the matcher, I had in total a workload of 9 hours. Which is longer than
expected


### Joseph

This week I was assigned to the creation of the deadline class.
The task in itself wasn't too hard to implement but I struggled a lot to make the CI work  properly,
as a result I largely underestimated my workload.

I'm not sure how I could have anticipated this but hopefully such logistical issues won't happen too
often from now on, as we finally got most of the tooling to work.

### Lenny (Scrum master)

### Léo

My goal for this week was to create a camera screen containing a QR-Code reading feature.
The camera screen was not so hard to implementin itself, but the permission managment was quite hard.
I had to learn that but it should be easier now. The QR-Code reader was relatively easy to implement
using a library, but UI testing was very hard, because a did not find a way to simulate a QR-Code
reading in the tests. So testing (and coverage) should be improved for next week.
The time estimation was good, I worked approximately one hour more than expected.

### Louis

I was assigned to make the log in system and the UI with it. 
It figured that i had to configure firebase to use its API.
It takes me a lot more time than expected. The code is finished but not the UI and the test.

For the next sprint i will finish this feature and done another little one. Maybe the possibility to
mark task as done or the notification system.
### Valentin

### Overall team
