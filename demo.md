# My simple serverless demo.

## Server less of a simple restful service

Show the code inside the hello folder. This is a simple Quarkus restful service.... nothing to do with serverless

#. Having shown the code, lets deploy the application to OpenShift using the kn cli and the image from quay.

```
kn service create hello --scale-metric rps --scale-target 20 --image quay.io/philprosser/hello:1
```

2. Test out the service using curl

```
curl https://hello-my-serverless-demo.apps.cluster-4kf2q.dynamic.opentlc.com/hello
```

3. Wait for the service to stop before running curl again (about 30 seconds)

4. Deploy a second revision of the service 




