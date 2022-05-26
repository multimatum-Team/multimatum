Sprint 12 summary
================

### Florian

### Joseph

### Lenny

### Léo

### Louis

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
