package com.taxirank.backend.services.impl;

import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.ProfilePhotoService;
import com.taxirank.backend.storage.PhotoStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

@Service
public class ProfilePhotoServiceImpl implements ProfilePhotoService {

    static final int MAX_BYTES = 5 * 1024 * 1024;
    static final int OUTPUT_SIZE = 512;
    // Refuse to decode anything bigger: a small file can declare a huge canvas and exhaust memory
    private static final int MAX_DIMENSION = 10_000;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoStorage photoStorage;

    @Override
    @Transactional
    public User upload(Long userId, byte[] image) {
        User user = person(userId);
        if (image == null || image.length == 0) {
            throw new IllegalArgumentException("Choose a photo to upload");
        }
        if (image.length > MAX_BYTES) {
            throw new IllegalArgumentException("Photo must be 5 MB or smaller");
        }
        if (!isJpeg(image) && !isPng(image)) {
            throw new IllegalArgumentException("Photo must be a JPG or PNG");
        }
        String previous = user.getProfilePicture();
        user.setProfilePicture(photoStorage.save(toSquareJpeg(decode(image))));
        userRepository.save(user);
        photoStorage.delete(previous);
        return user;
    }

    @Override
    @Transactional
    public User remove(Long userId) {
        User user = person(userId);
        photoStorage.delete(user.getProfilePicture());
        user.setProfilePicture(null);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<byte[]> load(Long userId) {
        return userRepository.findById(userId)
                .map(User::getProfilePicture)
                .flatMap(photoStorage::load);
    }

    private User person(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.isRankAccount()) {
            throw new IllegalArgumentException("Rank accounts don't have a profile photo");
        }
        return user;
    }

    private static BufferedImage decode(byte[] bytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Photo must be a JPG or PNG");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                if (reader.getWidth(0) > MAX_DIMENSION || reader.getHeight(0) > MAX_DIMENSION) {
                    throw new IllegalArgumentException("Photo is too large; use one under 10 000 pixels a side");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("This photo couldn't be read; try another one");
        }
    }

    /** Centre-crops to a square and scales down to OUTPUT_SIZE. Re-encoding drops all metadata. */
    private static byte[] toSquareJpeg(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        int size = Math.min(side, OUTPUT_SIZE);

        BufferedImage square = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = square.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(java.awt.Color.WHITE); // transparent PNG areas become white, not black
            g.fillRect(0, 0, size, size);
            g.drawImage(source, 0, 0, size, size, x, y, x + side, y + side, null);
        } finally {
            g.dispose();
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(square, "jpg", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not encode photo", e);
        }
    }

    private static boolean isJpeg(byte[] b) {
        return b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] b) {
        return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }
}
