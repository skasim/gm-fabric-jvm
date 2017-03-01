#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.thrift;

import com.twitter.util.Future;
import ${package}.${appName}Manager;
public class ${appName}ThriftService implements ${appName}.ServiceIface {
    private ${appName}Manager manager;

    public ${appName}ThriftService(${appName}Manager manager) {
        this.manager = manager;
    }

    @Override
    public Future<String> ping() {
        return Future.value(manager.getPong());
    }
}
