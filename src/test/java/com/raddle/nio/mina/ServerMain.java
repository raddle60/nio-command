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

import com.raddle.nio.mina.cmd.handler.AbstractCommandHandler;
import com.raddle.nio.mina.hessian.HessianDecoder;
import com.raddle.nio.mina.hessian.HessianEncoder;

public class ServerMain {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final BooleanHolder bool = new BooleanHolder(false);
		final IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.getSessionConfig().setReaderIdleTime(10);// 10秒沒收到数据就超时
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new HessianEncoder(), new HessianDecoder()));
		// 处理接收的命令和响应
		acceptor.setHandler(new AbstractCommandHandler() {

			@Override
			protected Object processCommand(Object command) {
				if ("quit".equals(command)) {
					System.out.println("command [" + command + "] received");
					bool.value = true;
					return null;
				} else {
					System.out.println("command [" + command + "] received");
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

		});
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
			acceptor.unbind();
			acceptor.dispose();
			System.out.println("MINA server shutdown");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("MINA server shutdown");
			acceptor.dispose();
		}
	}

}
