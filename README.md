# Spring Content Envers Example

## Notes
- The project's dependencies include `spring-data-envers`.  This provides a spring data (java only) API on top of [hibernate envers](https://hibernate.org/orm/envers/) feature.
- The File entity is annotated as `@Audited`.  This tells hibernate to create additional tables and record revisions of the entity 
as it is modified over time.
- [GenericRevisionController](https://github.com/paulcwarren/spring-content-with-envers/blob/dfb5702f801cfe99b12d13bb4652e00364dc842a/src/main/java/gettingstarted/GenericRevisionsController.java) is 
an implementation of a generic controller providing rest endpoints on top of the spring-data-envers java API.
- [AuditedSetContentEventHandler](https://github.com/paulcwarren/spring-content-with-envers/blob/dfb5702f801cfe99b12d13bb4652e00364dc842a/src/main/java/gettingstarted/AuditedSetContentEventHandler.java) is 
a spring content event handler bean that resets the content id so that it is regenerated when content is set.  This is important so that 
revision content can be fetched later on.   It is added to the application context [here]((https://github.com/paulcwarren/spring-content-with-envers/blob/dfb5702f801cfe99b12d13bb4652e00364dc842a/src/main/java/gettingstarted/SpringContentApplication.java#L31).

## Running the tests

`mvn clean test`

