These folders contain, per platform and version,
the bin/mysql_install_db, mysqld, mysql, mysqldump, mysqlcheck, my_print_defaults & share/*

Since 10.2.11 these unpacked binaries are zipped into mariaDB/<platform>.zip file as a some files from linux distribution
exceeds github size limit of single file.

These files come from a standard MariaDB distribution.
Technically if you really must then an Oracle mySQL distrib would work as well - as the commands are exactly the same.

Note that on OSX (at least) bin/mysql_install_db is a symlink to scripts/mysql_install_db, and you need to copy the
file from scripts rather than the symlink.
