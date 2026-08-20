
-- nflverse's college_name field can list multiple schools a player attended
-- (e.g. "Wisconsin; Wisconsin-Stevens Point; Mid-State Technical College (J.C.); ..."),
-- which overflows the original VARCHAR(100). Widened to match Hibernate's implicit
-- default length for an unannotated String field, avoiding a validate-mode mismatch.
ALTER TABLE players ALTER COLUMN college TYPE VARCHAR(255);
