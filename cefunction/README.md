# Function project

Build Quarkus native 

quarkus build --native --no-tests -Dquarkus.native.remote-container-build=true

deploy native version

kn func deploy -i quay.io/philprosser/cefunction:v1
kn func invoke

Welcome to your new Quarkus function project!

This sample project contains a single function: `functions.Function.function()`,
the function just returns its argument.

## Local execution
Make sure that `Java 11 SDK` is installed.

To start server locally run `./mvnw quarkus:dev`.
The command starts http server and automatically watches for changes of source code.
If source code changes the change will be propagated to running server. It also opens debugging port `5005`
so debugger can be attached if needed.

To run test locally run `./mvnw test`.

## The `func` CLI

It's recommended to set `FUNC_REGISTRY` environment variable.
```shell script
# replace ~/.bashrc by your shell rc file
# replace docker.io/johndoe with your registry
export FUNC_REGISTRY=quay.io/philprosser
echo "export FUNC_REGISTRY=docker.io/johndoe" >> ~/.bashrc 
```

### Building

This command builds OCI image for the function.

```shell script
func build
```

By default, JVM build is used.
To enable native build set following environment variables to `func.yaml`:
```yaml
buildEnvs:
  - name: BP_NATIVE_IMAGE
    value: "true"
  - name: BP_MAVEN_BUILT_ARTIFACT
    value: func.yaml target/native-sources/*
  - name: BP_MAVEN_BUILD_ARGUMENTS
    value: package -DskipTests=true -Dmaven.javadoc.skip=true -Dquarkus.package.type=native-sources
  - name: BP_NATIVE_IMAGE_BUILD_ARGUMENTS_FILE
    value: native-image.args
  - name: BP_NATIVE_IMAGE_BUILT_ARTIFACT
    value: '*-runner.jar'
```

### Running

This command runs the function locally in a container
using the image created above.
```shell script
func run
```

### Deploying

This commands will build and deploy the function into cluster.

```shell script
func deploy # also triggers build
```

## Function invocation

Do not forget to set `URL` variable to the route of your function.

You get the route by following command.
```shell script
func info
```

### cURL

```shell script
URL=http://localhost:8080/
curl -v ${URL} \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:cloud-event-example" \
  -H "Ce-Type:dev.knative.example" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"$(whoami)\"}\""
```

### HTTPie

```shell script
URL=http://localhost:8080/
http -v ${URL} \
  Content-Type:application/json \
  Ce-Id:1 \
  Ce-Source:cloud-event-example \
  Ce-Type:dev.knative.example \
  Ce-Specversion:1.0 \
  message=$(whoami)
```

URL=http://cefunction-test.apps.coffee.demolab.local/

### cURL

```shell script
URL=http://cefunction-test.apps.coffee.demolab.local/
curl -v "http://cefunction-test.apps.coffee.demolab.local/" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:cloud-event-example" \
  -H "Ce-Type:phil.camel.event" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"$(whoami)\", \"name\": \"Phil\" }\""
```
```
curl -v "http://localhost:8080/" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:cloud-event-example" \
  -H "Ce-Type:phil.camel.event" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"$(whoami)\", \"name\": \"Phil\" }\""
```
  ### Test with ping source 

  ```
apiVersion: sources.knative.dev/v1
kind: PingSource
metadata:
  annotations:
    openshift.io/generated-by: OpenShiftWebConsole
    sources.knative.dev/creator: pprosser
    sources.knative.dev/lastModifier: pprosser
  name: ping-source
  namespace: test
  labels:
    app: ping-source
    app.kubernetes.io/component: ping-source
    app.kubernetes.io/instance: ping-source
    app.kubernetes.io/name: ping-source
spec:
  contentType: application/json
  data: '{"message": "Hello world!"}'
  schedule: '*/2 * * * *'
  sink:
    ref:
      apiVersion: eventing.knative.dev/v1
      kind: Broker
      name: testbroker
  ```

  Add filter to trigger

  type='dev.knative.sources.ping', source='/apis/v1/namespaces/test/pingsources/ping-source

  add filter in the yaml

      attributes:
      source: /apis/v1/namespaces/test/pingsources/ping-source
      type: dev.knative.sources.ping



```
apiVersion: camel.apache.org/v1alpha1
kind: KameletBinding
metadata:
  name: route-phil
spec:
  sink:
    properties:
      type: phil.camel.test
    ref:
      apiVersion: eventing.knative.dev/v1
      kind: Broker
      name: antonbroker
  source:
    properties:
      contentType: application/json
      message: '{"message": "Hello world!"}'
      period: 1
    ref:
      apiVersion: camel.apache.org/v1alpha1
      kind: Kamelet
      name: timer-source
    types: {}
  steps:
    - properties:
        level: INFO
        showBody: 'true'
      ref:
        apiVersion: camel.apache.org/v1alpha1
        kind: Kamelet
        name: log-action
      types: {}

```