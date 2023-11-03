# My simple serverless demo.

## Serverless of a simple restful service

Show the code inside the hello folder. This is a simple Quarkus restful service.... nothing to do with serverless

* Having shown the code, lets deploy the application to OpenShift using the kn cli and the image from quay.

```
kn service create hello --scale-metric rps --scale-target 20 --image quay.io/philprosser/hello:1
```

* Test out the service using curl

```
curl https://hello-my-serverless-demo.apps.cluster-4kf2q.dynamic.opentlc.com/hello
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
curl -L https://goo.gl/S1Dc3R | bash -s 10 "https://hello-my-serverless-demo.apps.cluster-4kf2q.dynamic.opentlc.com/hello"
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
kn service create myfunction  --image quay.io/philprosser/myfunction:1
```

* Test the function 

Ce-Source is used to determine the function/method to use 
or /function on the URL

```
URL=https://myfunction-my-serverless-demo.apps.cluster-4kf2q.dynamic.opentlc.com/ &&
curl -v "$URL" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:function" \
  -H "Ce-Type:phil.camel.event" \
  -H "Ce-Specversion:1.0" \
  -d "{\"message\": \"$(whoami)\", \"name\": \"Phil\" }\""
```

## Serverless event driven demo 

* Deploy a Broker 

```
kn broker create philsbroker
```

* Create a ping source to send data to the broker

```
kn source ping create sendtobroker --schedule "*/1 * * * *" --data '{"message": "Hello", "name": "ping source" }' --sink broker:philsbroker
```


* Create a trigger to myfunction

```
kn trigger create pingsourceevents --broker philsbroker --filter type=dev.knative.sources.ping --sink ksvc:myfunction
```

* Try sending a curl command to the broker 

```
curl -v "http://broker-ingress.knative-eventing.svc.cluster.local/my-serverless-demo/philsbroker" \
-X POST \
-H "Ce-Id: fromcurl" \
-H "Ce-Specversion: 1.0" \
-H "Ce-Type: dev.knative.sources.ping" \
-H "Ce-Source: mycurl" \
-H "Content-Type: application/json" \
-d '{"message": "Hello", "name": "curl source" }'
```


