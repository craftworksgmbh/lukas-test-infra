# Deployment

Deploy application to kubernetes cluster

### Prerequisites

- kubectl

## Deploy application to k8s1

### Prerequisites

- Namespace `pr-lukas-test` created by infra team
- Developer access to k8s1 cluster - See [how-to](https://craftworks.atlassian.net/wiki/spaces/INFRA/pages/5151522928)

### Continues Deployment

`dev`, `master` and PRs* are deployed to the internal kubernetes cluster via the Jenkins Pipeline and ArgoCD automatically.
In the ArgoCD Dashboard each Github Repo gets one "Project" and each deployment (named after the respective branch name) are "Applications" within that project.

A PR can be deployed via the Jenkins parameter `DEPLOY_PR` or by assigning the label `deploy` to the PR. 

Via `kubectl` application resources can be checked by executing:
```
# resource names are prefixed by their branch name
kubectl get <resource_name> -n pr-lukas-test
```

Their status can be accessed via ArgoCD (see [how-to](https://craftworks.atlassian.net/wiki/spaces/INFRA/pages/5151522928) for access to dashboard).

### Secrets

* Secrets have to be created manually via `kubectl` once per Kubernetes namespace (= per GitHub repo)
* Secrets are shared between all (master, dev, PRs) deployments

```
# Via literal
kubectl create secret generic <name> -n pr-lukas-test --from-literal=<key>=<value>
 
# Via file
# https://kubernetes.io/docs/tasks/configmap-secret/managing-secret-using-config-file/
kubectl create secret generic <name> -n pr-lukas-test --from-file=<path/to/file>
```

To check which secrets are used by deployments, run:
```
kubectl get deployments -n pr-lukas-test -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.template.spec.containers[*].envFrom[*]}{"\n"}{end}' | grep secretRef
```

See [Secret Docs](https://kubernetes.io/docs/concepts/configuration/secret/) and [kubectl create secret generic command](https://jamesdefabia.github.io/docs/user-guide/kubectl/kubectl_create_secret_generic/) for more information.