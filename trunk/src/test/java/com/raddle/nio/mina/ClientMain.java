package com.raddle.nio.mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.raddle.nio.mina.cmd.CommandContext;
import com.raddle.nio.mina.cmd.SessionCommandSender;
import com.raddle.nio.mina.cmd.api.CommandCallback;
import com.raddle.nio.mina.cmd.api.CommandSender;
import com.raddle.nio.mina.cmd.handler.AbstractCommandHandler;
import com.raddle.nio.mina.hessian.HessianDecoder;
import com.raddle.nio.mina.hessian.HessianEncoder;

public class ClientMain {
	public static void main(String[] args) {
		NioSocketConnector connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(1000);
		connector.getSessionConfig().setReaderIdleTime(10);// 10秒沒收到数据就超时
		connector.getFilterChain().addFirst("binaryCodec", new ProtocolCodecFilter(new HessianEncoder(), new HessianDecoder()));
		// 处理接收的命令和响应
		connector.setHandler(new AbstractCommandHandler() {
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
		});
		ConnectFuture future = connector.connect(new InetSocketAddress("127.0.0.1", 12563));
		future.awaitUninterruptibly();
		IoSession session;
		try {
			session = future.getSession();
			CommandSender sender = new SessionCommandSender(session);
			sender.sendCommand("command:" + System.currentTimeMillis(), new CommandCallback<String, String>() {

				@Override
				public void commandResponse(String command, String response) {
					System.out.println(command + " response[" + response + "]");
					CommandContext.getCommandSender().sendCommand("send command after response [" + command + "]");
				}

				@Override
				public void responseTimeout(String command) {
					System.out.println(command + " timeout");
				}
			});
			// 收取响应
			Thread.sleep(1000);
			sender.sendCommand("quit");
			session.getCloseFuture().awaitUninterruptibly();
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
		connector.dispose();
	}
}
