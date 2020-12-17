# Spring Content Envers Example

## Notes
- The project's dependencies include [spring-data-envers](https://github.com/paulcwarren/spring-content-with-envers/blob/c8a5a1d43c8fc43f39d6d0fbd50a8f8642a0ad08/pom.xml#L52).  This provides a spring data (java only) API on top of [hibernate envers](https://hibernate.org/orm/envers/) feature.
- The [FileRepository](https://github.com/paulcwarren/spring-content-with-envers/blob/d7b9ddfc92a8651ae9c31e6f92df852fe4cdb576/src/main/java/gettingstarted/FileRepository.java#L8) extends
RevisionRepository.
- The [File](https://github.com/paulcwarren/spring-content-with-envers/blob/c388ff768a9524881d6d85a2e54dce3d79031ecd/src/main/java/gettingstarted/File.java) entity is annotated as `@Audited`.  This tells hibernate to create additional tables and record revisions of the entity 
as it is modified over time.
- [GenericRevisionController](https://github.com/paulcwarren/spring-content-with-envers/blob/dfb5702f801cfe99b12d13bb4652e00364dc842a/src/main/java/gettingstarted/GenericRevisionsController.java) is 
an implementation of a generic controller providing rest endpoints on top of the spring-data-envers java API.
- [AuditedSetContentEventHandler](https://github.com/paulcwarren/spring-content-with-envers/blob/dfb5702f801cfe99b12d13bb4652e00364dc842a/src/main/java/gettingstarted/AuditedSetContentEventHandler.java) is 
a spring content event handler bean that resets the content id so that it is regenerated when content is set.  This is important so that 
revision content can be fetched later on.   It is added to the application context [here](https://github.com/paulcwarren/spring-content-with-envers/blob/dfb5702f801cfe99b12d13bb4652e00364dc842a/src/main/java/gettingstarted/SpringContentApplication.java#L31).

## Running the tests

`mvn clean test`

