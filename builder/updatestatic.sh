# per component config
# ===marker:start:backend===
backend=__nameKebab__-backend
# ===marker:end:backend===
# ===marker:start:frontend===
frontend=__nameKebab__-frontend
# ===marker:end:frontend===
# ===marker:start:python===
python=__nameKebab__-python
# ===marker:end:python===

cp ../pom.xml pom.xml

# ===marker:start:backend===
# backend
mkdir -p $backend
cp "../$backend/pom.xml" "$backend/"
# ===marker:end:backend===

# ===marker:start:frontend===
# frontend
mkdir -p $frontend
cp "../$frontend/pom.xml" "$frontend/"
cp "../$frontend/package.json" "$frontend/"
cp "../$frontend/package-lock.json" "$frontend/"
# ===marker:end:frontend===

# ===marker:start:python===
# python
mkdir -p $python/src
cp "../$python/pom.xml" "$python/"
cp "../$python/src/requirements.txt" $python/src/requirements.txt
# ===marker:end:python===

# Set version 1.0.0
mvn versions:set -DnewVersion="1.0.0"  -DoldVersion=*
mvn validate -P version-set

# Remove artifacts
rm -f pom.xml.versionsBackup
# ===marker:start:backend===
rm -f "$backend/pom.xml.versionsBackup"
# ===marker:end:backend===
# ===marker:start:frontend===
rm -f "$frontend/pom.xml.versionsBackup"
# ===marker:end:frontend===
# ===marker:start:python===
rm -f "$python/pom.xml.versionsBackup"
# ===marker:end:python===


