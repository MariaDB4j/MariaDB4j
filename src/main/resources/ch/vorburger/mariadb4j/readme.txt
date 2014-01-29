This folders contains, per platform,
the bin/mysql_install_db, mysqld, mysql, mysqlcheck, my_print_defaults & share/*

These files come from a standard MariaDB distribution.
Technically if you really must then an Oracle mySQL distrib would work as well - as the commands are exactly the same.

Note that on OSX (at least) bin/mysql_install_db is a symlink to scripts/mysql_install_db, and you need to copy the
file from scripts rather than the symlink.