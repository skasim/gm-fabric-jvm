#!/bin/sh -

#
# This script takes the backed up install and assignes a Date Timestamp.
#
{
   DTS=`date "+_%Y_%m_%d_%H_%M_%S_%s"`
   APP=${rpm.basePath}/${app.name}-${major.minor}
   mv ${APP}_bkp ${APP}_bkp$DTS
}
