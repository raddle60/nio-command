package com.raddle.nio.mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.raddle.nio.codec.impl.HessianCodec;
import com.raddle.nio.mina.cmd.CommandContext;
import com.raddle.nio.mina.cmd.SessionCommandSender;
import com.raddle.nio.mina.cmd.api.CommandCallback;
import com.raddle.nio.mina.cmd.api.CommandSender;
import com.raddle.nio.mina.cmd.handler.AbstractCommandHandler;
import com.raddle.nio.mina.codec.ChainCodecFactory;

public class ClientMain {
	public static void main(String[] args) {
		NioSocketConnector connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(1000);
		connector.getSessionConfig().setReaderIdleTime(10);// 10秒沒收到数据就超时
		ChainCodecFactory chainCodecFactory = new ChainCodecFactory();
		chainCodecFactory.addFirst(new HessianCodec());
		connector.getFilterChain().addFirst("codec", new ProtocolCodecFilter(chainCodecFactory));
		AbstractCommandHandler handler = new AbstractCommandHandler() {
			@Override
			public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
				cause.printStackTrace();
				session.close(true);
			}

			@Override
			protected Object processCommand(Object command) {
				System.out.println("received command [" + command + "]");
				return null;
			}

			@Override
			public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
				session.close(true);
			}

			@Override
			protected String getExecuteQueue(Object command) {
				return null;
			}
		};
		// 处理接收的命令和响应
		connector.setHandler(handler);
		ConnectFuture future = connector.connect(new InetSocketAddress("127.0.0.1", 12563));
		future.awaitUninterruptibly();
		IoSession session;
		try {
			session = future.getSession();
			CommandSender sender = new SessionCommandSender(session);
			sender.sendCommand("command:" + System.currentTimeMillis(), 10, new CommandCallback<String, String>() {

				@Override
				public void commandResponse(String command, String response) {
					System.out.println(command + " response[" + response + "]");
					CommandContext.getCommandSender().sendCommand("send command after response [" + command + "]");
				}

				@Override
				public void responseTimeout(String command) {
					System.out.println(command + " timeout");
				}

				@Override
				public void responseException(String command, String type, String message) {
					System.out.println(command + " exception : " + type + " , " + message);
				}
			});
			
			sender.sendCommand("exception",10 ,new CommandCallback<String, String>() {
				
				@Override
				public void commandResponse(String command, String response) {
					System.out.println(command + " response[" + response + "]");
					CommandContext.getCommandSender().sendCommand("send command after response [" + command + "]");
				}
				
				@Override
				public void responseTimeout(String command) {
					System.out.println(command + " timeout");
				}
				
				@Override
				public void responseException(String command, String type, String message) {
					System.out.println(command + " exception : " + type + " , " + message);
				}
			});
			// 收取响应
			Thread.sleep(1000);
			sender.sendCommand("quit", 1, new CommandCallback<String, Object>() {

				@Override
				public void commandResponse(String command, Object response) {
					
				}

				@Override
				public void responseTimeout(String command) {
					System.out.println(command + " response timeout");
				}

				@Override
				public void responseException(String command, String type, String message) {
					System.out.println(command + " exception : " + type + " , " + message);
				}
			});
			session.getCloseFuture().awaitUninterruptibly();
			// 等待出现响应超时
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.dispose();
		connector.dispose();
	}
}
