package com.raddle.nio.mina;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.omg.CORBA.BooleanHolder;

import com.raddle.nio.codec.impl.HessianCodec;
import com.raddle.nio.mina.cmd.SessionCommandSender;
import com.raddle.nio.mina.cmd.handler.AbstractCommandHandler;
import com.raddle.nio.mina.codec.ChainCodecFactory;

public class ServerMain {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final BooleanHolder bool = new BooleanHolder(false);
		final IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.getSessionConfig().setReaderIdleTime(10);// 10秒沒收到数据就超时
		ChainCodecFactory chainCodecFactory = new ChainCodecFactory();
		chainCodecFactory.addFirst(new HessianCodec());
		acceptor.getFilterChain().addFirst("codec", new ProtocolCodecFilter(chainCodecFactory));
		// 处理接收的命令和响应
		AbstractCommandHandler handler = new AbstractCommandHandler() {

			@Override
			protected Object processCommand(Object command) {
				if ("quit".equals(command)) {
					System.out.println("command [" + command + "] received");
					bool.value = true;
					return null;
				} else if ("exception".equals(command)){
					throw new UnsupportedOperationException("manual");
				} else {
					System.out.println("command [" + command + "] received");
					// CommandContext.getCommandSender() is current session
					// notify all sessions
					for (IoSession session : acceptor.getManagedSessions().values()) {
						new SessionCommandSender(session).sendCommand("notify");
					}
					return "command [" + command + "] received";
				}
			}

			@Override
			public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
				cause.printStackTrace();
				session.close(true);
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
		acceptor.setHandler(handler);
		try {
			boolean bound = false;
			while (!bound) {
				try {
					acceptor.bind(new InetSocketAddress(12563));
					bound = true;
					System.out.println("MINA server started at " + 12563);
				} catch (BindException e) {
					System.out.println("Waiting For Port[" + 12563 + "] , " + e.getMessage());
					Thread.sleep(5000);
				}
			}
			while (!bool.value) {
				Thread.sleep(500);
			}
			// 不能在handler里调用，socket会无法关闭
			System.out.println("shuting down server ");
			acceptor.unbind();
			handler.dispose();
			acceptor.dispose();
			System.out.println("MINA server shutdown");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("MINA server shutdown");
			handler.dispose();
			acceptor.dispose();
		}
	}

}
