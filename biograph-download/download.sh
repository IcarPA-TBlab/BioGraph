#!/bin/bash

STARTDIR=`pwd`

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

WGET=/opt/local/bin/wget
GZIP=/usr/bin/gzip
RSYNC=/usr/bin/rsync
GREP=/usr/bin/grep
AWK=/usr/bin/awk
UNIQ=/usr/bin/uniq

# xlsx to csv converter (http://github.com/dilshod/xlsx2csv)
# sudo easy_install xlsx2csv / pip install xlsx2csv
XLSX2CSV=/usr/local/bin/xlsx2csv

TODAY=$(date +%Y-%m-%d)

cd $DIR/datasources

$WGET -N -i $DIR/urls.lst -o $DIR/log/status_$TODAY.log

if [[ $RC -ne 0 ]]; then
        echo 'failed with returncode:' $RC > $DIR/log/error_$TODAY.log
        exit $RC
fi

$GZIP -dfk *.gz

$GREP -v '^R.' miRNA.dat > miRNA.txt
$GREP R-HSA gene_association.reactome | $AWK '{ print $4 "\t" substr($5,index($5,":")+1) }' | $UNIQ > pathway2go.txt
$GREP R-HSA gene_association.reactome | $AWK '{ print $3 "\t" substr($5,index($5,":")+1) }' | $UNIQ > uniprot2pathway.txt
$XLSX2CSV -d 'tab' hsa_MTI.xlsx hsa_MTI.txt

cd $STARTDIR
