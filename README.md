# Stackmob Custom Code Local Runner

The StackMob Custom Code Local Runner provides a server to run your custom code in a local debugging environment,
so you can test before uploading to the StackMob servers. We attempt to check your code for possible problems
in the local runner, so that you can fix them before you upload them to StackMob's servers.

## Setup

### Maven:

```xml
<dependency>
  <groupId>com.stackmob</groupId>
  <artifactId>customcode-localrunner</artifactId>
  <version>0.1.0</version>
</dependency>
```

### SBT:

```scala
"com.stackmob" % "customcode-localrunner" % "0.1.0"
```

## Use

Use the CustomCodeMethodServer class to run a server with your custom code endpoints in it.

###In Java

```java
JarEntryObject jeo = new YourJarEntryObjectSubclass();
CustomCodeServer server = new CustomCodeServer(jeo);
server.serve();
```


###In Scala

```scala
val jeo = new YourJarEntryObjectSubclass()
val server = new CustomCodeMethodServer(jeo)
server.serve
```


## Details

The custom code localrunner implements a local HTTP server that parses incoming HTTP requests into `ProcessedAPIRequest`
objects, and passes them along to the appropriate `CustomCodeMethod` for that request. It also includes local
implementations of all of `SDKServiceProvider`'s methods, including the objects that it returns.
Below are details on how each works:

* `DataService`: All methods translate to calls to version 0 of your StackMob Datastore REST API, using the API key and secret that you
provided in the configuration file. In order for all calls to work properly, please ensure that your schemas have private key
ACL permissions set up. The local runner attempts to detect query patterns that will be problematic on StackMob's production servers.
For example, if your code makes more than 5 queries in a single request, the custom code method will fail.
* `DatastoreService`: All methods translate to calls to `DataService`, so this object works similarly to `DataService`.
* `PushService`: All methods translate to calls to version 0 of your StackMob Push REST API, using the API key and secret that you
provided in the configuration file. Please ensure that you have valid push credentials uploaded to the StackMob server.
* `TwitterService`: All methods are currently stubbed and have no usable functionality.
* `FacebookService`: All methods are currently stubbed and have no usable functionality.
* `isSandbox`: Always returns `true`.
* `getVersion`: always returns `localRunnerVersion`.
* `ConfigVarService`: All methods currently return constant values that are computed from `key` and `moduleName`, where applicable.
* `CachingService`: All methods cache locally, in memory. Calls to each method will simulate `TimeoutException`s and `RateLimitedException`s, so
make sure your code can handle those cases.
* `HttpService`: All methods perform real HTTP requests to the outside world, and calls to each method will simulate `AccessDeniedException`s and `TimeoutException`s,
so make sure your code can handle those cases.
* `LoggerService`: All logs go to the console on which you run your server.


## TODOs / Future Features

See https://github.com/stackmob/stackmob-customcode-localrunner/issues/