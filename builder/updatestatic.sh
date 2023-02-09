# per component config
backend=lukas-test-backend
frontend=lukas-test-frontend

cp ../pom.xml pom.xml

# backend
mkdir -p $backend
cp "../$backend/pom.xml" "$backend/"

# frontend
mkdir -p $frontend
cp "../$frontend/pom.xml" "$frontend/"
cp "../$frontend/package.json" "$frontend/"
cp "../$frontend/package-lock.json" "$frontend/"


# Set version 1.0.0
mvn versions:set -DnewVersion="1.0.0"  -DoldVersion=*
mvn validate -P version-set

# Remove artifacts
rm -f pom.xml.versionsBackup
rm -f "$backend/pom.xml.versionsBackup"
rm -f "$frontend/pom.xml.versionsBackup"


