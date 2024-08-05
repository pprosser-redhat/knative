# My simple serverless demo.

# To make scale to zero happend faster, add the following to the Knativeserving crd

  config:
    autoscaler:
      scale-to-zero-grace-period: 10s

## Serverless of a simple restful service

Show the code inside the hello folder. This is a simple Quarkus restful service.... nothing to do with serverless

* Having shown the code, lets deploy the application to OpenShift using the kn cli and the image from quay.

```
kn service create hello --scale-metric rps --scale-target 20 --scale-window 10s --image quay.io/philprosser/hello:1
```

* Test out the service using curl

```
curl https://hello-my-serverless-demo.apps.cluster-pb8xm.pb8xm.sandbox1517.opentlc.com/hello
```

* Wait for the service to stop before running curl again (about 30 seconds)

* Deploy a second revision of the service 

```
kn service update hello --image quay.io/philprosser/hello:2 --traffic hello-00001=50,hello-00002=50
```

* Retest the service using the curl above, show see it flip between versions

* Having tested, move to just the 2nd version running

```
kn service update hello --traffic hello-00002=100
```

* Run simple scale test using requests per second, chanage the request per second up and down - 10, 30, 85

```
export RATE=10
curl -L https://goo.gl/S1Dc3R | bash -s $RATE "https://hello-my-serverless-demo.apps.cluster-pb8xm.pb8xm.sandbox1517.opentlc.com/hello"
```

## Serverless demo using Functions 

* Firstly, in VSCode show how to create a function 

```
 kn func create -l quarkus -t cloudevents demofunc
```

Take a look around

* Set the image repo directory 

```
export FUNC_REGISTRY=quay.io/philprosser
```

* Deploy the function from the image, not doing the build as it takes too long

```
kn func deploy --build=false -i quay.io/philprosser/myfunction:1 -v
```

or for speed, using kn service deploy straight from the image

```
kn service create myfunction --scale-window 10s  --image quay.io/philprosser/myfunction:1
```

* Test the function 

Ce-Source is used to determine the function/method to use 
or /function on the URL

```
URL=https://myfunction-my-serverless-demo.apps.cluster-pb8xm.pb8xm.sandbox1517.opentlc.com/ &&
curl -v "$URL" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:function" \
  -H "Ce-Type:phil.camel.event" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"$(whoami)\", \"name\": \"Phil\" }\""
```


If you want to do some scale test with the function and the broker
```
kn service update myfunction --scale-metric rps --scale-target 20
```

## Serverless event driven demo 

* Deploy a Broker using inmemorybroker - note, the instructions are not geared up for this, they are written to use with Kafka

```
kn broker create philsbroker
```

* Deploy Broker with Kafka

Prereqs

Install AMQ Streams and create a Broker cluster



and then setup the knative eventing operator for kafka integration using the KnativeKafka CRD, somethinmg like below

```
kind: KnativeKafka
apiVersion: operator.serverless.openshift.io/v1alpha1
metadata:
  name: knative-kafka
  namespace: knative-eventing
spec:
  broker:
    enabled: true
    defaultConfig:
      numPartitions: 10
      replicationFactor: 3
      bootstrapServers: my-cluster-kafka-bootstrap.my-serverless-demo.svc.cluster.local:9092
  source:
    enabled: true
  sink:
    enabled: true
  channel:
    enabled: true
    bootstrapServers: my-cluster-kafka-bootstrap.my-serverless-demo.svc.cluster.local:9092
```
and then you can deploy the broker below during the demo either via the cli or on topology viewer in the console

**via the cli**

```
kn broker create philsbroker --namespace my-serverless-demo --class Kafka --broker-config cm:kafka-broker-config:knative-eventing
```
**via the topology viewer**
```
apiVersion: eventing.knative.dev/v1
kind: Broker
metadata:
  annotations:
    eventing.knative.dev/broker.class: Kafka 
  name: philsbroker
spec:
  config:
    apiVersion: v1
    kind: ConfigMap
    name: kafka-broker-config 
    namespace: knative-eventing
```
**To allow events to be sent from outside the broker, create a http route for the service**

```
oc -n knative-eventing create route edge knativeevent --service=kafka-broker-ingress
```

**Let's test the eventing**

* If a HTTP route has been created then try this 

```
curl -v "https://knativeevent-knative-eventing.apps.cluster-pb8xm.pb8xm.sandbox1517.opentlc.com/my-serverless-demo/philsbroker" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:curl" \
  -H "Ce-Type:phil.camel.test" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"curled through broker\", \"name\": \"Phil\" }\""
```

* if no http route is available then try this

```
oc rsh deployment/send-messages && \
curl -v "http://kafka-broker-ingress.knative-eventing.svc.cluster.local/my-serverless-demo/philsbroker" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:curl" \
  -H "Ce-Type:phil.camel.test" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"curled through broker\", \"name\": \"Phil\" }\""
```

* Create a trigger to myfunction

```
kn trigger create sourceevents --broker philsbroker --filter type=phil.camel.test --sink ksvc:myfunction
```

**if you want to show the messages in Kafka, then go a terminal window 


```
oc rsh my-cluster-kafka-0
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic knative-broker-my-serverless-demo-philsbroker --from-beginning --property print.headers=true
```



* To generate events, deploy the camel k binding  (preferrred approach for demo)

this is in the folder camel

cd folder camel

```
oc apply -f sendmessages-pipe.yaml
```

* Create another subscription using the topology viewer to receieve output async from function 

 1. Create event sink 
 2. choose log sink
 3. select form view
 4. In yaml view, under source, make it look like 

 ```
   source:
    properties: 
      type: function.output
    ref:
      apiVersion: eventing.knative.dev/v1
      kind: Broker
      name: philsbroker
 ```


