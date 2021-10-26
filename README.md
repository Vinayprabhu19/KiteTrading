# KiteTrading

Kite install local maven repo


heroku plugins:install java
heroku deploy:jar target/my-app.jar --app vpkite 

mvn install:install-file    -Dfile=C:\Users\I353309\Documents\workspace-spring-tool-suite-4-4.7.1.RELEASE\KiteTrading\libs\kiteconnect.jar    -DgroupId=com.zerodha    -DartifactId=kiteconnect    -Dversion=3.1.14    -Dpackaging=jar    -DgeneratePom=true
