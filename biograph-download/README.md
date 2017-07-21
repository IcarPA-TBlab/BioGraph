# biograph-download
Shell script to download required data sources.

It may not work as is, because it depends on the location of the following standard tools:

wget
gzip
grep
awk
uniq

It also depends by xlsx2csv, an xlsx to csv converter written in python (see http://github.com/dilshod/xlsx2csv). Type 

sudo easy_install xlsx2csv / pip install xlsx2csv

in a terminal window to install.
