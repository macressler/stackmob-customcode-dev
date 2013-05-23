# Stackmob Custom Code Development Environment (Beta)

[![Build Status](https://travis-ci.org/stackmob/stackmob-customcode-dev.png?branch=master)](https://travis-ci.org/stackmob/stackmob-customcode-dev)

The StackMob Custom Code Development Environment provides a server that runs your custom code in a local debugging environment, making it quicker and easier to test and iterate on your code before uploading it to StackMob's infrastructure.

The dev environment contains a complete [Custom Code SDK](https://github.com/stackmob/stackmob-customcode-sdk) implementation as well as a local API server. It runs your custom code locally and proxies all normal API calls to your v0 StackMob API. Normal push calls are not currently supported.

The development enviromment is currently in beta as we work to make its functionality mirror our servers' as closely as possible.

As always with StackMob open source software, we encourage you to [let us know](https://github.com/stackmob/stackmob-customcode-dev/issues) if you find issues, or submit pull requests.

## Setup

### Maven

```xml
<dependency>
  <groupId>com.stackmob</groupId>
  <artifactId>customcode-dev</artifactId>
  <version>0.1.7</version>
</dependency>
```

### SBT

```scala
"com.stackmob" % "customcode-dev" % "0.1.7"
```

## Usage

It takes just a few lines of code to set up and run a server that responds to your custom code's defined endpoints.

Below is complete working code to do so:

###In Java

```java
import com.stackmob.core.jar.JarEntryObject;
import com.stackmob.customcode.dev.server.CustomCodeServer;
import MyJarEntryObject; //this is the JarEntryObject subclass that you've created

public class LocalDevServer {
    public static void main(String[] args) {
        JarEntryObject entryObject = new MyJarEntryObject();
        CustomCodeServer.serve(entryObject, "example-api-key", "example-api-secret", 8080);
    }
}
```

###In Scala

```scala
import com.stackmob.customcode.dev.server.CustomCodeServer;
import MyJarEntryObject; //this is the JarEntryObject subclass that you've created

object LocalDevServer extends App {
  val entryObject = new MyJarEntryObject
  CustomCodeServer.serve(entryObject, "example-api-key", "example-api-secret", 8080)
}
```

## Details

The custom code dev server implements a local HTTP server that parses incoming HTTP requests into `ProcessedAPIRequest` objects, and passes them along to the appropriate `CustomCodeMethod` for that request.

It also includes local implementations of all of `SDKServiceProvider`'s methods. Below are details on how each works:

* `DataService`: All methods translate to calls to version 0 of your StackMob Datastore REST API, using the API key and secret that you provided in the configuration file. In order for all calls to work properly, please ensure that your schemas have private key ACL permissions, or equivalent, set up. The dev server attempts to detect query patterns that may be problematic on StackMob's production servers. For example, if your code makes more than 5 queries in a single request, the custom code method will fail.
* `DatastoreService`: All methods translate to calls to `DataService`, so this object works similarly to `DataService`.
* `PushService`: All methods translate to calls to version 0 of your StackMob Push REST API, using the API key and secret that you provided. Please ensure that you have valid push credentials uploaded to the StackMob server.
* `TwitterService`: All methods are currently stubbed and have no usable functionality.
* `FacebookService`: All methods are currently stubbed and have no usable functionality.
* `isSandbox`: Always returns `true`.
* `getVersion`: always returns `ccDevVersion`.
* `ConfigVarService`: All methods currently return constant values that are computed from `key` and `moduleName`, where applicable.
* `CachingService`: All methods cache locally, in memory. Calls to each method will randomly simulate `TimeoutException`s and `RateLimitedException`s, so make sure your code can handle those cases.
* `HttpService`: All methods perform real HTTP requests to the outside world, and calls to each method will randomly simulate `AccessDeniedException`s and `TimeoutException`s, so make sure your code can handle those cases.
* `LoggerService`: All logs go to the console on which you run your server.

## Bugs and New Features

We track known issues and new feature requests at https://github.com/stackmob/stackmob-customcode-dev/issues/. If you find an issue, please report it at the same place, complete with instructions
on how to reproduce it.

If you have a new feature that you'd like to see in the dev environment, please add it [here](https://github.com/stackmob/stackmob-customcode-dev/issues/new) and label it with "feature-request".
If you'd like to implement the feature yourself, please fork this repository and submit a pull request with your changes. Don't forget to add tests for your new feature!