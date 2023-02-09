# Deployment

## Prerequisites

* Ansible
* Access to 1Password "__nameKebab__ vars.private.yml"-entry
* VPN (LDAP)
* ssh-key copied to vm-cw-qa1 (if not: ask the devops team to copy your ssh-key to vm-cw-qa1)

## Settings

see inventories/vars.qa.cw.[master|dev].yml to adapt settings

* `basic_auth_enabled`: Whether to protect deployment via BasicAuth 
  * Username: __nameLower__
  * Password: (see private.vars.yml)
* `swagger_exposed`: Whether to expose the api.json (incl. swagger-ui), protected via BasicAuth

## Deploy Application on vm-cw-qa1

* Copy file of 1Password "__nameKebab__ vars.private.yml"-entry to inventories/vars.private.yml
* Connect to VPN (LDAP)
* Execute `deploy_qa_craftworks.sh`
* Enter the password of craftworks@vm-cw-qa1 when asked for `BECOME password` - https://start.1password.com/open/i?a=GR5AWTIYQJBLROVGEA2B5IFX3E&v=qohfgasyzziobfzk7khvrhljom&i=pfcknx2qf26posni4wzo7uxkem&h=craftworks.1password.com

## First Deployment

* Adapt inventories/vars.private.yml and set private_vars_version number
* Add vars.private.yml to 1Password 
  * Name: "__nameKebab__ vars.private.yml"
  * Type: Secure Note

## Change Deployment

* Adapt files in playbooks/roles/__nameKebab__-delivery and/or in inventories

### Change vars.private.yml

* Copy file of 1Password "__nameKebab__ vars.private.yml"-entry to inventories/vars.private.yml
* Adapt this file and increase private_vars_version number
* Adapt playbooks/roles/assertion-private-vars/tasks/main.yml and adapt private_vars_version
* Update file of 1Password "__nameKebab__ vars.private.yml"-entry