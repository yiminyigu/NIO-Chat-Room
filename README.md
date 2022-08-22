启动服务器端：
`java -cp ./bin/NIO-1.0-SNAPSHOT.jar com.pure_brandy.NIO.NIOServer [SERVER_PORT]`

启动客户端：
`java -cp ./bin/NIO-1.0-SNAPSHOT.jar com.pure_brandy.NIO.NIOClient [CLIENT_NAME] [SERVER_IP] [SERVER_PORT]`

例如：
启动服务器端：
`java -cp ./bin/NIO-1.0-SNAPSHOT.jar com.pure_brandy.NIO.NIOServer 6789`

启动客户端：
`java -cp ./bin/NIO-1.0-SNAPSHOT.jar com.pure_brandy.NIO.NIOClient Mike 127.0.0.1 6789`
`java -cp ./bin/NIO-1.0-SNAPSHOT.jar com.pure_brandy.NIO.NIOClient Amy 127.0.0.1 6789`
