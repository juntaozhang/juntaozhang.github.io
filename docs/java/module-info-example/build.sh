cd lib-a
mvn clean install

cd ../lib-b
mvn clean install

cd ../main
mvn clean install

cd main
rm -rf libs dist my-jre

cp target/*.jar libs/
cp ../lib-a/target/*.jar libs/
cp ../lib-b/target/*.jar libs/

jlink \
  --module-path libs \
  --add-modules main \
  --output my-jre \
  --strip-debug \
  --no-header-files \
  --no-man-pages


jpackage \
  --type app-image \
  --name MyApp \
  --input libs \
  --main-jar main-1.0-SNAPSHOT.jar \
  --main-class org.example.main.App \
  --module-path libs \
  --runtime-image my-jre \
  --java-options "--add-opens lib.b/org.b=ALL-UNNAMED" \
  --dest dist

cd dist/MyApp.app/Contents/MacOS
./MyApp