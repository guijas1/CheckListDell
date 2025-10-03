package com.guijas1.checklistDell.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QRCodeUtil {

    public static String gerarQRCodeBase64(String texto) {
        try {
            int width = 200;
            int height = 200;
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(texto, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
