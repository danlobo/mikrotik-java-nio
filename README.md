# mikrotik-java-nio

A Java client for Mikrotik API based on Apache MINA (NIO sockets)

## Current version: 0.1a

This version is the first public available library, so it most likely has bugs.

### Dependencies

This library depends on [Apache MINA](http://http://mina.apache.org/). Tested version is 2.0.9.

## Installation

The easy way to start using is:

1. Download the latest version of [library](https://github.com/danlobo/mikrotik-java-nio/releases/) and put in your lib folder
2. Download the [Apache MINA](http://http://mina.apache.org/) library and put in your lib folder
3. Use it

## Using

Here are some examples to use the library.

Connecting and getting results
```java
MikrotikConnection conn = new MikrotikConnection();
try {
   MikrotikSession session = con.connect("<ip of router>", 8728);
   try {
      session.login("<user>", "<pass>");
      List<Map<String, String>> res = session.execute(new Command("/system/routerboard/print"));
      for(Map<String, String> item : res) {
         System.out.println(item.getKey() + "=" + item.getValue());
      }
   } finally {
       session.close();
   }
} finally {
   conn.close();
}
```

Connection through TLS
```java
Certificate cert = ...;
KeyPair keyPair = ...;

MikrotikConnection conn = new MikrotikConnection();
try {
   MikrotikSession session = con.connect("<ip of router>", 8729, certificate, keyPair.private);
   try {
      session.login("<user>", "<pass>");
      List<Map<String, String>> res = session.execute(new Command("/system/routerboard/print"));
      for(Map<String, String> item : res) {
         System.out.println(item.getKey() + "=" + item.getValue());
      }
   } finally {
       session.close();
   }
} finally {
   conn.close();
}
```

The command accepts the path, some attributes and queries.

```java
Command cmd = new Command("/system/reboot"); //simple command
```

```java
Map<String, String> attrs = new HashMap<String, String>();
attrs.put("numbers", "*0")
Command cmd = new Command("/interface/print", attrs); //simple command with attributes
```

```java
Command cmd = new Command("/interface/print", new CommandQueryExpression("name", CommandQueryOperation.EQUALS, "ether1")); //simple command with query
```

```java
Map<String, String> attrs = new HashMap<String, String>();
attrs.put("numbers", "*0")
Command cmd = new Command("/interface/print", attrs, new CommandQueryExpression("name", CommandQueryOperation.EQUALS, "ether1"));
```

## Contributing

Found any bug, or want to improve the code (or this readme)? Any pull request is welcome. Want only to report? Open an issue. Thanks!

# References

1. [RouterOS API](http://wiki.mikrotik.com/wiki/Manual:API)

# License

This library is released under the Apache 2.0 licence. See the [License.md](LICENSE.md) file.
