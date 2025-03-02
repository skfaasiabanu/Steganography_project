import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class project {

    // Function to encode the secret data into the image
    public static void encodeImage(String imagePath, String secretData, String outputImagePath) throws IOException {
        // Append a termination character to indicate end of message
        secretData += "|";

        // Convert the secret data into binary
        String binaryData = toBinary(secretData);
        
        // Load the image
        File imageFile = new File(imagePath);
        BufferedImage image = ImageIO.read(imageFile);

        // Check if the image can hold the data
        if (binaryData.length() > image.getWidth() * image.getHeight() * 3) {
            System.out.println("Error: The image is too small to hold the data.");
            return;
        }

        // Encoding the data into the image
        int dataIndex = 0;
        outerLoop:
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                // Get the current pixel
                Color pixelColor = new Color(image.getRGB(i, j));

                // Modify the pixel colors to embed the secret data
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();

                // Modify the LSB (Least Significant Bit) of each color channel
                if (dataIndex < binaryData.length()) {
                    r = (r & 0xFE) | (binaryData.charAt(dataIndex++) - '0'); // LSB of Red
                }
                if (dataIndex < binaryData.length()) {
                    g = (g & 0xFE) | (binaryData.charAt(dataIndex++) - '0'); // LSB of Green
                }
                if (dataIndex < binaryData.length()) {
                    b = (b & 0xFE) | (binaryData.charAt(dataIndex++) - '0'); // LSB of Blue
                }

                // Set the new color back to the pixel
                image.setRGB(i, j, new Color(r, g, b).getRGB());

                if (dataIndex >= binaryData.length()) {
                    break outerLoop; // Stop once all data is encoded
                }
            }
        }

        // Save the modified image
        File outputFile = new File(outputImagePath);
        ImageIO.write(image, "png", outputFile);
        System.out.println("Data encoded into image successfully.");
    }

    // Function to decode the secret data from the image
    public static String decodeImage(String stegoImagePath) throws IOException {
        File stegoImageFile = new File(stegoImagePath);
        BufferedImage stegoImage = ImageIO.read(stegoImageFile);

        StringBuilder binaryData = new StringBuilder();

        // Traverse each pixel and extract the LSB (Least Significant Bit)
        for (int i = 0; i < stegoImage.getWidth(); i++) {
            for (int j = 0; j < stegoImage.getHeight(); j++) {
                Color pixelColor = new Color(stegoImage.getRGB(i, j));

                // Extract the LSB of Red, Green, and Blue channels
                binaryData.append((pixelColor.getRed() & 1));
                binaryData.append((pixelColor.getGreen() & 1));
                binaryData.append((pixelColor.getBlue() & 1));
            }
        }

        // Convert the binary data to characters
        StringBuilder secretData = new StringBuilder();
        for (int i = 0; i + 8 <= binaryData.length(); i += 8) {
            String byteString = binaryData.substring(i, i + 8);
            char c = (char) Integer.parseInt(byteString, 2);

            if (c == '|') break; // Stop decoding when the terminator is found
            secretData.append(c);
        }

        return secretData.toString();
    }

    // Helper function to convert text to binary string
    public static String toBinary(String data) {
        StringBuilder binaryData = new StringBuilder();
        for (char c : data.toCharArray()) {
            binaryData.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binaryData.toString();
    }

    public static void main(String[] args) {
        try {
            String imagePath = "cover_image.png";  // The original image
            String secretData = "Hello, this is a secret message!"; // The secret data you want to hide
            String encodedImagePath = "encoded_image.png"; // Output file with hidden data

            // Encode the data into the image
            encodeImage(imagePath, secretData, encodedImagePath);
            
            // Decode the data from the stego image
            String decodedData = decodeImage(encodedImagePath);
            System.out.println("Decoded Data: " + decodedData);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
