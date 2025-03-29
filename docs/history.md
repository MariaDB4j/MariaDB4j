# MariaDB4j Historical Notes

## Maven Plugin migration

To upgrade from the original `mariadb4j-maven-plugin` by [`@mike10004`](https://github.com/mike10004) to this MariaDB4j (by vorburger@) version, please change:

```xml
<plugin>
    <groupId>com.github.mike10004</groupId>
    <artifactId>mariadb4j-maven-plugin</artifactId>
    ...
</plugin>
```

to

```xml
<plugin>
    <groupId>ch.vorburger.mariaDB4j</groupId>
    <artifactId>mariaDB4j-maven-plugin</artifactId>
    ...
</plugin>
```

If you are using the argument `createDatabase` rename it to `databaseName`.
