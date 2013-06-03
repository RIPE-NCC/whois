package net.ripe.db.whois.nrtm.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketChannelFactory {


    public static SocketChannel createSocketChannel(final String host, final int port) throws IOException {
        final SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
        socketChannel.connect(new InetSocketAddress(host, port));
        return socketChannel;
    }

    public static Reader createReader(final SocketChannel socketChannel) {
        return new Reader(socketChannel);
    }

    public static Writer createWriter(final SocketChannel socketChannel) {
        return new Writer(socketChannel);
    }

    public static class Reader {
        private final SocketChannel socketChannel;
        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        public Reader(final SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        public String readLine() throws IOException {
            final StringBuilder builder = new StringBuilder();

            if (buffer.position() > 0) {
                if (readLineFromBuffer(builder)) {
                    return builder.toString();
                }
            }

            for (;;) {
                final int length = socketChannel.read(buffer);
                if (length == 0) {
                    continue;
                }

                if (length == -1) {
                    throw new IOException("End of stream");
                }

                buffer.flip();


                if (readLineFromBuffer(builder)) {
                    return builder.toString();
                }
            }
        }

        private boolean readLineFromBuffer(final StringBuilder builder) {
            while (buffer.hasRemaining()) {
                byte next = buffer.get();
                if (next == '\n') {
                    if (buffer.position() == buffer.limit()) {
                        // no more bytes in buffer
                        buffer.clear();
                    }
                    return true;
                }

                builder.append((char)next);
            }

            // no more bytes in buffer
            buffer.clear();
            return false;
        }

    }

    public static class Writer {
        private static final byte[] NEWLINE = new byte[]{'\n'};

        private final SocketChannel socketChannel;

        public Writer(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        public void writeLine(final String line) throws IOException {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(line.length() + 1);
            byteBuffer.put(line.getBytes());
            byteBuffer.put(NEWLINE, 0, 1);
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                socketChannel.write(byteBuffer);
            }
        }
    }


}
