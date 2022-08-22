package com.pure_brandy.NIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOClientHandler extends Thread{
    private Selector selector;

    public NIOClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try{

            while(true){
                int cnt = selector.select();
                if(cnt==0) continue;

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isReadable()){
                        iterator.remove();
                        handleMessage(selectionKey.channel(),selector);
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void handleMessage(SelectableChannel channel, Selector selector) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        if(channel!=null &&
                channel instanceof SocketChannel){
            String response="";
            while((((SocketChannel) channel).read(byteBuffer)) >0) {//读取通道数据到缓冲区中,非-1就代表有数据
                //确定缓冲区数据的起点和终点
                byteBuffer.flip();
                //对bytebuf进行解码，避免乱码
                response+= Charset.forName("UTF-8").decode(byteBuffer).toString();

                //清空缓冲区，再次放入数据
                byteBuffer.clear();
            }
            if(response.length()>0){
                System.out.println(response);
            }

            //返还channel给selector
            channel.register(selector, SelectionKey.OP_READ);
        }


    }

}
