Sprint 3 summary
================

### Florian
This week, I have developped a Adapter for the deadline allowing to make a customised
list for the deadlines. With it, we can see the title, the date, and if it is done,
already due or the remaining time for the deadline. If the remaining time is small,
it will be displayed in red or orange. I wanted to add a button to add deadlines
and a detailed view of a deadline when you click on it but I was really too short
on time and I will do it next sprint.

### Joseph

This week I was assigned to the implementation of the remote storage of the
list of deadlines.
Since the login system wasn't finished yet, I had to store the data globally,
which we will resolve next week to integrate Louis's work.
I almost managed to combine my code to Florian's work on the UI side,
but ended up short on time.
Lastly, I had some trouble understanding the asynchronous API of Firebase,
which from what I understand is untestable on the Java runtime, since
`Task`s are run on the Android runtime.
I switched back to a synchronous interface to get something merged.

### Lenny

I did some functions to set notifications. I didn't get time to make any test, implementing features that could be tested already took me 8hours. Next week I'll implement those tests.

### Léo

### Louis

I finished the login and it's testing, i struggle with the CI. I didn't find a way to test result 
coming from google sign in, I assume this is not possible with our current framework. 
Next sprint i'll work on log-out system, profile page and sorted deadline as done and undone.

### Valentin
My task for this week was to set up dependency injection in the app and to use it to improve the tests of MainSettingsActivity. So I read documentation and imported Hilt in the project. I tried to do mocking with Mockk, but despite many attempts I did not manage to make it work, so I used Mockito. For this part my time estimate was accurate. After talking with the rest of the group, we decided that I would make a guide on how to use DI tests, so that my teammates hopefully won't have to read the whole documentation when they will use the DI frameworks that I imported. My time estimate for this part (writing the guide) was very bad, which can be partially explained by the fact that I have very little experience in writing complete Markdown documents.

### Overall team
