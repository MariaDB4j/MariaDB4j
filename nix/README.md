# MariaDB4j's static MariaDB binaries with Nix

For background, see https://github.com/MariaDB4j/MariaDB4j/issues/1231.

For more about Nix, see e.g. https://nixfiles.vorburger.ch

## MariaDB4j Binary Requirements

MariaDB4j only ever invokes a small subset of MariaDB binaries (see `DBConfigurationBuilder._getExecutables()`).

The table below lists these required binaries along with their statically linked file sizes for **MariaDB 11.4.12** (LTS):

| Executable Enum | Linux File Path | Size (v11.4.12) | Purpose |
| :--- | :--- | :--- | :--- |
| `Server` | `bin/mariadbd` | 33 MB | The actual MariaDB server daemon |
| `Client` | `bin/mariadb` | 13 MB | The interactive CLI client |
| `Admin` | `bin/mariadb-admin` | 12 MB | Ping, status, and graceful shutdown |
| `Dump` | `bin/mariadb-dump` | 12 MB | Database export / dump |
| *(Optional)* | `bin/mariadb-check` | 12 MB | Table maintenance and check utility |
| *(Optional)* | `bin/resolveip` | 4.6 MB | Resolves hostnames to IP addresses |
| `PrintDefaults` | `bin/my_print_defaults` | 4.5 MB | Reads configuration options / defaults |
| `InstallDB` | `scripts/mariadb-install-db` | 23 KB | Shell script to initialize system database tables |
| **Total** | | **~90 MB** *(~31 MB compressed `.tar.gz` with `share/`)* | Full standalone binary footprint |

Everything else built by upstream MariaDB (e.g. `mariadb-backup` [29 MB], `aria_chk` [11 MB], `innochecksum` [4.8 MB], `wsrep_*`, test suites, and examples) is never used by MariaDB4j.

## Usage

### Building

To build the static MariaDB server, client, and tools:

```bash
cd nix
nix build .#mariadb-static
```

The resulting binaries will be available in `./result/bin`:

* `mariadbd` (Server)
* `mariadb` (Client)
* `my_print_defaults`
* `mariadb-dump`
* `mariadb-admin`
* `mariadb-check`
* `resolveip`
* `mariadb-install-db` (Script)

### Verifying Static Linking

You can verify that the binaries are 100% statically linked:

```bash
file ./result/bin/mariadbd
# Output: ELF 64-bit LSB executable, x86-64, ..., statically linked

ldd ./result/bin/mariadbd
# Output: not a dynamic executable
```

### Initializing a Database

You can run `mariadb-install-db` to verify database initialization directly:

```bash
./result/bin/mariadb-install-db \
  --datadir=/tmp/mariadb-data \
  --basedir=./result \
  --no-defaults \
  --force \
  --skip-name-resolve
```

### Fast Incremental Builds with ccache

This sub-flake uses `pkgsStatic.ccacheStdenv` and stores the compiler cache in `/var/cache/ccache`.

[NixOS systems with](https://github.com/vorburger/nixfiles/commit/e1ed115c87b872bc84c8d886cbd7bdcb00edfa21)
`programs.ccache.enable = true` and `nix.settings.extra-sandbox-paths = [ "/var/cache/ccache" ];`
will automatically cache and reuse compiled objects across builds.
