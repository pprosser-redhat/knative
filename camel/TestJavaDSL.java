// camel-k: language=java trait=knative.auto=true

import org.apache.camel.builder.RouteBuilder;

public class TestJavaDSL extends RouteBuilder {

    private static int counter;

    public static int getCounter() {
        return counter;
    }

    public static int incrementCounter() {
        TestJavaDSL.counter = TestJavaDSL.counter + 1;
        System.out.println ("Count is " + TestJavaDSL.getCounter());
        return TestJavaDSL.counter;
    }

    @Override
    public void configure() throws Exception {

        // Write your routes here, for example:
        from("timer:java?period={{time:1000}}")
            .routeId("send")
            .setHeader("Content-Type").simple("application/json")
            .process( e -> {
                e.getMessage().setHeader("count", TestJavaDSL.incrementCounter());
            })
            .setBody()
                .simple("{\"message\": \"curled through broker\", \"name\": \"Phil ${header.count}\" }\"")
            
            .log("${body}")
            .to("knative:event/phil.camel.test?cloudEventsType=org.apache.camel.event&name=philsbroker");
    }
}
