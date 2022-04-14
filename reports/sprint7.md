Sprint <n> summary
================

### Florian
During this week, I have worked on linking DeadlineDetailsActivity to the viewModel to allow the modifications
made in this activity to really change the data of the deadline in firebase. With the view model, I have also changed
the way to send the data to this activity by simply sending the id of the deadline instead of all the data of it, 
by recuperating the data via the viewModel. However, I have realised that it was possible tha the viewModel in the activity
hadn't the time to fetch the deadlines, thus leading to nullPointerException and crashing the app.
I have tried many thing to solve this and read a lot of documentation before finding a solution and made my PR.
With this and a little work on trying to stabilise the CI, without success on my tries and thus leading to no commit for this, 
I have largely underestimated my time and I wasn't able to make one of the issue assigned to me. 
I will do it next time and I also try to refactor all the tests to work with robolectric, hoping to increase more
the stability of the CI.

### Joseph

### Lenny

### Léo

### Louis

### Valentin

### Overall team