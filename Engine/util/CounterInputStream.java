package util;

import java.io.DataInput;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Objects;

public class CounterInputStream extends FilterInputStream implements DataInput {
	
	private long position = 0L;

    public CounterInputStream(InputStream in) {
        super(in);
    }

    private byte bytearr[] = new byte[80];
    private char chararr[] = new char[80];

    public final int read(byte b[]) throws IOException {
        int numRead = in.read(b, 0, b.length);
        position += Math.max(0, numRead);
        return numRead;
    }

    public final int read(byte b[], int off, int len) throws IOException {
    	int numRead = in.read(b, off, len);
        position += Math.max(0, numRead);
        return numRead;
    }

    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte b[], int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
        position += n;
    }

    public final int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }
        position += total;
        return total;
    }

    public final boolean readBoolean() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        position++;
        return (ch != 0);
    }

    public final byte readByte() throws IOException {
        int ch = in.read();
        position++;
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    public final int readUnsignedByte() throws IOException {
        int ch = in.read();
        position++;
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    public final short readShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        position += 2;
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public final int readUnsignedShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        position += 2;
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    public final char readChar() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        position += 2;
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    public final int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        position += 4;
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    private byte readBuffer[] = new byte[8];

    public final long readLong() throws IOException {
        readFully(readBuffer, 0, 8);
        position += 8;
        return (((long)readBuffer[0] << 56) +
                ((long)(readBuffer[1] & 255) << 48) +
                ((long)(readBuffer[2] & 255) << 40) +
                ((long)(readBuffer[3] & 255) << 32) +
                ((long)(readBuffer[4] & 255) << 24) +
                ((readBuffer[5] & 255) << 16) +
                ((readBuffer[6] & 255) <<  8) +
                ((readBuffer[7] & 255) <<  0));
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    private char lineBuffer[];

    @Deprecated
    public final String readLine() throws IOException {
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        int room = buf.length;
        int offset = 0;
        int c;

loop:   while (true) {
            switch (c = in.read()) {
              case -1:
              case '\n':
                break loop;

              case '\r':
                int c2 = in.read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream)in).unread(c2);
                }
                break loop;

              default:
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }
	
	public long getPosition() {
		return position;
	}
	
	public void clearPosition() {
		position = 0L;
	}

	@Override
	public int read() throws IOException {
		position++;
		return super.read();
	}

	@Override
	public String readUTF() throws IOException {
		return "";
	}
}
