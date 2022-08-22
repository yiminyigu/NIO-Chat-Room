package com.pure_brandy.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NIOClient {
    private String uname;
    private final String server_ip;
    private final int server_port;

    public NIOClient(String uname, String server_ip, int server_port) {
        this.uname = uname;
        this.server_ip = server_ip;
        this.server_port = server_port;
    }

    public static void main(String[] args) throws IOException {
        NIOClient nioClient = new NIOClient(args[0], args[1],Integer.parseInt(args[2]));
        nioClient.start();
    }


    public void start() throws IOException {
        //1- 和服务器建立连接
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",6789));

        socketChannel.write(Charset.forName("UTF-8").encode("[login]"+uname));

        socketChannel.configureBlocking(false);

        //2- 创建selector轮询
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);

        //3- 监听服务器发送的数据
        new NIOClientHandler(selector).start();

        //4- 接收console发送的数据
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line!=null&&line.length()>0){
                socketChannel.write(Charset.forName("UTF-8").encode("[msg]"+line));
            }
        }
    }
}
