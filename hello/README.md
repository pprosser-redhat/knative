# hello

Little Knative serviing example

to deploy....

quarkus build --native --no-tests -Dquarkus.native.container-build=true -Dquarkus.kubernetes.deploy=true -Dquarkus.native.container-runtime=podman

to create new revision, update pom VERSION to create new image tag

deploy again and update the knative configuration to recognise the 2 revisions
 - need to update the service manually back to version one so the revisions are deployed. Quakus does a totally redeploy



 **** Note, when creating podman machine on mac, need to mount /Users folder like this :-

 podman machine init -v /Users:/Users

 build container 

podman build -f src/main/docker/Dockerfile.native-micro -t quay.io/philprosser/hello:1
podman build -f src/main/docker/Dockerfile.native-micro -t quay.io/philprosser/hello:2


Deploy version 1

kn service create hello --image quay.io/philprosser/hello:1

Update with version 2

kn service update hello --image quay.io/philprosser/hello:2

kn service update hello --image quay.io/philprosser/hello:2 --traffic hello-00001=50,hello-00002=50

kn service update hello --traffic hello-00002=100