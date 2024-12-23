package io.sailex.ai.npc.client.util;

import java.nio.ByteBuffer;

public class VectorUtil {

	private static final double THRESHOLD = 0.5;

	private VectorUtil() {}

	public static double cosineSimilarity(double[] vec1, double[] vec2) {
		if (vec1.length != vec2.length) return 0.0;
		double dotProduct = 0.0;
		double norm1 = 0.0;
		double norm2 = 0.0;
		for (int i = 0; i < vec1.length; i++) {
			dotProduct += vec1[i] * vec2[i];
			norm1 += Math.pow(vec1[i], 2);
			norm2 += Math.pow(vec2[i], 2);
		}
		return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
	}

	public static boolean isSimilar(double angle) {
		return angle < THRESHOLD;
	}

	public static double[] convertToDoubles(byte[] bytes) {
		return new double[bytes.length / 8];
	}

	public static byte[] convertToBytes(double[] embedding) {
		ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 8);
		buffer.asDoubleBuffer().put(embedding);
		return buffer.array();
	}
}
