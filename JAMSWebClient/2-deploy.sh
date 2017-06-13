#!/bin/bash
# 2-deploy.sh is the second step in deploying the client. It is supposed to be
# run on the server that hosts the website. The script unpacks the uploaded
# archive and moves it into the correct location.

DIR=/home/modis/websites/jams-web-client
cd "$DIR"

# Unpack archive to temporary location
TIMESTAMP=`date +%s`
TEMP_DIR=./temp-$TIMESTAMP
mkdir "$TEMP_DIR"
tar -xf ./dist.tar.xz -C "$TEMP_DIR"

# Move current website to backup location
mv ./dist ./dist-$TIMESTAMP.bak

# Move new website to live location
mv $TEMP_DIR/dist ./dist

# Clean up
rmdir "$TEMP_DIR"
rm ./dist.tar.xz
