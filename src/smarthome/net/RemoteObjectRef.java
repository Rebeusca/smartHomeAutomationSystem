package smarthome.net;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Representa uma referência a um objeto remoto.
 * Usado para passagem por referência em invocações remotas.
 */
public class RemoteObjectRef implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private InetAddress host;
    private int port;
    private int objectId;  // Identificador único do objeto remoto
    private String interfaceName;  // Nome da interface do objeto remoto
    
    public RemoteObjectRef(InetAddress host, int port, int objectId, String interfaceName) {
        this.host = host;
        this.port = port;
        this.objectId = objectId;
        this.interfaceName = interfaceName;
    }
    
    public InetAddress getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getObjectId() {
        return objectId;
    }
    
    public String getInterfaceName() {
        return interfaceName;
    }
    
    @Override
    public String toString() {
        return "RemoteObjectRef{" +
                "host=" + host +
                ", port=" + port +
                ", objectId=" + objectId +
                ", interfaceName='" + interfaceName + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteObjectRef that = (RemoteObjectRef) o;
        return port == that.port &&
               objectId == that.objectId &&
               host.equals(that.host) &&
               interfaceName.equals(that.interfaceName);
    }
    
    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        result = 31 * result + objectId;
        result = 31 * result + interfaceName.hashCode();
        return result;
    }
}

