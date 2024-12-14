package io.sailex.aiNpc.client.util;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

public class VectorUtil {

    public static double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) return 0.0;
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += Math.pow(vec1.get(i), 2);
            norm2 += Math.pow(vec2.get(i), 2);
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public static float[] convertToFloats(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] embedding = new float[bytes.length / 4];
        buffer.asFloatBuffer().get(embedding);
        return embedding;
    }

    public static byte[] convertToBytes(float[] embedding) {
        ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 4);
        buffer.asFloatBuffer().put(embedding);
        return buffer.array();
    }

    protected Float[] convertEmbedding(List<List<Double>> embedding) {
        return embedding.stream().flatMap(Collection::stream)
                .map(Double::floatValue)
                .toArray(Float[]::new);
    }

}
