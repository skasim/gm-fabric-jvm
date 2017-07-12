#!/bin/sh -

{

echo "Stopping service [﻿${app.name}-${major.minor} ]..."
service ${app.name}-${major.minor} stop || true

#
# Backup the current install. Remove the unneeded directories.
# Doing this to keep the configuration and logs of the install being removed.
# May want to re-use the configureation & review the logs.
#
APP=${rpm.basePath}/${app.name}-${major.minor}
if [ -e "${APP}" ]
then
  cd ${rpm.basePath}
  cp -a ${APP} ${APP}_bkp
  rm -rf ${APP}_bkp/{bin,lib,logs}
fi

}

