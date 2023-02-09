if [ -z $1 ]
then
    echo "no PR argument given - no deployment will be created"
else
    # create folder for pr
    mkdir -p "prs/pr-$1"
    # Copy files to new deployment
    cp .env Makefile set-version.sh "prs/pr-$1/"
    cp prs/docker-compose.yml "prs/pr-$1/"
    cp -R conf "prs/pr-$1/"
    
    # copy DB to new deployment
    # TODO this does not really work like that because of permissions => disable until someone
    # figured it out how to do it right
    # cp -R -n storage "prs/pr-$1/"
    
    # set domain prefix in new deployment
    replaceString="pr-$1-"
        sed -i "s/\(PR_PREFIX=\).*\$/\1${replaceString}/" "prs/pr-$1/.env"
    # set image version in new deployment
    replaceString="CRAFTWORKS-PR$1"
        sed -i "s/\(IMAGE_VERSION=\).*\$/\1${replaceString}/" "prs/pr-$1/.env"
    echo "pr-$1 deployment created/updated"
fi