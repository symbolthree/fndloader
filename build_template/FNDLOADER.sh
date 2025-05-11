#!/bin/sh
############################################
# FNDLOADER shell script (Client Version)
# $Header: /TOOL/FNDLOADER_V4/build_template/FNDLOADER.sh 1     2/09/17 8:49a Christopher Ho $
#############################################

java -cp \
./lib/client/FNDLOADER-@build.version@.jar:\
./lib/client/CALLA-1.3.jar:\
./lib/client/log4j-api-2.6.2.jar:\
./lib/client/log4j-core-2.6.2.jar:\
./lib/client/jaxen-1.1.6.jar:\
./lib/client/jdom-2.0.6.jar:\
./lib/client/commons-io-2.5.jar:\
./lib/server/collections.zip:\
./lib/server/fndext.jar:\
./lib/server/netcfg.jar:\
./lib/server/ojdbc6.jar:\
./lib/server/share.jar:\
./lib/server/uix2.jar:\
./lib/server/xmlparserv2.jar:\
./lib/server/oamdsdt.jar:\
./lib/server/mdsrt.jar:\
./lib/server/mdsdt.jar:\
./lib/server/xdo.zip \
-splash:splash.gif \
symbolthree.flower.CALLA $*
