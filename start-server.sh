export PATH=/opt/apache-maven-3.8.6/bin:$PATH
git pull origin master &&
export TELEGRAM_BOT_TOKEN=5304881616:AAHO2sMJry5AZHryTxsrqND0ex3uo8H5gQ4
mvn clean package -DskipTests &&
mvn spring-boot:start -Dspring-boot.run.arguments='--server.port=8085'
