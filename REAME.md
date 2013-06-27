# varnishtest-exec

varnishtest-exec is a portable Java library for varnishtest, the test framework
for [Varnish Cache](https://www.varnish-cache.org/). This framework is used by
the Varnish project, and usually module (VMOD) developers. But as a Varnish
user, you can and *should* use it too. This library not only helps manipulate
varnishtest within a JVM, but it also comes with an Ant task and a Maven plugin.

varnishtest-exec is distributed under the terms of the Apache License version
2.0 (see LICENSE for more details).

## The Ant task

With `varnishtest-exec` in your Ant *classpath*, you can use it this way:

```xml
<project xmlns:varnishtest="antlib:com.zenika.varnishtest.ant">
    <target name="integration-test">
        <varnishtest:run>
            <fileset dir="src/test/varnish">
                <include name="**.vtc"/>
            </fileset>
        </varnishtest:run>
    </target>
</project>
```

## The Maven plugin

For Maven users, you can use the `varnishtest-maven-plugin` instead:

```xml
<plugin>
    <groupId>com.zenika</groupId>
    <artifactId>varnishtest-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

It will run during the `integration-test` phase.

# Miscellaneous

Future releases may support more build/integration tools, Maven reporting, and a
better `varnishtest` integration. If you find a bug or miss a particular feature,
please file an [issue](https://github.com/zenika/varnishtest-exec/issues). You
can find the full documentation of the latest release at
http://zenika.github.io/varnishtest-exec.
