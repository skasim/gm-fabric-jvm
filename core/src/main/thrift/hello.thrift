namespace java com.deciphernow.thriftjava
#@namespace scala com.deciphernow.thriftscala

#
# This file is used for Spec testing. The 'thrift' directory has to reside under main directory.
# It does not re-generate the thrift files; Use this as a reference.
# The generated thrift files were put in test/scala/com/deciphernow/server/security/support.
#
service Hello {
  string hi();
}