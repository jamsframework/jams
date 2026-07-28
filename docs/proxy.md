# Building behind an HTTP proxy

Most dependencies are bundled in [`libs-repo/`](../libs-repo/) (see the main
README), but a few libraries — notably GeoTools — are pulled from Maven
Central on first build. If your network requires an HTTP/HTTPS proxy, this
download can fail even though `http_proxy` / `https_proxy` environment
variables are set: Maven does **not** read those automatically — the proxy
has to be configured in `~/.m2/settings.xml` instead.

To fix this, create `~/.m2/settings.xml` (or add the `<proxies>` block
below to it, if the file already exists) with your proxy's host and port:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <proxies>
    <proxy>
      <id>http-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>proxy.example.com</host>
      <port>3128</port>
      <!-- <username>your-username</username> -->
      <!-- <password>your-password</password> -->
      <nonProxyHosts>localhost|127.0.0.1</nonProxyHosts>
    </proxy>
    <proxy>
      <id>https-proxy</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>proxy.example.com</host>
      <port>3128</port>
      <!-- <username>your-username</username> -->
      <!-- <password>your-password</password> -->
      <nonProxyHosts>localhost|127.0.0.1</nonProxyHosts>
    </proxy>
  </proxies>
</settings>
```

Replace `host`/`port` with your proxy's values, uncomment `username`/
`password` if authentication is required, and re-run the build.

## Still failing after configuring the proxy?

If a specific plugin or dependency keeps failing with `Connect timed out`
even after the proxy is set up, Maven likely cached that failure from
before the proxy was configured (or from a transient network blip) and
won't retry until its update interval elapses. Force a retry:

```
./mvnw -U package
```

Or, to only re-check the one affected artifact instead of everything,
delete its stale marker directly and rebuild:

```
rm ~/.m2/repository/<group-path>/<artifact>/<version>/*.lastUpdated
```
