# Spring Content Envers Example

## Running the tests

`mvn clean test`

## Notes
- The dependencies include `spring-data-envers`
- The File entity is annotated as `@Audited`

This tells hibernate to create additional tables and record revisions of the entity as it is modified over time

-  
