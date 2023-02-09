#!/bin/bash

# add --check --diff to see the changes

# deploy dev
ansible-playbook playbooks/deploy.yml -i inventories/hosts.yml -e @inventories/vars.yml -e @inventories/vars.qa.cw.dev.yml -e @inventories/vars.private.yml --ask-become-pass --user craftworks

# deploy master
ansible-playbook playbooks/deploy.yml -i inventories/hosts.yml -e @inventories/vars.yml -e @inventories/vars.qa.cw.master.yml -e @inventories/vars.private.yml --ask-become-pass --user craftworks
