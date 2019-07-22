1. Make sure you have all required libs: ``brew install cmake jemalloc``
2. Download the MacOS SDK for the minimum supported version from https://github.com/phracker/MacOSX-SDKs/releases
3. Unzip MacOS SDK (e.g. `~/dev/MacOSX10.13.sdk`)
4. Download OpenSSL source from  https://www.openssl.org/source (latest version openssl-1.1.1c.tar.gz)
5. Unzip openssl (e.g. `~/dev/openssl-1.1.1c`)
6. Create new folder for openssl build (e.g. `~/dev/openssl`)
7. Inside openssl source folder run following command:
    `./Configure darwin64-x86_64-cc --prefix={absolute path to build target => ~/dev/openssl} no-shared CFLAGS=" -isysroot {absolute path to MacOS SDK => ~/dev/MacOSX10.13.sdk}" LDFLAGS=" -isysroot {absolute path to MacOS SDK => ~/dev/MacOSX10.13.sdk}"`
8. In the openssl source folder run `make`
9. In the openssl source folder run `make install`
10. Download the source of the mariadb version to build
11. Unzip source
12. Create folder `mariadb-build` as sibling of mariadb source
13. `cd mariadb-build`
14. run following command (before running check if paths are correct) 
`cmake ../maraidb-source 
-DBUILD_CONFIG=mysql_release -DCMAKE_INSTALL_PREFIX=~/dev/mariadb 
-DOPENSSL_INCLUDE_DIR=/Users/osx/dev/openssl/include -DOPENSSL_LIBRARIES=/Users/osx/dev/openssl/lib/libssl.a 
-DCRYPTO_LIBRARY=/Users/osx/dev/openssl/lib/libcrypto.a  -DOPENSSL_ROOT_DIR=/Users/osx/dev/openssl 
-DWITH_SSL=/Users/osx/dev/openssl -DCMAKE_C_FLAGS="-Wno-deprecated-declarations" 
-DCMAKE_OSX_SYSROOT=/Users/osx/dev/MacOSX10.13.sdk -DCMAKE_OSX_DEPLOYMENT_TARGET=10.13 -DWITHOUT_TOKUDB=1 
-DWITH_SSL=yes -DDEFAULT_CHARSET=UTF8 -DDEFAULT_COLLATION=utf8_general_ci -DCOMPILATION_COMMENT=CrafterCms  
-DWITH_PCRE=bundled -DWITH_READLINE=on  -DWITH_JEMALLOC=/usr/local/Cellar/jemalloc/5.2.0/includeke ../ 
-DBUILD_CONFIG=mysql_release -DCMAKE_INSTALL_PREFIX=~/dev/mariadb -DOPENSSL_INCLUDE_DIR=/Users/osx/dev/openssl/include -DOPENSSL_LIBRARIES=/Users/osx/dev/openssl/lib/libssl.a -DCRYPTO_LIBRARY=/Users/osx/dev/openssl/lib/libcrypto.a  -DOPENSSL_ROOT_DIR=/Users/osx/dev/openssl -DWITH_SSL=/Users/osx/dev/openssl -DCMAKE_C_FLAGS="-Wno-deprecated-declarations" -DCMAKE_OSX_SYSROOT=/Users/osx/dev/MacOSX10.13.sdk -DCMAKE_OSX_DEPLOYMENT_TARGET=10.13 -DWITHOUT_TOKUDB=1 -DWITH_SSL=yes -DDEFAULT_CHARSET=UTF8 -DDEFAULT_COLLATION=utf8_general_ci -DCOMPILATION_COMMENT=CrafterCms  -DWITH_PCRE=bundled -DWITH_READLINE=on  -DWITH_JEMALLOC=/usr/local/Cellar/jemalloc/5.2.0/include
`
15. run `make`
16. run `make install`
17. the binaries should be in this folder `~/dev/mariadb`