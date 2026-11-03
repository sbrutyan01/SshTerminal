package sshterminal;

import com.formdev.flatlaf.FlatDarkLaf;
import com.jcraft.jsch.*;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;

public class SshTerminal {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(SshTerminal::showConnectDialog);
    }

    static void showConnectDialog() {
        var host = new JTextField("192.168.1.10");
        var port = new JTextField("22");
        var user = new JTextField(System.getProperty("user.name"));
        var pass = new JPasswordField();
        var form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Host:"));     form.add(host);
        form.add(new JLabel("Port:"));     form.add(port);
        form.add(new JLabel("User:"));     form.add(user);
        form.add(new JLabel("Password (empty = key):")); form.add(pass);

        if (JOptionPane.showConfirmDialog(null, form, "SSH Connect",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) System.exit(0);

        // Never do network I/O on the Event Dispatch Thread
        Thread.ofVirtual().start(() -> {
            try {
                Session s = openSession(host.getText(), Integer.parseInt(port.getText()),
                                        user.getText(), new String(pass.getPassword()));
                var connector = new JschTtyConnector(s);
                SwingUtilities.invokeLater(() -> openTerminalWindow(connector));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Connection failed",
                                                  JOptionPane.ERROR_MESSAGE);
                    showConnectDialog();
                });
            }
        });
    }

    static Session openSession(String host, int port, String user, String password) throws Exception {
        var jsch = new JSch();
        Path sshDir = Path.of(System.getProperty("user.home"), ".ssh");   // C:\Users\<you>\.ssh
        Path knownHosts = sshDir.resolve("known_hosts");
        if (!Files.exists(knownHosts)) { Files.createDirectories(sshDir); Files.createFile(knownHosts); }
        jsch.setKnownHosts(knownHosts.toString());

        for (String key : new String[]{"id_ed25519", "id_ecdsa", "id_rsa"}) {
            Path p = sshDir.resolve(key);
            if (Files.exists(p)) jsch.addIdentity(p.toString());
        }

        Session s = jsch.getSession(user, host, port);
        if (!password.isEmpty()) s.setPassword(password);
        s.setConfig("StrictHostKeyChecking", "ask");
        s.setUserInfo(new SwingUserInfo());          // host-key + passphrase prompts
        s.setServerAliveInterval(30_000);
        s.connect(15_000);
        return s;
    }

    static void openTerminalWindow(JschTtyConnector connector) {
        var settings = new DefaultSettingsProvider() {
            @Override public Font getTerminalFont() {
                return new Font("Cascadia Mono", Font.PLAIN, (int) getTerminalFontSize());
            }
            @Override public float getTerminalFontSize() { return 15; }
        };
        var widget = new JediTermWidget(100, 30, settings);
        widget.setTtyConnector(connector);
        widget.start();

        var frame = new JFrame(connector.getName());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { connector.close(); }
        });
        frame.add(widget);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        widget.requestFocusInWindow();
    }

    /** Shows Swing dialogs for "unknown host key" and key passphrase prompts. */
    static class SwingUserInfo implements UserInfo, UIKeyboardInteractive {
        private String passphrase;
        public String getPassphrase() { return passphrase; }
        public String getPassword() { return null; }
        public boolean promptPassword(String msg) { return false; }
        public boolean promptPassphrase(String msg) {
            var f = new JPasswordField();
            boolean ok = JOptionPane.showConfirmDialog(null, f, msg,
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
            passphrase = new String(f.getPassword());
            return ok;
        }
        public boolean promptYesNo(String msg) {   // e.g. "authenticity of host can't be established"
            return JOptionPane.showConfirmDialog(null, msg, "Host key",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
        public void showMessage(String msg) { JOptionPane.showMessageDialog(null, msg); }
        public String[] promptKeyboardInteractive(String dest, String name, String instr,
                                                  String[] prompts, boolean[] echo) {
            String[] answers = new String[prompts.length];
            for (int i = 0; i < prompts.length; i++) {
                var f = new JPasswordField();
                if (JOptionPane.showConfirmDialog(null, f, prompts[i],
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return null;
                answers[i] = new String(f.getPassword());
            }
            return answers;
        }
    }
}
