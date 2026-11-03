package sshterminal;

import com.jcraft.jsch.*;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class JschTtyConnector implements TtyConnector {
    private final Session session;
    private final ChannelShell channel;
    private final Reader reader;
    private final OutputStream out;

    public JschTtyConnector(Session session) throws JSchException, IOException {
        this.session = session;
        this.channel = (ChannelShell) session.openChannel("shell");
        channel.setPtyType("xterm-256color");
        channel.setEnv("LANG", "en_US.UTF-8");
        InputStream in = channel.getInputStream();   // get streams BEFORE connect()
        this.out = channel.getOutputStream();
        this.reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        channel.connect(10_000);
    }

    @Override public int read(char[] buf, int off, int len) throws IOException {
        return reader.read(buf, off, len);           // blocks; -1 = remote closed
    }
    @Override public void write(byte[] bytes) throws IOException {
        out.write(bytes); out.flush();
    }
    @Override public void write(String s) throws IOException {
        write(s.getBytes(StandardCharsets.UTF_8));
    }
    @Override public boolean isConnected() { return channel.isConnected(); }
    @Override public boolean ready() throws IOException { return reader.ready(); }
    @Override public String getName() { return session.getUserName() + "@" + session.getHost(); }

    @Override public void resize(TermSize size) {        // window resized -> tell server
        channel.setPtySize(size.getColumns(), size.getRows(), 0, 0);
    }
    @Override public int waitFor() throws InterruptedException {
        while (!channel.isClosed()) Thread.sleep(100);
        return channel.getExitStatus();
    }
    @Override public void close() {
        channel.disconnect();
        session.disconnect();
    }
}
