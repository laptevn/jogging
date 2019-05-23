#Write a REST API that tracks jogging times of users
1. API Users must be able to create an account and log in.
2. All API calls must be authenticated.
3. Implement at least three roles with different permission levels: a regular user would only be able to CRUD on their owned records, a user manager would be able to CRUD only users, and an admin would be able to CRUD all records and users.
4. Each time entry when entered has a date, distance, time, and location.
5. Based on the provided date and location, API should connect to a weather API provider and get the weather conditions for the run, and store that with each run.
6. The API must create a report on average speed & distance per week.
7. The API must be able to return data in the JSON format.
8. The API should provide filter capabilities for all endpoints that return a list of elements, as well should be able to support pagination.
9. The API filtering should allow using parenthesis for defining operations precedence and use any combination of the available fields. The supported operations should at least include or, and, eq (equals), ne (not equals), gt (greater than), lt (lower than).
Example -> (date eq '2016-05-01') AND ((distance gt 20) OR (distance lt 10)).
10. Write unit tests.