# Function project

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
export FUNC_REGISTRY=docker.io/johndoe
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

### Note to self

If you don't put quay.io creds into OpenShift then you have to make the repo public on quay.io

### Deploying

This commands will build and deploy the function into cluster.

```shell script
func deploy # also triggers build
```

```
kn func deploy -i quay.io/philprosser/myfunction:1 -v
```
```
kn func deploy --build=false -i quay.io/philprosser/myfunction:1 -v
```

###

For Quarkus function, need to add OCP container registry to props file

quarkus.container-image.registry=image-registry.openshift-image-registry.svc:5000

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

### Added for Phils demo

URL=http://cefunction-test.apps.coffee.demolab.local/

### cURL

```shell script
URL=https://myfunction-my-serverless-demo.apps.cluster-4kf2q.dynamic.opentlc.com/
curl -v "https://myfunction-my-serverless-demo.apps.cluster-4kf2q.dynamic.opentlc.com/" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:function" \
  -H "Ce-Type:phil.camel.event" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"$(whoami)\", \"name\": \"Phil\" }\""
```
```
curl -v "https://myfunction-my-serverless-demo.apps.cluster-4kf2q.dynamic.opentlc.com/" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:function" \
  -H "Ce-Type:phil.camel.event" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"$(whoami)\", \"name\": \"Phil P\" }\""
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
  data: '{"message": "Hello", "name": "Phil P" }'
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


### Log sink
``````
apiVersion: camel.apache.org/v1alpha1
kind: KameletBinding
metadata:
  name: kamelet-log-sink
  namespace: my-serverless-demo
  labels:
    app: kamelet-log-sink
    app.kubernetes.io/component: kamelet-log-sink
    app.kubernetes.io/instance: kamelet-log-sink
    app.kubernetes.io/name: kamelet-log-sink
spec:
  sink:
    ref:
      apiVersion: camel.apache.org/v1alpha1
      kind: Kamelet
      name: log-sink
  source:
    properties:
      type: function.output
    ref:
      apiVersion: eventing.knative.dev/v1
      kind: Broker
      name: philsbroker
```

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
      name: philsbroker
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
    - to:
        uri: kamelet:log-action
        id: to-ae0e
        parameters:
          showBody: true

```