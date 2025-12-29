import { BadRequestException } from '@nestjs/common';
import { memoryStorage } from 'multer';
import { Request } from 'express';

/**
 * Memory storage configuration for profile pictures.
 * Files are stored in memory as Buffer for S3 upload.
 */
export const makeProfilePicStorageConfig = () => ({
  storage: memoryStorage(),
  limits: { fileSize: 5 * 1024 * 1024 }, // 5 MB
  fileFilter: (
    req: Request,
    file: Express.Multer.File,
    cb: (error: Error | null, acceptFile: boolean) => void,
  ) => {
    if (!file.mimetype.startsWith('image/')) {
      return cb(new BadRequestException('Only image files are allowed!'), false);
    }
    cb(null, true);
  },
});

/**
 * Memory storage configuration for creating new properties with images.
 * Files are stored in memory as Buffer for S3 upload.
 */
export const makePropertyCreationStorageConfig = () => ({
  storage: memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10 MB per file
  fileFilter: (
    req: Request,
    file: Express.Multer.File,
    cb: (error: Error | null, acceptFile: boolean) => void,
  ) => {
    if (!file.mimetype.startsWith('image/')) {
      return cb(new BadRequestException('Only image files are allowed!'), false);
    }
    cb(null, true);
  },
});

/**
 * Memory storage configuration for adding images to existing properties.
 * Files are stored in memory as Buffer for S3 upload.
 */
export const makePropertyImagesStorageConfig = () => ({
  storage: memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10 MB per file
  fileFilter: (
    req: Request,
    file: Express.Multer.File,
    cb: (error: Error | null, acceptFile: boolean) => void,
  ) => {
    if (!file.mimetype.startsWith('image/')) {
      return cb(new BadRequestException('Only image files are allowed!'), false);
    }
    cb(null, true);
  },
});
