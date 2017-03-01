#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
namespace * ${package}.thrift

service ${appName} {

   // Keep this. Use to see if the service is accepting connections.
  string ping()
}