# db-data-load
A data loader using [Liquibase](http://www.liquibase.org/)

If you've ever had to setup a database using sql scripts then this might help.

It uses Liquibase to do the heavy lifting and maven repositories to handle storing the sql/changesets.

# How to Use with Docker compose

 1. Package your sql as a liquibase changeset in a simple maven/gradle jar which allows it to be versioned and stored in a repository (eg nexus, Artifactory etc)
    See changeset1 as an example project which does this. It has two changesets.

 2. Add an entry to the compose file for this app which specifies which artifact to grab and the path to the changeset inside the jar. Somethings like:
    See the docker-compose file for a compose system with a db and a db loader which loads the changeset for an exmaple.


How to run the example:
  0) install git maven and docker and docker compose.
  i) Check out the source and build using maven
  ii) cd to the folder with docker-compose (sqlstack)
  iii) run docker-compose up

You should see:
  * The db container start.
  * The db loader loads the changeset ( the changeset could also be downloaded from a repository)
  * The db loader exits once it is finished leaving just the DB populated as required.

Once you get db subsets into versioned artifacts it also allows easier backwards compatablity tests
eg runnning your old application against your current DB to allow easy app rollback.

What kinds of data could you load.

In production you could version your seed data independently of your application. 

For testing you could define useful subsets of data for testing different types of scenarios.
eg. If you have different types of customers you could define a data bundle for them.


      - MAVEN_LOCAL_REPO=$MAVEN_CONFIG/repository 
      - SPRING_DATASOURCE_USERNAME=dbuser
      - SPRING_DATASOURCE_PASSWORD=testdb
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/dbtest 
      - DB_MIGRATIONS=
        [
            {   "changeLogRoot":"gav:au.com.cybernostics.changesets:changeset1",
                "changelogs":[
                    "db/changelog/db.changelog-master.yaml",
                    "db/changelog2/db.changelog-master.yaml"
                    ]
            }
        ]
 


