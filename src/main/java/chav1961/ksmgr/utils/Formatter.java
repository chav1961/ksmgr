package chav1961.ksmgr.utils;

import java.io.IOException;

import chav1961.ksmgr.interfaces.FileExtension;

public class Formatter {
	public static byte[] serialize(final byte[] content, final FileExtension ext) throws IOException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content to serialize can't be null or empty");
		}
		else if (ext == null) {
			throw new NullPointerException("Extension can't be null");
		}
		else {
			return content;
		}
	}

	public static byte[] deserialize(final byte[] content, final FileExtension ext) throws IOException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content to deserialize can't be null or empty");
		}
		else if (ext == null) {
			throw new NullPointerException("Extension can't be null");
		}
		else {
			return content;
		}
	}
}
