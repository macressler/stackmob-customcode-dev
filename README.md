# Stackmob Custom Code Local Runner

The StackMob Custom Code Local Runner allows you to run your StackMob custom code locally for simple testing purposes.

## Setup

Add a dependency in your code:

<dependency>
  <groupId>com.stackmob</groupId>
  <artifactId>customcode-localrunner</artifactId>
  <version>0.1.0</version>
</dependency>

## Use

Use the CustomCodeMethodServer class to create a REST API out of your custom code. Here's how:

###In Java
    JarEntryObject jeo = new YourJarEntryObjectSubclass();
    CustomCodeServer server = new CustomCodeServer(jeo);
    server.serve();


###In Scala
    val jeo = new YourJarEntryObjectSubclass()
    val server = new CustomCodeMethodServer(jeo)
    server.serve


## Details

(TODO: configuration file details)

The custom code localrunner implements a local HTTP server that parses incoming HTTP requests into `ProcessedAPIRequest`
objects, and passes them along to the appropriate `CustomCodeMethod` for that request. It also includes local
implementations of all of `SDKServiceProvider`'s methods, including the objects that it returns.
Below are details on how each works:

* `DataService`: All methods translate to calls to version 0 of your StackMob Datastore REST API, using the API key and secret that you
provided in the configuration file. In order for all calls to work properly, please ensure that your schemas have private key
ACL permissions set up. The local runner attempts to detect query patterns that will be problematic on StackMob's production servers.
For example, if your code makes more than 5 queries in a single request, your method will fail.
* `DatastoreService`: All methods translate to calls to `DataService`, so this object works similarly to `DataService`.
* `PushService`: All methods translate to calls to version 0 of your StackMob Push REST API, using the API key and secret that you
provided in the configuration file. Please ensure that you have valid push credentials uploaded to the StackMob server.
* `TwitterService`: All methods are currently stubbed and have no usable functionality.
* `FacebookService`: All methods are currently stubbed and have no usable functionality.
* `isSandbox`: Always returns `true`.
* `getVersion`: always returns `localRunnerVersion`.
* `ConfigVarService`: All methods currently return constant values that are computed from `key` and `moduleName`, where applicable.
* `CachingService`: All methods cache locally, in memory. Calls to each method will simulate `TimeoutException`s and `RateLimitedException`s, so
make sure your code can handle those cases. TODO: make a way to disable error simulations.
* `HttpService`: All methods perform real HTTP requests to the outside world, and calls to each method will simulate `AccessDeniedException`s and `TimeoutException`s,
so make sure your code can handle those cases. TODO: make a way to disable this error simulations.
* `LoggerService`: All logs go to the console on which you run your server. TODO: log to a file
