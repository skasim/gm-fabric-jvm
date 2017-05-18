#!/bin/sh -

{
# Create service::
chkconfig --add ${app.name}-${major.minor}
}

