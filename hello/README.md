# hello

Little Knative serviing example

to deploy....

quarkus build --native --no-tests -Dquarkus.native.container-build=true -Dquarkus.kubernetes.deploy=true

to create new revision, update pom VERSION to create new image tag

deploy again and update the knative configuration to recognise the 2 revisions