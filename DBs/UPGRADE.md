Upgrade Instructions
====================

All downloads should be made from the official page https://downloads.mariadb.org/.

Windows
-------

1. Download the binary distribution for the new version `mariadb-{x.y.z}-winx64.zip`
2. Unzip and copy the required files from `bin` & `shared` folders
3. Update the version in `pom.xml` and all folders

Linux
-----

1. Download the binary distribution for the new version `mariadb-{x.y.z}-linuxx86_64.tar.gz`
2. Uncompress and copy the required files from `bin` & `shared` folders
3. Update the version in `pom.xml` and all folders

macOS
-----

Make sure you have all required libs: ``brew install cmake jemalloc``

1. Checkout the LTS version of OpenSSL from https://github.com/openssl/openssl
2. Build OpenSSL
  1. `./Configure darwin64-x86_64-cc  --prefix={path}/openssl`
  2. `make && make install`
3. Download the MacOS SDK for the minimum supported version from https://github.com/phracker/MacOSX-SDKs/releases
  1. `tar xf MacOSXx.y.sdk.tar.xz`
4. Checkout the new version of MariaDB from https://github.com/MariaDB/server
5. Build MariaDB
  1. `mkdir mariadb-build && cd mariadb-build`
  2. Download LZ4 sources by running `../server-mariadb-{x.y.z}/storage/mroonga/vendor/groonga/vendor/download_lz4.rb`
  2. ```cmake ../server-mariadb-{x.y.z} -DBUILD_CONFIG=mysql_release -DCMAKE_INSTALL_PREFIX={path}/mariadb -DOPENSSL_INCLUDE_DIR={path}/openssl/include -DOPENSSL_LIBRARIES={path}/openssl/lib/libssl.a -DCRYPTO_LIBRARY={path}/openssl/lib/libcrypto.a  -DOPENSSL_ROOT_DIR={path}/openssl -DWITH_SSL={path}/openssl -DCMAKE_C_FLAGS="-Wno-deprecated-declarations" -DCMAKE_OSX_DEPLOYMENT_TARGET=10.10 -DCMAKE_OSX_SYSROOT={path}/MacOSX10.10.sdk -DWITHOUT_TOKUDB=1 -DWITH_SSL=yes -DDEFAULT_CHARSET=UTF8 -DDEFAULT_COLLATION=utf8_general_ci -DCOMPILATION_COMMENT=CrafterCms  -DWITH_PCRE=bundled -DWITH_READLINE=on -DWITH_JEMALLOC=/usr/local/Cellar/jemalloc/5.1.0/include -DGRN_WITH_BUNDLED_LZ4=yes```
  3. `make && make install`
6. Copy the required files from `bin` & `shared` folders
7. Update the version in `pom.xml` and all folders