{
  description = "Static MariaDB binaries for MariaDB4j";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      forAllSystems = nixpkgs.lib.genAttrs [ "x86_64-linux" "aarch64-linux" ];
    in
    {
      packages = forAllSystems (system:
        let
          pkgs = import nixpkgs {
            inherit system;
          };

          staticPkgs = pkgs.pkgsStatic;

          # Override dependencies that do not support static compilation or are unnecessary
          mariadbStaticBase = staticPkgs.mariadb_114.override {
            stdenv = staticPkgs.ccacheStdenv;
            systemd = null;
            linux-pam = null;
            withStorageMroonga = false;
            withStorageRocks = false;
            withNuma = false;
          };

          # Custom static server derivation with static InnoDB/Aria and disabled plugins
          mariadbStatic = mariadbStaticBase.overrideAttrs (oldAttrs: {
            pname = "mariadb-static";
            outputs = [ "out" ];

            CCACHE_DIR = "/var/cache/ccache";
            CCACHE_UMASK = "007";

            cmakeFlags = (oldAttrs.cmakeFlags or [ ]) ++ [
              "-DDISABLE_SHARED=ON"

              "-DPLUGIN_INNOBASE=STATIC"
              "-DPLUGIN_ARIA=STATIC"
              "-DPLUGIN_MYISAM=STATIC"
              "-DPLUGIN_SEQUENCE=STATIC"

              "-DWITHOUT_ROCKSDB=1"
              "-DWITHOUT_MROONGA=1"
              "-DWITHOUT_TOKUDB=1"
              "-DWITHOUT_COLUMNSTORE=1"
              "-DWITHOUT_PLUGIN_S3=1"
              "-DWITHOUT_CONNECT=1"
              "-DWITHOUT_OQGRAPH=1"
              "-DPLUGIN_AUTH_PAM=NO"
              "-DPLUGIN_AUTH_PAM_V1=NO"
              "-DPLUGIN_AUTH_GSSAPI=NO"
              "-DWITH_WSREP=OFF"
              "-DWITHOUT_DYNAMIC_PLUGINS=ON"
            ];

            postPatch = (oldAttrs.postPatch or "") + ''
              # In tests/CMakeLists.txt, disable async_queries which fails against static libevent
              substituteInPlace tests/CMakeLists.txt \
                --replace-fail 'IF(HAVE_EVENT_H AND EVENT_LIBRARY)' 'IF(FALSE)'

              # In libmariadb, default all client plugins to STATIC
              substituteInPlace libmariadb/cmake/plugins.cmake \
                --replace-fail 'string(TOUPPER ''${CC_PLUGIN_DEFAULT} CC_PLUGIN_DEFAULT)' \
                               'string(TOUPPER ''${CC_PLUGIN_DEFAULT} CC_PLUGIN_DEFAULT)
      if(CC_PLUGIN_DEFAULT STREQUAL "DYNAMIC")
        set(CC_PLUGIN_DEFAULT "STATIC")
      endif()'

              # In libmariadb/libmariadb/CMakeLists.txt, build libmariadb as STATIC
              substituteInPlace libmariadb/libmariadb/CMakeLists.txt \
                --replace-fail 'ADD_LIBRARY(libmariadb SHARED' 'ADD_LIBRARY(libmariadb STATIC' \
                --replace-fail 'INSTALL(TARGETS libmariadb LIBRARY DESTINATION ''${INSTALL_LIBDIR}' \
                               'INSTALL(TARGETS libmariadb ARCHIVE DESTINATION ''${INSTALL_LIBDIR} LIBRARY DESTINATION ''${INSTALL_LIBDIR}'
            '';

            # postInstall in nixpkgs tries to rm non-existent plugins/symlinks which will fail
            postInstall = ''
              rm -f "$out"/lib/mysql/plugin/daemon_example.ini
              rm -f "$out"/lib/{libmariadb.a,libmariadbclient.a,libmysqlclient.a,libmysqlclient_r.a,libmysqlservices.a}
              rm -f "$out"/bin/{mariadb-config,mariadb_config,mysql_config}
              rm -rf "$out"/include
              rm -rf "$out"/lib/pkgconfig
              # Delete any broken/dangling symlinks
              find "$out" -xtype l -delete

              # Symlink scripts/mariadb-install-db to bin/mariadb-install-db for MariaDB4j backward compatibility
              mkdir -p "$out"/scripts
              ln -sf ../bin/mariadb-install-db "$out"/scripts/mariadb-install-db
            '';

            postFixup = ''
              # Ensure scripts keep a portable #!/bin/sh shebang rather than hardcoding /nix/store
              sed -i '1s|^#!.*|#!/bin/sh|' "$out"/bin/mariadb-install-db 2>/dev/null || true
              if [ -f "$out"/scripts/mariadb-install-db ]; then
                sed -i '1s|^#!.*|#!/bin/sh|' "$out"/scripts/mariadb-install-db 2>/dev/null || true
              fi
            '';
          });
        in
        {
          mariadb-static = mariadbStatic;
          default = mariadbStatic;
        });
    };
}
