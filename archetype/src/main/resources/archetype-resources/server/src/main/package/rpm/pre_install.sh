#!/bin/sh -


{
#
# Add group.
HAVE_GROUP=`getent group ${app.groupName}`
if [ "$HAVE_GROUP" == "" ]
then
  groupadd ${app.groupName}
fi

#
# Create user account.
HAVE_USER=`getent passwd ${app.runUser}`
if [ "$HAVE_USER" == "" ]
then
   useradd --no-create-home --system --group ${app.groupName} --user ${app.runUser}
fi

#
# For STIG'd group access enforcement
GROUP_ACCESS="/etc/security/groupaccess.conf"
HAVE_STIGGED_GROUPS=`ls $GROUP_ACCESS 2>/dev/null`
if [ "$HAVE_STIGGED_GROUPS" == "" ]
then
   echo "${app.groupName}" > $GROUP_ACCESS
   echo "${app.runUser}" >> $GROUP_ACCESS
else
   ALREADY_HAVE=`grep ${app.groupName} $GROUP_ACCESS 2>/dev/null`
   if [ "$ALREADY_HAVE" == "" ]
   then
     echo "${app.groupName}" >> $GROUP_ACCESS
     echo "${app.runUser}" >> $GROUP_ACCESS
   fi
fi
}