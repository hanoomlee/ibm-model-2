#! /bin/bash
sed -e 's/(/ ( /g' $1 > temp1
sed -e 's/)/ ) /g' temp1 > temp2
sed -e 's/%/ % /g' temp2 > temp3
sed -e 's/\,/ \, /g' temp3 > temp4
sed -e 's/\?/ \? /g' temp4 > temp5
sed -e 's/!/ ! /g' temp5 > temp6
sed -e 's/\"/ \" /g' temp6 > temp7
sed -e 's/\./ \. /g' temp7 > temp8
sed -e 's/:/ : /g' temp8 > temp9
sed -e 's/;/ ; /g' temp9 > $2 