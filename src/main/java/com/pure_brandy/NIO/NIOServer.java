package com.pure_brandy.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NIOServer {
    private final int PORT;

    Map<SocketChannel,String> onlineUsers;
    Pattern pattern= Pattern.compile("(\\[)(.*)(\\])(.*)");

    public NIOServer(int port) {
        PORT = port;
        onlineUsers=new HashMap<>();
    }

    public static void main(String[] args) throws IOException {
        NIOServer server = new NIOServer(Integer.parseInt(args[0]));
        server.start();
    }

    public void start() throws IOException {
        //1- 创建并配置serversocketchannel用于监听连接
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.bind(new InetSocketAddress(PORT));

        //2- 创建selector用于轮询
        Selector selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务器端启动！");
        while(true){
            int cnt = selector.select();
            if(cnt==0) continue;

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                //selected keys 不会自动移除，不移除下次会重复处理；
                iterator.remove();
                if(selectionKey.isAcceptable()){
                    handleAccept(selectionKey,selector);
                }else if(selectionKey.isReadable()){
                    handleMessage(selectionKey,selector);
                }
            }
        }
    }

    private void handleMessage(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel channel=(SocketChannel) selectionKey.channel();
        if(!(channel!=null &&
                channel instanceof SocketChannel)){
            return;
        }
        String request = receiveMsg((SocketChannel) channel);
        if(request==null){//断开了
            String user=onlineUsers.get(channel);
            System.out.println("客户端断开了连接..."
                    + channel.socket().getRemoteSocketAddress()+"("+user+")");

            //广播给其他用户
            broadcastMsg(channel, selector, user+"离开了聊天室！\n",false);
            onlineUsers.remove(channel);
            selectionKey.channel();
            channel.close();
            return;
        }

        if(request.length()>0){
            Matcher matcher = pattern.matcher(request);
            if(matcher.find()){
                String msgType=matcher.group(2);
                String msg=matcher.group(4);

                if(msgType.equals("login")){
                    onlineUsers.put(channel,msg);
                    //广播给其他用户
                    broadcastMsg(channel, selector, "欢迎"+msg+"加入直播间！\n",true);

                }else{//msg类
                    String user=onlineUsers.get(channel);
                    //广播给其他用户
                    broadcastMsg(channel, selector, user+" : "+msg+"\n",false);
                }
            }
            //返还channel给selector
            channel.register(selector,SelectionKey.OP_READ);
        }


    }

    private String receiveMsg(SocketChannel channel) throws IOException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String request="";
        while(true) {//读取通道数据到缓冲区中,非-1就代表有数据
            int read = channel.read(byteBuffer);

            if(read==-1){
                return null;
            }

            if(read==0){
                break;
            }

            //确定缓冲区数据的起点和终点
            byteBuffer.flip();
            //对bytebuf进行解码，避免乱码
            request+= Charset.forName("UTF-8").decode(byteBuffer).toString();

            //清空缓冲区，再次放入数据
            byteBuffer.clear();
        }
        return request;
    }

    private void broadcastMsg(SocketChannel channel, Selector selector, String request,boolean includeSelf) throws IOException {
        Set<SelectionKey> keys = selector.keys();
        for(SelectionKey selectionKey:keys){
            SelectableChannel selectableChannel = selectionKey.channel();
            if(selectableChannel instanceof SocketChannel){
                if(includeSelf || !includeSelf && !selectableChannel.equals(channel) )
                    ((SocketChannel) selectableChannel).write(Charset.forName("UTF-8").encode(request));
            }
        }
    }

    private void handleAccept(SelectionKey selectionKey, Selector selector) throws IOException {
        SelectableChannel channel=selectionKey.channel();
        if(channel!=null &&
                channel instanceof ServerSocketChannel){
            //1- 创建接收连接的sc
            SocketChannel messageChannel = ((ServerSocketChannel) channel).accept();

            messageChannel.configureBlocking(false);

            System.out.println("客户端连接..."
                    + messageChannel.socket().getRemoteSocketAddress());

            //2- sc注册到selector
            messageChannel.register(selector,SelectionKey.OP_READ);

        }
    }
}
