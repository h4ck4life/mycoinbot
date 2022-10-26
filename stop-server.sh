export PATH=/opt/apache-maven-3.8.6/bin:$PATH
mvn spring-boot:stop
kill -9 `lsof -t -c java`
