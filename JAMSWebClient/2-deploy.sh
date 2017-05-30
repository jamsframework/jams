#!/bin/bash
# 2-deploy.sh is the second step in deploying the client. It is supposed to be
# run on the server that hosts the website. The script unpacks the uploaded
# archive and moves it into the correct location.

DIR=/home/modis/websites/jams-web-client
cd "$DIR"

# Unpack archive to temporary location
mkdir ./temp
tar -xf ./dist.tar.xz -C ./temp

# Move current website to backup location
mv ./dist ./dist.bak

# Move new website to live location
mv ./temp/dist ./dist

# Clean up
rmdir ./temp
rm ./dist.tar.xz
rm -r ./dist.bak
