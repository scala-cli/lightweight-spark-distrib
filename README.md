# lightweight-spark-distrib

*lightweight-spark-distrib* is a small application allowing to make Spark distributions more
lightweight. From an existing Spark distribution, *lightweight-spark-distrib* looks for the
JARs it contains and tries to find those on Maven Central. It then copies all files but the JARs
it found on Maven Central to a new directory, and writes alongside them a script that
relies on [coursier](https://github.com/coursier/coursier) to fetch the missing JARs.

The resulting Spark distributions are much more lightweight (~25 MB uncompressed / ~16 MB compressed)
than their original counterpart (which usually weight more than 200 MB). As a consequence, the former
are easier to distribute, and more easily benefit from mechanisms such as CI caches.


## Generate a lightweight archive

```text
$ scala-cli run \
    --workspace . \
    src \
    -- \
      --dest spark-3.0.3-bin-hadoop2.7-lightweight.tgz \
      https://archive.apache.org/dist/spark/spark-3.0.3/spark-3.0.3-bin-hadoop2.7.tgz \
      --spark 3.0.3 \
      --scala 2.12.10 \
      --archive
```

## Using a lightweight archive

Run the `fetch-jars.sh` script right before use. This script downloads missing JARs using [coursier](https://github.com/coursier/coursier). It downloads coursier on its own if needed.

```bash
$ curl -fLo spark-distrib.tar.gz https://github.com/scala-cli/lightweight-spark-distrib/releases/download/v0.0.4/spark-2.4.2-bin-hadoop2.7-scala2.12.tgz
$ tar -zxf spark-distrib.tar.gz
$ cd spark-2.4.2-bin-hadoop2.7
$ ./fetch-jars.sh
```
