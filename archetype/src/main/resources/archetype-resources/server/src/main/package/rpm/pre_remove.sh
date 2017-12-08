#!/bin/sh -

{

echo "Stopping service [﻿${app.name}-${major.minor} ]..."
service ${app.name}-${major.minor} stop || true

}

