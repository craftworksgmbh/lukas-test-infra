#!/bin/sh

# Set variables for sed to work
# https://stackoverflow.com/questions/11287564/getting-sed-error-illegal-byte-sequence-in-bash
LANG=C
LC_CTYPE=C

echo ""
echo "This script initialises the newly created project by setting up the namings and components correctly."
echo ""
echo "To do this, you need to know the name of your project."
echo "Please refer to the project naming schema - https://craftworks.atlassian.net/wiki/spaces/DEV/pages/1532723300/Project+naming+scheme".

if ! [ -x "$(command -v awk)" ]; then
    echo ""
    echo '\033[1;31mError: awk is not installed. Install it. E.g. via `sudo apt-get install awk` or `brew install awk`\033[0m' >&2
    exit 1
fi

if ! [ -x "$(command -v sed)" ]; then
    echo ""
    echo '\033[1;31mError: sed is not installed. Install it. E.g. via `sudo apt-get install sed` or `brew install sed`\033[0m' >&2
    exit 1
fi

if ! [ -x "$(command -v perl)" ]; then
    echo ""
    echo '\033[1;31mError: perl is not installed. Install it. E.g. via `sudo apt-get install perl` or `brew install perl`\033[0m' >&2
    exit 1
fi

if ! [ -x "$(command -v rename)" ]; then
    echo ""
    echo '\033[1;31mError: rename is not installed. Install it. E.g. via `sudo apt-get install rename` or `brew install rename`\033[0m' >&2
    exit 1
fi

if [ "$#" -ne 7 ]; then
    echo ""
    read -r -p "Company name in display form (e.g. Microsoft Corporation): " companyDisplay
    read -r -p "Project name in display form (e.g. Data Platform Extended): " projectDisplay
    echo ""
    read -r -p "Company name in short form, kebab-case (e.g. microsoft): " companyKebab
    read -r -p "Project name in short form, kebab-case (e.g. dp-x): " projectKebab

    echo ""
    read -p "Remove backend (incl. database setup) component? (Y/N): " ask_remove_backend

    echo ""
    read -p "Remove frontend component? (Y/N): " ask_remove_frontend

    echo ""
    read -p "Remove python component? (Y/N): " ask_remove_python
else
    companyDisplay=$1
    projectDisplay=$2
    companyKebab=$3
    projectKebab=$4
    ask_remove_backend=$5
    ask_remove_frontend=$6
    ask_remove_python=$7
fi

remove_backend=false
case $ask_remove_backend in
    y|Y|"")
      remove_backend=true
      ;;
    *)
      remove_backend=false
      ;;
esac

remove_frontend=false
case $ask_remove_frontend in
    y|Y|"")
      remove_frontend=true
      ;;
    *)
      remove_frontend=false
      ;;
esac

remove_python=false
case $ask_remove_python in
    y|Y|"")
      remove_python=true
      ;;
    *)
      remove_python=false
      ;;
esac

if [ "$remove_backend" = true ] || [ "$remove_frontend" = true ] || [ "$remove_python" = true ] ; then
  echo ""
  echo "\033[1;31mNote that removing a component is difficult to test. Double check that everything has been removed correctly (If fixes are necessary, incorporate them into the craftworks-template).\033[0m"
fi

remove_deploy_legacy=true

# Just in case if the user entered something wrong, all lowercase, white-spaces as dashes
companyKebab=$(echo "$companyKebab" | awk '{print tolower($0)}' | sed 's/[[:space:]]/-/g')
projectKebab=$(echo "$projectKebab" | awk '{print tolower($0)}' | sed 's/[[:space:]]/-/g')

nameDisplay="${companyDisplay} ${projectDisplay}"
nameKebab="${companyKebab}-${projectKebab}"

# Ask for double-check
echo ""
echo "\033[1;32m$nameDisplay ($nameKebab)\033[0m"
echo "\033[1;34mRemove frontend component: $remove_frontend\033[0m"
echo "\033[1;34mRemove backend component: $remove_backend\033[0m"
echo "\033[1;34mRemove python component: $remove_python\033[0m"
echo ""
echo "Note the following two values, they will be needed during the set-up process"
echo "\033[1;33m<name-display>: \"$nameDisplay\"\033[0m"
echo "\033[1;33m<name-short>: \"$nameKebab\"\033[0m"

echo ""
read -p "Init project with this name and settings? (Y/N): " ask_do_it
case $ask_do_it in
    y|Y|"")
        echo ""
        # Supported markers
        # `===marker:start:backend===`[...]`===marker:end:backend===`
        # `===marker:inline:start:backend===`[...]`===marker:inline:end:backend===`
        # `===marker:start:frontend===`[...]`===marker:end:frontend===`
        # `===marker:inline:start:frontend===`[...]`===marker:inline:end:frontend===`
        # `===marker:start:python===`[...]`===marker:end:python===`
        # `===marker:inline:start:python===`[...]`===marker:inline:end:python===`

        # Supported placeholder
        # https://craftworks.atlassian.net/wiki/spaces/DEV/pages/1532723300/Project+Naming+Schema
        # https://betterprogramming.pub/string-case-styles-camel-pascal-snake-and-kebab-case-981407998841
        # Examples are based on the name "Microsoft Corporation Data Platform Extended (microsoft-dp-x)"
        # `__nameDisplay__` - e.g.: "Microsoft Corporation Data Platform Extended"
        # `__nameKebab__` - e.g.: "microsoft-dp-x"
        # `__namePascal__` - e.g.: "MicrosoftDpX"
        # `__nameLower__` - e.g.: "microsoftdpx"
        # `__nameSnake__` - e.g.: "microsoft_data_platform_x"

        # `__companyDisplay__` - e.g.: "Microsoft Corporation"
        # `__companyKebab__` - e.g.: "microsoft"
        # `__companyPascal__` - e.g.: "Microsoft"
        # `__companyLower__` - e.g.: "microsoft"
        # `__companySnake__` - e.g.: "microsoft"

        # `__projectDisplay__` - e.g.: "Data Platform Extended"
        # `__projectKebab__` - e.g.: "dp-x"
        # `__projectPascal__` - e.g.: "DpX"
        # `__projectLower__` - e.g.: "dpx"
        # `__projectSnake__` - e.g.: "dp_x"

        # `__randomTimeAtNight__` - a generated random time at night in the format `hh:mm`, e.g. "02:15"

        # abc-def-ghi --> AbcDefGhi
        companyPascal=$(echo "$companyKebab" | awk 'BEGIN{FS="";RS="-";ORS=""} {$0=toupper(substr($0,1,1)) substr($0,2)} 1')
        projectPascal=$(echo "$projectKebab" | awk 'BEGIN{FS="";RS="-";ORS=""} {$0=toupper(substr($0,1,1)) substr($0,2)} 1')
        namePascal="${companyPascal}${projectPascal}"

        # abc-def-ghi --> abcdefghi
        companyLower=$(echo "$companyKebab" | sed 's/-//g')
        projectLower=$(echo "$projectKebab" | sed 's/-//g')
        nameLower="${companyLower}${projectLower}"

        # abc-def-ghi --> abc_def_ghi
        companySnake=$(echo "$companyKebab" | sed 's/-/_/g')
        projectSnake=$(echo "$projectKebab" | sed 's/-/_/g')
        nameSnake="${companySnake}_${projectSnake}"

        # Remove legacy deployment via ansible
        if [ "$remove_deploy_legacy" = true ] ; then
            echo "Removing legacy deployment via ansible..."
            rm -r 'deploy/legacy'
        fi

        # Remove backend component
        if [ "$remove_backend" = true ] ; then
            echo "Removing backend component..."
            rm -r '__nameKebab__-backend'
            rm -r 'deploy/templates/backend.yaml'
            rm -r 'deploy/templates/backend-app-props.yaml'
            rm -r 'deploy/templates/db.yaml'
            rm -r 'deploy/templates/db-config.yaml'
            find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:start:backend===/,/===marker:end:backend===/d' {} \;
            find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/(===marker:inline:start:backend===).*(===marker:inline:end:backend===)//g' {} \;
        fi

        # Remove frontend component
        if [ "$remove_frontend" = true ] ; then
            echo "Removing frontend component..."
            rm -r '__nameKebab__-frontend'
            rm -r 'deploy/templates/frontend.yaml'
            find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:start:frontend===/,/===marker:end:frontend===/d' {} \;
            find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/(===marker:inline:start:frontend===).*(===marker:inline:end:frontend===)//g' {} \;
        fi

        # Remove python component
        if [ "$remove_python" = true ] ; then
            echo "Removing python component..."
            rm -r '__nameKebab__-python'
            find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:start:python===/,/===marker:end:python===/d' {} \;
            find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/(===marker:inline:start:python===).*(===marker:inline:end:python===)//g' {} \;
        fi

        echo "Removing markers..."
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:start:backend===/d' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:end:backend===/d' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:start:frontend===/d' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:end:frontend===/d' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:start:python===/d' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec sed -i.bak '/===marker:end:python===/d' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/===marker:inline:start:backend===//g' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/===marker:inline:end:backend===//g' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/===marker:inline:start:frontend===//g' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/===marker:inline:end:frontend===//g' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/===marker:inline:start:python===//g' {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe 's/===marker:inline:end:python===//g' {} \;

        echo "Rename all files and their contents..."

        # Replace company in files
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__companyDisplay__/$companyDisplay/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__companyKebab__/$companyKebab/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__companyPascal__/$companyPascal/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__companyLower__/$companyLower/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__companySnake__/$companySnake/g" {} \;

        # Replace company in directories and filenames
        find . -type d -name '*__companyDisplay__*' -prune -exec rename  "s/__companyDisplay__/$companyDisplay/g" {} \;
        find . -type f -name '*__companyDisplay__*' -prune -exec rename  "s/__companyDisplay__/$companyDisplay/g" {} \;
        find . -type d -name '*__companyKebab__*' -prune -exec rename  "s/__companyKebab__/$companyKebab/g" {} \;
        find . -type f -name '*__companyKebab__*' -prune -exec rename  "s/__companyKebab__/$companyKebab/g" {} \;
        find . -type d -name '*__companyPascal__*' -prune -exec rename  "s/__companyPascal__/$companyPascal/g" {} \;
        find . -type f -name '*__companyPascal__*' -prune -exec rename  "s/__companyPascal__/$companyPascal/g" {} \;
        find . -type d -name '*__companyLower__*' -prune -exec rename  "s/__companyLower__/$companyLower/g" {} \;
        find . -type f -name '*__companyLower__*' -prune -exec rename  "s/__companyLower__/$companyLower/g" {} \;
        find . -type d -name '*__companySnake__*' -prune -exec rename  "s/__companySnake__/$companySnake/g" {} \;
        find . -type f -name '*__companySnake__*' -prune -exec rename  "s/__companySnake__/$companySnake/g" {} \;

        # Replace project in files
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__projectDisplay__/$projectDisplay/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__projectKebab__/$projectKebab/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__projectPascal__/$projectPascal/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__projectLower__/$projectLower/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__projectSnake__/$projectSnake/g" {} \;

        # Replace project in directories and filenames
        find . -type d -name '*__projectDisplay__*' -prune -exec rename  "s/__projectDisplay__/$projectDisplay/g" {} \;
        find . -type f -name '*__projectDisplay__*' -prune -exec rename  "s/__projectDisplay__/$projectDisplay/g" {} \;
        find . -type d -name '*__projectKebab__*' -prune -exec rename  "s/__projectKebab__/$projectKebab/g" {} \;
        find . -type f -name '*__projectKebab__*' -prune -exec rename  "s/__projectKebab__/$projectKebab/g" {} \;
        find . -type d -name '*__projectPascal__*' -prune -exec rename  "s/__projectPascal__/$projectPascal/g" {} \;
        find . -type f -name '*__projectPascal__*' -prune -exec rename  "s/__projectPascal__/$projectPascal/g" {} \;
        find . -type d -name '*__projectLower__*' -prune -exec rename  "s/__projectLower__/$projectLower/g" {} \;
        find . -type f -name '*__projectLower__*' -prune -exec rename  "s/__projectLower__/$projectLower/g" {} \;
        find . -type d -name '*__projectSnake__*' -prune -exec rename  "s/__projectSnake__/$projectSnake/g" {} \;
        find . -type f -name '*__projectSnake__*' -prune -exec rename  "s/__projectSnake__/$projectSnake/g" {} \;

        # Replace name in files
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__nameDisplay__/$nameDisplay/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__nameKebab__/$nameKebab/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__namePascal__/$namePascal/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__nameLower__/$nameLower/g" {} \;
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' -exec perl -i -pe "s/\__nameSnake__/$nameSnake/g" {} \;

        # Replace name in directories and filenames
        find . -type d -name '*__nameDisplay__*' -prune -exec rename  "s/__nameDisplay__/$nameDisplay/g" {} \;
        find . -type f -name '*__nameDisplay__*' -prune -exec rename  "s/__nameDisplay__/$nameDisplay/g" {} \;
        find . -type d -name '*__nameKebab__*' -prune -exec rename  "s/__nameKebab__/$nameKebab/g" {} \;
        find . -type f -name '*__nameKebab__*' -prune -exec rename  "s/__nameKebab__/$nameKebab/g" {} \;
        find . -type d -name '*__namePascal__*' -prune -exec rename  "s/__namePascal__/$namePascal/g" {} \;
        find . -type f -name '*__namePascal__*' -prune -exec rename  "s/__namePascal__/$namePascal/g" {} \;
        find . -type d -name '*__nameLower__*' -prune -exec rename  "s/__nameLower__/$nameLower/g" {} \;
        find . -type f -name '*__nameLower__*' -prune -exec rename  "s/__nameLower__/$nameLower/g" {} \;
        find . -type d -name '*__nameSnake__*' -prune -exec rename  "s/__nameSnake__/$nameSnake/g" {} \;
        find . -type f -name '*__nameSnake__*' -prune -exec rename  "s/__nameSnake__/$nameSnake/g" {} \;
        
        # Replace randomTimeAtNight in files
        find . -type f ! -path '*/.git/*' ! -name 'init.sh' | xargs grep '__randomTimeAtNight__' | while read -r line ; do
            file=${line%%:*}
            
            startHourFirstDay=22
            endHourSecondDay=6
            randomTimeAtNightHour=$((((RANDOM % ((24-startHourFirstDay) + endHourSecondDay)) + startHourFirstDay) % 24))
            randomTimeAtNightMinute=$((RANDOM % 60))
            randomTimeAtNight=$(printf "%.2d:%.2d" "$randomTimeAtNightHour" "$randomTimeAtNightMinute")
            
            awk -v src="__randomTimeAtNight__" -v repl="$randomTimeAtNight" '!x{x=sub(src,repl)}7' $file > tmp  && mv tmp $file
        done

        # Replace randomTimeAtNight in directories and filenames
        # -- Not supported --
        
        # Remove all bak files
        find . -name "*.bak" -type f -delete
        
        # Remove all ".!" Ìfiles
        find . -name ".\!*" -type f -delete

        # Remove legacy deployment via ansible
        if [ "$remove_deploy_legacy" = false ] ; then
            echo "Renaming vars.private.template.yml to vars.private.yml..."
            mv deploy/legacy/inventories/vars.private.template.yml deploy/legacy/inventories/vars.private.yml
        fi
        
        echo "Staging files to git..."
        git add .
        
        echo ""
        echo "\033[1;32mDone\033[0m"
        
        echo ""
        read -p "Remove init.sh? (Y/N): " removeinitsh
        case $removeinitsh in
            y|Y|"")
                rm init.sh
            ;;
            *)
            ;;
        esac
    ;;
    *)
        echo ""
        echo "\033[1;31mQuiting...\033[0m"
    ;;
esac
